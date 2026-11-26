package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.aura
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class SnowFlakeParticle(
    var normX: Float,
    var normY: Float,
    val depth: Float,       // 0.15f (distant) .. 1.0f (close)
    val radiusDp: Float,
    val fallSpeed: Float,   // normalized screen-heights per second
    val swayAmp: Float,     // normalized horizontal flutter speed
    val swayFreq: Float,
    var swayPhase: Float,
    var rotationDeg: Float,
    val spinSpeedDeg: Float,
    val baseAlpha: Float,
    val isCrystal: Boolean,
    val windResponse: Float
)

private class SnowFieldSimulation(
    count: Int,
    foregroundOnly: Boolean,
    seed: Int
) {
    val particles: Array<SnowFlakeParticle>

    init {
        val rng = Random(seed)
        particles = Array(count) { idx ->
            val depth = if (foregroundOnly) {
                0.76f + rng.nextFloat() * 0.24f
            } else {
                val tier = idx % 10
                when {
                    tier < 5 -> 0.16f + rng.nextFloat() * 0.24f // Far background mist flakes
                    tier < 9 -> 0.42f + rng.nextFloat() * 0.30f // Midground crisp flakes
                    else -> 0.74f + rng.nextFloat() * 0.22f     // Near depth flakes
                }
            }

            val isCrystal = if (foregroundOnly) {
                idx % 5 == 0
            } else {
                depth > 0.45f && idx % 7 == 0
            }

            val radiusDp = if (foregroundOnly) {
                4.8f + depth * 5.2f
            } else {
                1.0f + depth * 2.5f + if (isCrystal) 1.6f else 0f
            }

            val fallSpeed = (0.026f + depth * 0.088f) * (0.85f + rng.nextFloat() * 0.32f)
            val swayAmp = (0.006f + (1f - depth * 0.35f) * 0.016f) * (0.7f + rng.nextFloat() * 0.6f)
            val swayFreq = 0.85f + rng.nextFloat() * 1.45f
            val baseAlpha = if (foregroundOnly) {
                0.08f + rng.nextFloat() * 0.10f
            } else {
                (0.20f + depth * 0.56f).coerceIn(0.18f, 0.82f)
            }

            SnowFlakeParticle(
                normX = rng.nextFloat(),
                normY = rng.nextFloat(),
                depth = depth,
                radiusDp = radiusDp,
                fallSpeed = fallSpeed,
                swayAmp = swayAmp,
                swayFreq = swayFreq,
                swayPhase = rng.nextFloat() * (2f * PI.toFloat()),
                rotationDeg = rng.nextFloat() * 360f,
                spinSpeedDeg = (rng.nextFloat() - 0.5f) * 34f,
                baseAlpha = baseAlpha,
                isCrystal = isCrystal,
                windResponse = 0.45f + depth * 0.75f
            )
        }
    }

    fun step(dt: Float, elapsedSec: Float, resetSeedBase: Int) {
        // Multi-harmonic natural winter breeze with gentle gusts
        val windX = (
            sin(elapsedSec * 0.23f) * 0.52f +
                sin(elapsedSec * 0.57f + 1.35f) * 0.31f +
                cos(elapsedSec * 0.11f + 0.4f) * 0.17f
            ) * 0.036f
        val verticalGust = 1f + sin(elapsedSec * 0.37f) * 0.10f

        for (i in particles.indices) {
            val p = particles[i]
            p.swayPhase += p.swayFreq * dt
            val flutterX = sin(p.swayPhase) * p.swayAmp + cos(p.swayPhase * 0.53f) * (p.swayAmp * 0.35f)

            p.normX += (windX * p.windResponse + flutterX) * dt
            p.normY += p.fallSpeed * verticalGust * dt
            p.rotationDeg = (p.rotationDeg + p.spinSpeedDeg * dt) % 360f

            // Seamless wrap on horizontal borders
            if (p.normX > 1.06f) {
                p.normX -= 1.12f
            } else if (p.normX < -0.06f) {
                p.normX += 1.12f
            }

            // Respawn smoothly above the top edge after exiting bottom
            if (p.normY > 1.05f) {
                p.normY = -0.05f
                val hash = ((i * 2654435761L + resetSeedBase + (elapsedSec * 1000f).toLong()) and 0xFFFFL) / 65535f
                p.normX = (hash * 1.08f) - 0.04f
            }
        }
    }
}

/**
 * VSync-locked, zero-recomposition 3-depth atmospheric snowfall for the Home screen when streak == 0.
 * Updates particle kinematics directly via `withFrameNanos` inside a single Canvas DrawScope.
 */
@Composable
fun HomeSnowfallLayer(
    isActive: Boolean,
    isForegroundBokeh: Boolean = false,
    modifier: Modifier = Modifier
) {
    val visibilityAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "home_snowfall_visibility"
    )
    if (visibilityAlpha <= 0.01f) return

    val aura = MaterialTheme.aura
    val simulation = remember(isForegroundBokeh) {
        SnowFieldSimulation(
            count = if (isForegroundBokeh) 14 else 78,
            foregroundOnly = isForegroundBokeh,
            seed = if (isForegroundBokeh) 4099 else 1337
        )
    }

    var frameClockSec by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isActive, isForegroundBokeh) {
        var lastNanos = 0L
        var elapsed = 0f
        while (coroutineContext.isActive) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.045f)
                    elapsed += dt
                    simulation.step(
                        dt = dt,
                        elapsedSec = elapsed,
                        resetSeedBase = if (isForegroundBokeh) 91 else 37
                    )
                    frameClockSec = elapsed
                }
                lastNanos = now
            }
        }
    }

    val coreColor = if (aura.isDark) Color(0xFFFFFFFF) else Color(0xFF0284C7)
    val iceTintColor = if (aura.isDark) Color(0xFFBAE6FD) else Color(0xFF38BDF8)
    val crystalTintColor = if (aura.isDark) Color(0xFFE0F7FA) else Color(0xFF0369A1)

    Canvas(modifier = modifier.fillMaxSize()) {
        // Read frameClockSec inside DrawScope so only the Canvas redraws at 60-120 FPS
        @Suppress("UNUSED_VARIABLE")
        val tick = frameClockSec
        val w = size.width
        val h = size.height
        if (w <= 1f || h <= 1f) return@Canvas

        val densityGlowMultiplier = visibilityAlpha

        for (p in simulation.particles) {
            val x = p.normX * w
            val y = p.normY * h

            // Smooth vertical edge fade-in at top and fade-out near bottom
            val topFade = ((p.normY + 0.04f) / 0.10f).coerceIn(0f, 1f)
            val bottomFade = ((1.03f - p.normY) / 0.18f).coerceIn(0f, 1f)
            val alpha = (p.baseAlpha * topFade * bottomFade * densityGlowMultiplier).coerceIn(0f, 1f)
            if (alpha <= 0.008f) continue

            val radiusPx = p.radiusDp.dp.toPx()

            if (isForegroundBokeh) {
                drawSoftBokehFlake(
                    center = Offset(x, y),
                    radiusPx = radiusPx,
                    coreColor = coreColor,
                    haloColor = iceTintColor,
                    alpha = alpha
                )
            } else if (p.isCrystal) {
                drawIceCrystalFlake(
                    center = Offset(x, y),
                    radiusPx = radiusPx,
                    rotationDeg = p.rotationDeg,
                    color = crystalTintColor,
                    alpha = alpha * 0.85f
                )
            } else {
                drawSoftSnowFlake(
                    center = Offset(x, y),
                    radiusPx = radiusPx,
                    depth = p.depth,
                    coreColor = coreColor,
                    haloColor = iceTintColor,
                    alpha = alpha
                )
            }
        }
    }
}

private fun DrawScope.drawSoftSnowFlake(
    center: Offset,
    radiusPx: Float,
    depth: Float,
    coreColor: Color,
    haloColor: Color,
    alpha: Float
) {
    // Zero-allocation multi-pass atmospheric bloom for smooth depth-of-field
    drawCircle(
        color = haloColor.copy(alpha = (alpha * 0.14f).coerceIn(0f, 1f)),
        radius = radiusPx * (2.1f + depth * 0.6f),
        center = center
    )
    drawCircle(
        color = haloColor.copy(alpha = (alpha * 0.34f).coerceIn(0f, 1f)),
        radius = radiusPx * 1.28f,
        center = center
    )
    drawCircle(
        color = coreColor.copy(alpha = (alpha * 0.92f).coerceIn(0f, 1f)),
        radius = radiusPx * 0.68f,
        center = center
    )
}

private fun DrawScope.drawSoftBokehFlake(
    center: Offset,
    radiusPx: Float,
    coreColor: Color,
    haloColor: Color,
    alpha: Float
) {
    drawCircle(
        color = haloColor.copy(alpha = (alpha * 0.14f).coerceIn(0f, 1f)),
        radius = radiusPx * 2.05f,
        center = center
    )
    drawCircle(
        color = coreColor.copy(alpha = (alpha * 0.34f).coerceIn(0f, 1f)),
        radius = radiusPx * 1.25f,
        center = center
    )
    drawCircle(
        color = coreColor.copy(alpha = (alpha * 0.54f).coerceIn(0f, 1f)),
        radius = radiusPx * 0.65f,
        center = center
    )
}

private fun DrawScope.drawIceCrystalFlake(
    center: Offset,
    radiusPx: Float,
    rotationDeg: Float,
    color: Color,
    alpha: Float
) {
    val strokeW = (radiusPx * 0.22f).coerceAtLeast(1.0f)
    val armColor = color.copy(alpha = alpha)
    // Soft glow behind crystal
    drawCircle(
        color = color.copy(alpha = (alpha * 0.16f).coerceIn(0f, 1f)),
        radius = radiusPx * 1.55f,
        center = center
    )

    rotate(degrees = rotationDeg, pivot = center) {
        val branchOffset = radiusPx * 0.56f
        val branchLen = radiusPx * 0.34f
        for (i in 0 until 6) {
            val angleRad = (i * (PI / 3.0)).toFloat()
            val cosA = cos(angleRad)
            val sinA = sin(angleRad)
            val tip = Offset(center.x + cosA * radiusPx, center.y + sinA * radiusPx)
            drawLine(
                color = armColor,
                start = center,
                end = tip,
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Delicate lateral crystal chevrons near each tip
            val mid = Offset(center.x + cosA * branchOffset, center.y + sinA * branchOffset)
            val leftAngle = angleRad + 0.62f
            val rightAngle = angleRad - 0.62f
            drawLine(
                color = armColor,
                start = mid,
                end = Offset(mid.x + cos(leftAngle) * branchLen, mid.y + sin(leftAngle) * branchLen),
                strokeWidth = strokeW * 0.8f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = armColor,
                start = mid,
                end = Offset(mid.x + cos(rightAngle) * branchLen, mid.y + sin(rightAngle) * branchLen),
                strokeWidth = strokeW * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}
