package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.StoreAvatarItem
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure Jetpack Compose 60 FPS animated Avatar & Holographic Aura Frame renderer.
 * Designed around Liquid Glass Dark Mode (#000000 OLED black core, frosted glass translucent layers,
 * luminous neon vector geometry, zero external bitmap dependencies).
 */
@Composable
fun CyberAvatarPortraitCanvas(
    avatar: StoreAvatarItem,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showAuraFrame: Boolean = true
) {
    val infinite = rememberInfiniteTransition(label = "cyber_avatar_${avatar.id}")
    // Smooth unbroken continuous loop cycle (integer-multiple harmonic frequencies ensure zero seam snap)
    val cycle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cyber_avatar_cycle"
    )

    val clipCirclePath = remember { Path() }
    val workPathA = remember { Path() }
    val workPathB = remember { Path() }

    val primary = remember(avatar) { Color(avatar.primaryAccentHex) }
    val secondary = remember(avatar) { Color(avatar.secondaryAccentHex) }
    val tertiary = remember(avatar) { Color(avatar.tertiaryAccentHex) }

    Box(
        modifier = modifier.semantics {
            contentDescription = avatar.displayNameEn
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minDim = minOf(w, h)
            val cx = w * 0.5f
            val cy = h * 0.5f
            val frameRadius = if (showAuraFrame) minDim * 0.435f else minDim * 0.47f
            val portraitRadius = if (showAuraFrame) minDim * 0.365f else minDim * 0.45f
            val tau = (2.0 * PI).toFloat()
            val angleRad = cycle * tau
            val pulse = 0.5f + 0.5f * sin(angleRad * 2f)

            // 1. Deep OLED Ambient Glow Backdrop behind frame
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.22f + 0.08f * pulse),
                        secondary.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = minDim * 0.50f
                ),
                center = Offset(cx, cy),
                radius = minDim * 0.50f
            )

            // 2. Holographic Aura Frame (Outer rotating rings & avatar-bespoke particles)
            if (showAuraFrame) {
                drawHolographicAuraFrame(
                    avatar = avatar,
                    cx = cx,
                    cy = cy,
                    frameRadius = frameRadius,
                    portraitRadius = portraitRadius,
                    cycle = cycle,
                    angleRad = angleRad,
                    pulse = pulse,
                    primary = primary,
                    secondary = secondary,
                    tertiary = tertiary,
                    isCompact = isCompact,
                    hexPath = workPathA
                )
            }

            // 3. Clipped OLED #000000 Portrait Aperture + Distinct Avatar Engine
            clipCirclePath.reset()
            clipCirclePath.addOval(
                Rect(
                    left = cx - portraitRadius,
                    top = cy - portraitRadius,
                    right = cx + portraitRadius,
                    bottom = cy + portraitRadius
                )
            )

            clipPath(clipCirclePath) {
                // OLED #000000 void base inside portrait medallion
                drawCircle(
                    color = Color(0xFF000000),
                    radius = portraitRadius,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.18f),
                            secondary.copy(alpha = 0.08f),
                            Color(0xFF000000)
                        ),
                        center = Offset(cx, cy - portraitRadius * 0.15f),
                        radius = portraitRadius * 1.1f
                    ),
                    radius = portraitRadius,
                    center = Offset(cx, cy)
                )

                when (avatar) {
                    StoreAvatarItem.CYBER_SAMURAI -> drawCyberSamurai(
                        cx, cy, portraitRadius, angleRad, pulse, primary, secondary, tertiary, workPathA, workPathB
                    )
                    StoreAvatarItem.NEURAL_HACKER -> drawNeuralHacker(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA
                    )
                    StoreAvatarItem.QUANTUM_GHOST -> drawQuantumGhost(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA
                    )
                    StoreAvatarItem.DEEP_SPACE_PILOT -> drawDeepSpacePilot(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA
                    )
                    StoreAvatarItem.NEON_GLITCH -> drawNeonGlitch(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA
                    )
                    StoreAvatarItem.MECHA_ANDROID -> drawMechaAndroid(
                        cx, cy, portraitRadius, angleRad, pulse, primary, secondary, tertiary, workPathA, workPathB
                    )
                    StoreAvatarItem.VOID_WANDERER -> drawVoidWanderer(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA
                    )
                    StoreAvatarItem.BIO_LUMINESCENT_SYNTH -> drawBioLuminescentSynth(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA
                    )
                    StoreAvatarItem.APEX_CYBER_HUNTER -> drawApexCyberHunter(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA, workPathB
                    )
                    StoreAvatarItem.AURA_SOVEREIGN -> drawAuraSovereign(
                        cx, cy, portraitRadius, cycle, angleRad, pulse, primary, secondary, tertiary, workPathA, workPathB
                    )
                }

                // Liquid Glass top specular sheen across medallion
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        startY = cy - portraitRadius,
                        endY = cy - portraitRadius * 0.15f
                    ),
                    topLeft = Offset(cx - portraitRadius * 0.80f, cy - portraitRadius * 0.94f),
                    size = Size(portraitRadius * 1.60f, portraitRadius * 0.72f)
                )
            }

            // Inner frosted glass rim framing portrait
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        primary.copy(alpha = 0.75f),
                        secondary.copy(alpha = 0.55f)
                    ),
                    start = Offset(cx - portraitRadius, cy - portraitRadius),
                    end = Offset(cx + portraitRadius, cy + portraitRadius)
                ),
                radius = portraitRadius,
                center = Offset(cx, cy),
                style = Stroke(width = if (isCompact) 1.3.dp.toPx() else 1.8.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawHolographicAuraFrame(
    avatar: StoreAvatarItem,
    cx: Float,
    cy: Float,
    frameRadius: Float,
    portraitRadius: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    isCompact: Boolean,
    hexPath: Path
) {
    val strokeBase = if (isCompact) 1.6.dp.toPx() else 2.4.dp.toPx()
    val midRadius = (frameRadius + portraitRadius) * 0.5f

    // Continuous counter-rotating holographic sweep track
    rotate(degrees = cycle * 360f, pivot = Offset(cx, cy)) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    primary.copy(alpha = 0.92f),
                    Color.Transparent,
                    secondary.copy(alpha = 0.85f),
                    tertiary.copy(alpha = 0.65f),
                    Color.Transparent,
                    primary.copy(alpha = 0.92f)
                ),
                center = Offset(cx, cy)
            ),
            radius = frameRadius,
            center = Offset(cx, cy),
            style = Stroke(width = strokeBase, cap = StrokeCap.Round)
        )
    }

    // Secondary dashed counter-ring
    rotate(degrees = -cycle * 360f, pivot = Offset(cx, cy)) {
        drawCircle(
            color = secondary.copy(alpha = 0.45f + 0.25f * pulse),
            radius = midRadius,
            center = Offset(cx, cy),
            style = Stroke(
                width = strokeBase * 0.55f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(frameRadius * 0.22f, frameRadius * 0.14f),
                    0f
                ),
                cap = StrokeCap.Round
            )
        )
    }

    // Avatar-bespoke frame accents (plasma sparks, hex armor, targeting ticks, crown rays, etc.)
    when (avatar) {
        StoreAvatarItem.CYBER_SAMURAI -> {
            // Rotating Plasma-Spark Cyber Frame: 6 plasma spark streaks orbiting outer halo
            for (i in 0 until 6) {
                val a = angleRad * 2f + i * (PI.toFloat() / 3f)
                val x = cx + cos(a) * (frameRadius + 2.dp.toPx())
                val y = cy + sin(a) * (frameRadius + 2.dp.toPx())
                val c = if (i % 2 == 0) primary else secondary
                drawCircle(color = c.copy(alpha = 0.35f), radius = strokeBase * 1.9f, center = Offset(x, y))
                drawCircle(color = Color.White, radius = strokeBase * 0.65f, center = Offset(x, y))
            }
        }
        StoreAvatarItem.MECHA_ANDROID -> {
            // Hex-Reactor Armor Frame
            rotate(degrees = cycle * 360f, pivot = Offset(cx, cy)) {
                hexPath.reset()
                val hr = frameRadius + 1.5.dp.toPx()
                for (i in 0 until 6) {
                    val a = i * (PI.toFloat() / 3f)
                    val px = cx + cos(a) * hr
                    val py = cy + sin(a) * hr
                    if (i == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
                }
                hexPath.close()
                drawPath(
                    path = hexPath,
                    color = primary.copy(alpha = 0.58f + 0.25f * pulse),
                    style = Stroke(width = strokeBase * 0.72f, join = StrokeJoin.Miter)
                )
            }
        }
        StoreAvatarItem.APEX_CYBER_HUNTER -> {
            // 4 cardinal tactical reticle ticks spinning outside the ring
            rotate(degrees = -cycle * 360f, pivot = Offset(cx, cy)) {
                val tickLen = frameRadius * 0.16f
                for (q in 0 until 4) {
                    val a = q * (PI.toFloat() / 2f)
                    val ix = cx + cos(a) * (frameRadius - tickLen * 0.3f)
                    val iy = cy + sin(a) * (frameRadius - tickLen * 0.3f)
                    val ox = cx + cos(a) * (frameRadius + tickLen)
                    val oy = cy + sin(a) * (frameRadius + tickLen)
                    drawLine(
                        color = if (q % 2 == 0) primary else secondary,
                        start = Offset(ix, iy),
                        end = Offset(ox, oy),
                        strokeWidth = strokeBase,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        StoreAvatarItem.AURA_SOVEREIGN -> {
            // 8 royal diamond prisms orbiting
            for (i in 0 until 8) {
                val a = angleRad + i * (PI.toFloat() / 4f)
                val rx = cx + cos(a) * (frameRadius + 1.5.dp.toPx())
                val ry = cy + sin(a) * (frameRadius + 1.5.dp.toPx())
                drawCircle(
                    color = if (i % 2 == 0) primary else secondary,
                    radius = strokeBase * (0.75f + 0.25f * pulse),
                    center = Offset(rx, ry)
                )
            }
        }
        else -> {
            // Twin orbital photon satellites
            for (i in 0 until 3) {
                val a = angleRad * 2f + i * (2f * PI.toFloat() / 3f)
                val px = cx + cos(a) * frameRadius
                val py = cy + sin(a) * frameRadius
                drawCircle(
                    color = (if (i == 0) primary else secondary).copy(alpha = 0.45f),
                    radius = strokeBase * 1.55f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White,
                    radius = strokeBase * 0.58f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private fun DrawScope.drawCyberSamurai(
    cx: Float,
    cy: Float,
    r: Float,
    angleRad: Float,
    pulse: Float,
    cyan: Color,
    orange: Color,
    tertiary: Color,
    pathA: Path,
    pathB: Path
) {
    // Shoulders silhouette
    drawArc(
        brush = Brush.verticalGradient(listOf(Color(0xFF141B29), Color(0xFF060910))),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.85f, cy + r * 0.32f),
        size = Size(r * 1.70f, r * 1.1f)
    )

    // Kabuto Helmet Dome
    pathA.reset()
    pathA.moveTo(cx - r * 0.56f, cy - r * 0.05f)
    pathA.cubicTo(cx - r * 0.58f, cy - r * 0.68f, cx + r * 0.58f, cy - r * 0.68f, cx + r * 0.56f, cy - r * 0.05f)
    pathA.lineTo(cx + r * 0.42f, cy + r * 0.48f)
    pathA.lineTo(cx, cy + r * 0.62f)
    pathA.lineTo(cx - r * 0.42f, cy + r * 0.48f)
    pathA.close()
    drawPath(
        path = pathA,
        brush = Brush.verticalGradient(listOf(Color(0xFF172033), Color(0xFF090D16)))
    )
    drawPath(
        path = pathA,
        color = cyan.copy(alpha = 0.6f),
        style = Stroke(width = r * 0.035f, join = StrokeJoin.Round)
    )

    // Twin Samurai V-Crest Horns
    pathB.reset()
    pathB.moveTo(cx, cy - r * 0.36f)
    pathB.lineTo(cx - r * 0.44f, cy - r * 0.72f)
    pathB.lineTo(cx - r * 0.14f, cy - r * 0.44f)
    pathB.lineTo(cx, cy - r * 0.56f)
    pathB.lineTo(cx + r * 0.14f, cy - r * 0.44f)
    pathB.lineTo(cx + r * 0.44f, cy - r * 0.72f)
    pathB.close()
    drawPath(path = pathB, brush = Brush.horizontalGradient(listOf(cyan, orange, cyan)))

    // Glowing Dual Cyber Eyes (Left Blue/Cyan, Right Blazing Orange)
    val eyeGlow = 0.72f + 0.28f * pulse
    // Left eye
    pathB.reset()
    pathB.moveTo(cx - r * 0.34f, cy - r * 0.10f)
    pathB.lineTo(cx - r * 0.10f, cy - r * 0.03f)
    pathB.lineTo(cx - r * 0.18f, cy + r * 0.05f)
    pathB.lineTo(cx - r * 0.36f, cy - r * 0.02f)
    pathB.close()
    drawPath(pathB, color = cyan.copy(alpha = eyeGlow))
    drawCircle(cyan.copy(alpha = 0.32f * eyeGlow), radius = r * 0.18f, center = Offset(cx - r * 0.23f, cy - r * 0.03f))

    // Right eye
    pathB.reset()
    pathB.moveTo(cx + r * 0.34f, cy - r * 0.10f)
    pathB.lineTo(cx + r * 0.10f, cy - r * 0.03f)
    pathB.lineTo(cx + r * 0.18f, cy + r * 0.05f)
    pathB.lineTo(cx + r * 0.36f, cy - r * 0.02f)
    pathB.close()
    drawPath(pathB, color = orange.copy(alpha = eyeGlow))
    drawCircle(orange.copy(alpha = 0.34f * eyeGlow), radius = r * 0.18f, center = Offset(cx + r * 0.23f, cy - r * 0.03f))

    // Tactical Menpo Lower Jaw Mask with Glowing Slats
    pathB.reset()
    pathB.moveTo(cx - r * 0.36f, cy + r * 0.14f)
    pathB.lineTo(cx, cy + r * 0.06f)
    pathB.lineTo(cx + r * 0.36f, cy + r * 0.14f)
    pathB.lineTo(cx + r * 0.30f, cy + r * 0.45f)
    pathB.lineTo(cx, cy + r * 0.56f)
    pathB.lineTo(cx - r * 0.30f, cy + r * 0.45f)
    pathB.close()
    drawPath(pathB, color = Color(0xFF0A0F1A))
    drawPath(pathB, color = tertiary.copy(alpha = 0.5f), style = Stroke(width = r * 0.025f))

    for (s in 0 until 3) {
        val sy = cy + r * (0.24f + s * 0.09f)
        val halfW = r * (0.20f - s * 0.04f)
        drawLine(
            color = cyan.copy(alpha = 0.5f + 0.3f * sin(angleRad + s)),
            start = Offset(cx - halfW, sy),
            end = Offset(cx + halfW, sy),
            strokeWidth = r * 0.028f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawNeuralHacker(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    matrixGreen: Color,
    cyan: Color,
    mint: Color,
    pathA: Path
) {
    // Cascading Matrix Code Streams behind & subtly over portrait
    val cols = 9
    for (c in 0 until cols) {
        val x = cx - r * 0.78f + (c / (cols - 1f)) * (r * 1.56f)
        val speedFactor = 1f + (c % 3)
        val dropY = ((cycle * speedFactor + c * 0.19f) % 1f) * (r * 2f) + (cy - r)
        for (seg in 0 until 5) {
            val y = dropY - seg * r * 0.20f
            val fade = (1f - seg / 5f) * 0.55f
            drawLine(
                color = (if (seg == 0) Color.White else if (c % 2 == 0) matrixGreen else cyan).copy(alpha = fade),
                start = Offset(x, y),
                end = Offset(x, y + r * 0.11f),
                strokeWidth = r * 0.032f,
                cap = StrokeCap.Round
            )
        }
    }

    // Dark Digital Bust & Head Silhouette
    drawArc(
        color = Color(0xFF0B1416).copy(alpha = 0.92f),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.76f, cy + r * 0.32f),
        size = Size(r * 1.52f, r * 1.05f)
    )
    pathA.reset()
    pathA.addOval(Rect(cx - r * 0.38f, cy - r * 0.48f, cx + r * 0.38f, cy + r * 0.42f))
    drawPath(pathA, color = Color(0xE80A1318))
    drawPath(pathA, color = matrixGreen.copy(alpha = 0.55f), style = Stroke(width = r * 0.03f))

    // Holographic HUD Visor Glasses
    val glassLeft = cx - r * 0.41f
    val glassTop = cy - r * 0.16f
    val glassW = r * 0.82f
    val glassH = r * 0.22f
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(matrixGreen.copy(alpha = 0.38f), cyan.copy(alpha = 0.42f), matrixGreen.copy(alpha = 0.38f))
        ),
        topLeft = Offset(glassLeft, glassTop),
        size = Size(glassW, glassH),
        cornerRadius = CornerRadius(r * 0.07f, r * 0.07f)
    )
    drawRoundRect(
        color = mint,
        topLeft = Offset(glassLeft, glassTop),
        size = Size(glassW, glassH),
        cornerRadius = CornerRadius(r * 0.07f, r * 0.07f),
        style = Stroke(width = r * 0.032f)
    )

    // Sweeping HUD telemetry beam inside glasses
    val beamX = glassLeft + ((cycle * 2f) % 1f) * glassW
    drawLine(
        color = Color.White.copy(alpha = 0.78f + 0.22f * pulse),
        start = Offset(beamX, glassTop + r * 0.03f),
        end = Offset(beamX, glassTop + glassH - r * 0.03f),
        strokeWidth = r * 0.035f,
        cap = StrokeCap.Round
    )
    // Binary telemetry dots on glasses
    for (d in 0 until 4) {
        val bx = glassLeft + glassW * (0.18f + d * 0.21f)
        val by = glassTop + glassH * 0.5f
        val blink = 0.35f + 0.65f * abs(sin(angleRad * 2f + d * 1.1f))
        drawCircle(color = cyan.copy(alpha = blink), radius = r * 0.028f, center = Offset(bx, by))
    }
}

private fun DrawScope.drawQuantumGhost(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    iceCyan: Color,
    whiteIce: Color,
    indigo: Color,
    pathA: Path
) {
    // Floating Spectral Translucent Frosted Glass Silhouette
    val floatY = sin(angleRad) * r * 0.06f
    pathA.reset()
    pathA.moveTo(cx - r * 0.48f, cy + r * 0.56f + floatY)
    pathA.cubicTo(
        cx - r * 0.54f, cy - r * 0.15f + floatY,
        cx - r * 0.36f, cy - r * 0.58f + floatY,
        cx, cy - r * 0.58f + floatY
    )
    pathA.cubicTo(
        cx + r * 0.36f, cy - r * 0.58f + floatY,
        cx + r * 0.54f, cy - r * 0.15f + floatY,
        cx + r * 0.48f, cy + r * 0.56f + floatY
    )
    // Wavy spectral hem
    pathA.quadraticBezierTo(cx + r * 0.26f, cy + r * 0.42f + floatY, cx, cy + r * 0.58f + floatY)
    pathA.quadraticBezierTo(cx - r * 0.26f, cy + r * 0.42f + floatY, cx - r * 0.48f, cy + r * 0.56f + floatY)
    pathA.close()

    drawPath(
        path = pathA,
        brush = Brush.verticalGradient(
            listOf(
                whiteIce.copy(alpha = 0.34f),
                iceCyan.copy(alpha = 0.22f),
                indigo.copy(alpha = 0.16f)
            )
        )
    )
    drawPath(
        path = pathA,
        brush = Brush.linearGradient(listOf(whiteIce.copy(alpha = 0.8f), iceCyan.copy(alpha = 0.55f))),
        style = Stroke(width = r * 0.036f, join = StrokeJoin.Round)
    )

    // Internal Floating Quantum Energy Particles inside translucent silhouette
    for (p in 0 until 8) {
        val phase = (cycle * (1f + (p % 2)) + p * 0.125f) % 1f
        val px = cx + sin(angleRad * 2f + p * 1.4f) * r * 0.25f
        val py = cy + r * 0.36f - phase * r * 0.78f + floatY
        val pAlpha = sin(phase * PI.toFloat()).coerceIn(0f, 1f)
        drawCircle(
            color = iceCyan.copy(alpha = pAlpha * 0.45f),
            radius = r * 0.07f,
            center = Offset(px, py)
        )
        drawCircle(
            color = whiteIce.copy(alpha = pAlpha * 0.9f),
            radius = r * 0.028f,
            center = Offset(px, py)
        )
    }

    // Glowing Spectral Eyes
    val eyeAlpha = 0.72f + 0.28f * pulse
    drawOval(
        color = whiteIce.copy(alpha = eyeAlpha),
        topLeft = Offset(cx - r * 0.22f, cy - r * 0.20f + floatY),
        size = Size(r * 0.11f, r * 0.15f)
    )
    drawOval(
        color = whiteIce.copy(alpha = eyeAlpha),
        topLeft = Offset(cx + r * 0.11f, cy - r * 0.20f + floatY),
        size = Size(r * 0.11f, r * 0.15f)
    )
}

private fun DrawScope.drawDeepSpacePilot(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    violet: Color,
    cyan: Color,
    pink: Color,
    pathA: Path
) {
    // Space Suit Collar Shoulders
    drawArc(
        brush = Brush.verticalGradient(listOf(Color(0xFF1D2436), Color(0xFF0A0D16))),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.80f, cy + r * 0.36f),
        size = Size(r * 1.60f, r * 1.0f)
    )

    // Helmet Outer Shell
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF212B42), Color(0xFF0A0F1C)),
            start = Offset(cx - r * 0.5f, cy - r * 0.6f),
            end = Offset(cx + r * 0.5f, cy + r * 0.5f)
        ),
        radius = r * 0.58f,
        center = Offset(cx, cy - r * 0.04f)
    )
    drawCircle(
        color = cyan.copy(alpha = 0.45f),
        radius = r * 0.58f,
        center = Offset(cx, cy - r * 0.04f),
        style = Stroke(width = r * 0.032f)
    )

    // Wide Panoramic Visor reflecting Moving Star Nebulae
    pathA.reset()
    pathA.addOval(Rect(cx - r * 0.44f, cy - r * 0.34f, cx + r * 0.44f, cy + r * 0.26f))
    clipPath(pathA) {
        drawRect(
            color = Color(0xFF04050D),
            topLeft = Offset(cx - r * 0.46f, cy - r * 0.36f),
            size = Size(r * 0.92f, r * 0.64f)
        )
        // Drifting Cosmic Nebula Waves inside visor
        val nebShiftX = cos(angleRad) * r * 0.16f
        val nebShiftY = sin(angleRad) * r * 0.08f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(violet.copy(alpha = 0.72f), pink.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(cx - r * 0.14f + nebShiftX, cy - r * 0.08f + nebShiftY),
                radius = r * 0.42f
            ),
            radius = r * 0.42f,
            center = Offset(cx - r * 0.14f + nebShiftX, cy - r * 0.08f + nebShiftY)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(cyan.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(cx + r * 0.18f - nebShiftX, cy + r * 0.04f - nebShiftY),
                radius = r * 0.36f
            ),
            radius = r * 0.36f,
            center = Offset(cx + r * 0.18f - nebShiftX, cy + r * 0.04f - nebShiftY)
        )

        // Subtle Starlight Twinkles
        for (s in 0 until 7) {
            val sx = cx - r * 0.32f + ((s * 0.19f + cycle * 0.5f) % 1f) * (r * 0.64f)
            val sy = cy - r * 0.24f + ((s * 0.31f) % 1f) * (r * 0.40f)
            val twinkle = 0.3f + 0.7f * abs(sin(angleRad * 2f + s * 1.2f))
            drawCircle(color = Color.White.copy(alpha = twinkle), radius = r * 0.022f, center = Offset(sx, sy))
        }
    }
    drawPath(
        path = pathA,
        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.65f), cyan, violet)),
        style = Stroke(width = r * 0.035f)
    )

    // Helmet Oxygen Chin Node Glow
    drawRoundRect(
        color = cyan.copy(alpha = 0.65f + 0.35f * pulse),
        topLeft = Offset(cx - r * 0.14f, cy + r * 0.34f),
        size = Size(r * 0.28f, r * 0.08f),
        cornerRadius = CornerRadius(r * 0.04f, r * 0.04f)
    )
}

private fun DrawScope.drawNeonGlitch(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    pink: Color,
    cyan: Color,
    violet: Color,
    pathA: Path
) {
    // Smooth periodic chromatic split oscillation
    val splitOffset = r * (0.045f + 0.045f * abs(sin(angleRad * 3f)))

    // Left Cyan Chromatic Aberration Silhouette
    drawCircle(
        color = cyan.copy(alpha = 0.55f),
        radius = r * 0.34f,
        center = Offset(cx - splitOffset, cy - r * 0.14f)
    )
    drawArc(
        color = cyan.copy(alpha = 0.45f),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.68f - splitOffset, cy + r * 0.26f),
        size = Size(r * 1.36f, r * 0.96f)
    )

    // Right Neon Pink Chromatic Aberration Silhouette
    drawCircle(
        color = pink.copy(alpha = 0.55f),
        radius = r * 0.34f,
        center = Offset(cx + splitOffset, cy - r * 0.14f)
    )
    drawArc(
        color = pink.copy(alpha = 0.45f),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.68f + splitOffset, cy + r * 0.26f),
        size = Size(r * 1.36f, r * 0.96f)
    )

    // Crisp Dark Center Mask Portrait
    drawCircle(
        color = Color(0xFF0A0812),
        radius = r * 0.33f,
        center = Offset(cx, cy - r * 0.14f)
    )
    drawArc(
        color = Color(0xFF0A0812),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.66f, cy + r * 0.26f),
        size = Size(r * 1.32f, r * 0.94f)
    )

    // Sharp Pulsing Neon Glitch Energy Bars across the portrait
    for (g in 0 until 5) {
        val gy = cy - r * 0.46f + ((g * 0.22f + cycle * 2f) % 1f) * (r * 0.92f)
        val gWidth = r * (0.45f + 0.35f * sin(angleRad * 2f + g))
        val gxShift = cos(angleRad * 3f + g * 1.7f) * r * 0.14f
        drawLine(
            color = (if (g % 2 == 0) pink else cyan).copy(alpha = 0.72f + 0.28f * pulse),
            start = Offset(cx - gWidth * 0.5f + gxShift, gy),
            end = Offset(cx + gWidth * 0.5f + gxShift, gy),
            strokeWidth = r * (0.025f + (g % 2) * 0.015f),
            cap = StrokeCap.Square
        )
    }

    // Asymmetric Dual Glitch Eyes
    pathA.reset()
    pathA.moveTo(cx - r * 0.22f, cy - r * 0.18f)
    pathA.lineTo(cx - r * 0.07f, cy - r * 0.12f)
    pathA.lineTo(cx - r * 0.20f, cy - r * 0.10f)
    pathA.close()
    drawPath(pathA, color = cyan)

    pathA.reset()
    pathA.moveTo(cx + r * 0.22f, cy - r * 0.18f)
    pathA.lineTo(cx + r * 0.07f, cy - r * 0.12f)
    pathA.lineTo(cx + r * 0.20f, cy - r * 0.10f)
    pathA.close()
    drawPath(pathA, color = pink)

    // Subtle Violet Center Bolt Accent
    drawCircle(
        color = violet.copy(alpha = 0.35f * pulse),
        radius = r * 0.25f,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.018f)
    )
}

private fun DrawScope.drawMechaAndroid(
    cx: Float,
    cy: Float,
    r: Float,
    angleRad: Float,
    pulse: Float,
    nuclearOrange: Color,
    gold: Color,
    crimsonOrange: Color,
    pathA: Path,
    pathB: Path
) {
    // Matte Black Steel Armored Shoulders
    drawArc(
        color = Color(0xFF14161B),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.80f, cy + r * 0.34f),
        size = Size(r * 1.60f, r * 1.05f)
    )

    // Angular Mechanical Faceplate
    pathA.reset()
    pathA.moveTo(cx - r * 0.38f, cy - r * 0.50f)
    pathA.lineTo(cx + r * 0.38f, cy - r * 0.50f)
    pathA.lineTo(cx + r * 0.48f, cy - r * 0.05f)
    pathA.lineTo(cx + r * 0.30f, cy + r * 0.46f)
    pathA.lineTo(cx - r * 0.30f, cy + r * 0.46f)
    pathA.lineTo(cx - r * 0.48f, cy - r * 0.05f)
    pathA.close()
    drawPath(
        path = pathA,
        brush = Brush.verticalGradient(listOf(Color(0xFF232730), Color(0xFF111318)))
    )
    drawPath(pathA, color = Color(0xFF3E4554), style = Stroke(width = r * 0.03f, join = StrokeJoin.Miter))

    // Glowing Orange Nuclear Core Seam Joints & Temple Hinges
    val coreAlpha = 0.70f + 0.30f * pulse
    // Forehead seam line
    drawLine(
        color = nuclearOrange.copy(alpha = coreAlpha),
        start = Offset(cx, cy - r * 0.50f),
        end = Offset(cx, cy - r * 0.18f),
        strokeWidth = r * 0.035f
    )
    // Jaw mechanical joints (Left & Right)
    drawLine(
        color = nuclearOrange.copy(alpha = coreAlpha),
        start = Offset(cx - r * 0.42f, cy + r * 0.08f),
        end = Offset(cx - r * 0.22f, cy + r * 0.24f),
        strokeWidth = r * 0.032f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = nuclearOrange.copy(alpha = coreAlpha),
        start = Offset(cx + r * 0.42f, cy + r * 0.08f),
        end = Offset(cx + r * 0.22f, cy + r * 0.24f),
        strokeWidth = r * 0.032f,
        cap = StrokeCap.Round
    )

    // Glowing Optical Visor Slit
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(crimsonOrange, gold, crimsonOrange)),
        topLeft = Offset(cx - r * 0.32f, cy - r * 0.14f),
        size = Size(r * 0.64f, r * 0.11f),
        cornerRadius = CornerRadius(r * 0.04f, r * 0.04f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = r * 0.045f,
        center = Offset(cx + cos(angleRad) * r * 0.14f, cy - r * 0.085f)
    )

    // Chest Nuclear Core Turbine below chin
    val coreCenter = Offset(cx, cy + r * 0.62f)
    drawCircle(
        brush = Brush.radialGradient(listOf(gold, nuclearOrange, Color.Transparent), center = coreCenter, radius = r * 0.22f),
        radius = r * 0.22f,
        center = coreCenter
    )
    pathB.reset()
    pathB.addOval(Rect(coreCenter.x - r * 0.11f, coreCenter.y - r * 0.11f, coreCenter.x + r * 0.11f, coreCenter.y + r * 0.11f))
    drawPath(pathB, color = Color(0xFF0A0B0E))
    drawCircle(color = gold.copy(alpha = coreAlpha), radius = r * 0.06f, center = coreCenter)
}

private fun DrawScope.drawVoidWanderer(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    violet: Color,
    crimson: Color,
    cyan: Color,
    pathA: Path
) {
    // Hooded Cloak Silhouette
    pathA.reset()
    pathA.moveTo(cx, cy - r * 0.72f)
    pathA.cubicTo(cx + r * 0.65f, cy - r * 0.42f, cx + r * 0.78f, cy + r * 0.25f, cx + r * 0.82f, cy + r * 0.88f)
    pathA.lineTo(cx - r * 0.82f, cy + r * 0.88f)
    pathA.cubicTo(cx - r * 0.78f, cy + r * 0.25f, cx - r * 0.65f, cy - r * 0.42f, cx, cy - r * 0.72f)
    pathA.close()
    drawPath(
        path = pathA,
        brush = Brush.verticalGradient(listOf(Color(0xFF171026), Color(0xFF05040A)))
    )
    drawPath(pathA, color = violet.copy(alpha = 0.55f), style = Stroke(width = r * 0.032f, join = StrokeJoin.Round))

    // Hood Inner Void Aperture
    val coreCenter = Offset(cx, cy - r * 0.06f)
    drawOval(
        color = Color(0xFF000000),
        topLeft = Offset(cx - r * 0.36f, cy - r * 0.42f),
        size = Size(r * 0.72f, r * 0.72f)
    )

    // Swirling Accretion Disk Ring around Black Hole Core
    rotate(degrees = cycle * 360f, pivot = coreCenter) {
        drawOval(
            brush = Brush.sweepGradient(
                listOf(violet, crimson, cyan, violet),
                center = coreCenter
            ),
            topLeft = Offset(coreCenter.x - r * 0.34f, coreCenter.y - r * 0.14f),
            size = Size(r * 0.68f, r * 0.28f),
            style = Stroke(width = r * 0.042f, cap = StrokeCap.Round)
        )
    }

    // Event Horizon Singularity Core
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.Black, Color.Black, violet.copy(alpha = 0.75f + 0.25f * pulse), Color.Transparent),
            center = coreCenter,
            radius = r * 0.26f
        ),
        radius = r * 0.26f,
        center = coreCenter
    )
    drawCircle(
        color = Color.Black,
        radius = r * 0.16f,
        center = coreCenter
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.78f),
        radius = r * 0.16f,
        center = coreCenter,
        style = Stroke(width = r * 0.016f)
    )

    // Accretion particles spiraling
    for (i in 0 until 5) {
        val a = -angleRad * 2f + i * (2f * PI.toFloat() / 5f)
        val px = coreCenter.x + cos(a) * r * 0.28f
        val py = coreCenter.y + sin(a) * r * 0.22f
        drawCircle(color = if (i % 2 == 0) cyan else crimson, radius = r * 0.025f, center = Offset(px, py))
    }
}

private fun DrawScope.drawBioLuminescentSynth(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    bioGreen: Color,
    neonPink: Color,
    cyan: Color,
    pathA: Path
) {
    // Sleek Synthetic Head & Neck Profile
    drawArc(
        brush = Brush.verticalGradient(listOf(Color(0xFF132229), Color(0xFF060B10))),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.74f, cy + r * 0.34f),
        size = Size(r * 1.48f, r * 1.0f)
    )
    drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFF172730), Color(0xFF0A1117))),
        topLeft = Offset(cx - r * 0.36f, cy - r * 0.48f),
        size = Size(r * 0.72f, r * 0.88f)
    )
    drawOval(
        color = bioGreen.copy(alpha = 0.55f),
        topLeft = Offset(cx - r * 0.36f, cy - r * 0.48f),
        size = Size(r * 0.72f, r * 0.88f),
        style = Stroke(width = r * 0.026f)
    )

    // Interwoven Organic DNA-Luminescent Fiber Strands across Synth Face & Neck
    val steps = 18
    for (strand in 0 until 2) {
        pathA.reset()
        val phaseShift = if (strand == 0) 0f else PI.toFloat()
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val y = cy - r * 0.42f + t * r * 1.02f
            val x = cx + sin(t * 2.6f * PI.toFloat() + angleRad * 2f + phaseShift) * r * 0.24f
            if (i == 0) pathA.moveTo(x, y) else pathA.lineTo(x, y)
        }
        drawPath(
            path = pathA,
            color = (if (strand == 0) bioGreen else neonPink).copy(alpha = 0.78f),
            style = Stroke(width = r * 0.030f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Synaptic Bio-Nodes pulsing along the strands
    for (n in 0 until 5) {
        val t = (n + 0.5f) / 5f
        val y = cy - r * 0.42f + t * r * 1.02f
        val x = cx + sin(t * 2.6f * PI.toFloat() + angleRad * 2f) * r * 0.24f
        drawCircle(
            color = cyan.copy(alpha = 0.45f + 0.45f * pulse),
            radius = r * 0.055f,
            center = Offset(x, y)
        )
        drawCircle(color = Color.White, radius = r * 0.022f, center = Offset(x, y))
    }

    // Bioluminescent Optical Lenses
    val eyeY = cy - r * 0.11f
    drawCircle(color = bioGreen, radius = r * 0.055f, center = Offset(cx - r * 0.16f, eyeY))
    drawCircle(color = neonPink, radius = r * 0.055f, center = Offset(cx + r * 0.16f, eyeY))
    // Moving energy pulse ring
    drawCircle(
        color = bioGreen.copy(alpha = (1f - cycle) * 0.45f),
        radius = r * (0.20f + 0.35f * cycle),
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.018f)
    )
}

private fun DrawScope.drawApexCyberHunter(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    crimson: Color,
    cyan: Color,
    gold: Color,
    pathA: Path,
    pathB: Path
) {
    // Dynamic Tactical Background Grid Lines
    val gridLines = 4
    val gridShift = (cycle * (r * 0.4f))
    for (i in -gridLines..gridLines) {
        val x = cx + i * r * 0.26f
        drawLine(
            color = cyan.copy(alpha = 0.16f),
            start = Offset(x, cy - r),
            end = Offset(x, cy + r),
            strokeWidth = r * 0.012f
        )
        val y = cy - r + ((i + gridLines) * r * 0.26f + gridShift) % (r * 2f)
        drawLine(
            color = crimson.copy(alpha = 0.14f),
            start = Offset(cx - r, y),
            end = Offset(cx + r, y),
            strokeWidth = r * 0.012f
        )
    }

    // Sharp-Featured Hunter Cowl & Jaw-Guard
    pathA.reset()
    pathA.moveTo(cx, cy - r * 0.60f)
    pathA.lineTo(cx + r * 0.44f, cy - r * 0.26f)
    pathA.lineTo(cx + r * 0.38f, cy + r * 0.38f)
    pathA.lineTo(cx, cy + r * 0.62f)
    pathA.lineTo(cx - r * 0.38f, cy + r * 0.38f)
    pathA.lineTo(cx - r * 0.44f, cy - r * 0.26f)
    pathA.close()
    drawPath(
        path = pathA,
        brush = Brush.verticalGradient(listOf(Color(0xFF1A1520), Color(0xFF09080E)))
    )
    drawPath(pathA, color = crimson.copy(alpha = 0.72f), style = Stroke(width = r * 0.032f, join = StrokeJoin.Miter))

    // Active Targeting HUD Visor
    pathB.reset()
    pathB.moveTo(cx - r * 0.40f, cy - r * 0.16f)
    pathB.lineTo(cx + r * 0.40f, cy - r * 0.16f)
    pathB.lineTo(cx + r * 0.30f, cy + r * 0.06f)
    pathB.lineTo(cx - r * 0.30f, cy + r * 0.06f)
    pathB.close()
    drawPath(pathB, brush = Brush.horizontalGradient(listOf(crimson.copy(alpha = 0.42f), cyan.copy(alpha = 0.28f))))
    drawPath(pathB, color = cyan.copy(alpha = 0.85f), style = Stroke(width = r * 0.025f))

    // Active Lock-On Crosshair Reticle inside Visor
    val targetX = cx + cos(angleRad * 2f) * r * 0.12f
    val targetY = cy - r * 0.05f
    drawCircle(
        color = crimson.copy(alpha = 0.75f + 0.25f * pulse),
        radius = r * 0.10f,
        center = Offset(targetX, targetY),
        style = Stroke(width = r * 0.024f)
    )
    drawCircle(color = gold, radius = r * 0.028f, center = Offset(targetX, targetY))
    drawLine(
        color = cyan.copy(alpha = 0.85f),
        start = Offset(targetX - r * 0.16f, targetY),
        end = Offset(targetX + r * 0.16f, targetY),
        strokeWidth = r * 0.018f
    )
    drawLine(
        color = cyan.copy(alpha = 0.85f),
        start = Offset(targetX, targetY - r * 0.14f),
        end = Offset(targetX, targetY + r * 0.14f),
        strokeWidth = r * 0.018f
    )
}

private fun DrawScope.drawAuraSovereign(
    cx: Float,
    cy: Float,
    r: Float,
    cycle: Float,
    angleRad: Float,
    pulse: Float,
    royalGold: Color,
    imperialBlue: Color,
    royalViolet: Color,
    pathA: Path,
    pathB: Path
) {
    // Imperial Halo Light behind Sovereign Head
    drawCircle(
        brush = Brush.radialGradient(
            listOf(royalGold.copy(alpha = 0.30f + 0.14f * pulse), imperialBlue.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(cx, cy - r * 0.12f),
            radius = r * 0.85f
        ),
        radius = r * 0.85f,
        center = Offset(cx, cy - r * 0.12f)
    )

    // Noble Frosted Glass Bust Silhouette
    drawArc(
        brush = Brush.verticalGradient(listOf(Color(0xFF19243B), Color(0xFF0A0F1D))),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(cx - r * 0.78f, cy + r * 0.34f),
        size = Size(r * 1.56f, r * 1.02f)
    )
    drawOval(
        brush = Brush.verticalGradient(listOf(imperialBlue.copy(alpha = 0.25f), Color(0xFF0C1424))),
        topLeft = Offset(cx - r * 0.33f, cy - r * 0.24f),
        size = Size(r * 0.66f, r * 0.76f)
    )
    drawOval(
        brush = Brush.linearGradient(listOf(royalGold.copy(alpha = 0.85f), imperialBlue)),
        topLeft = Offset(cx - r * 0.33f, cy - r * 0.24f),
        size = Size(r * 0.66f, r * 0.76f),
        style = Stroke(width = r * 0.028f)
    )

    // Levitating Geometric Glass-Shard Crown floating smoothly above forehead
    val crownHover = sin(angleRad * 2f) * r * 0.045f
    val crownY = cy - r * 0.36f + crownHover

    // Center Royal Shard
    pathA.reset()
    pathA.moveTo(cx, crownY - r * 0.38f)
    pathA.lineTo(cx + r * 0.13f, crownY - r * 0.08f)
    pathA.lineTo(cx, crownY + r * 0.06f)
    pathA.lineTo(cx - r * 0.13f, crownY - r * 0.08f)
    pathA.close()
    drawPath(pathA, brush = Brush.verticalGradient(listOf(Color.White, royalGold, imperialBlue)))
    drawPath(pathA, color = royalGold, style = Stroke(width = r * 0.024f, join = StrokeJoin.Miter))

    // Left Floating Shard
    pathB.reset()
    pathB.moveTo(cx - r * 0.24f, crownY - r * 0.26f)
    pathB.lineTo(cx - r * 0.14f, crownY + r * 0.02f)
    pathB.lineTo(cx - r * 0.32f, crownY + r * 0.06f)
    pathB.close()
    drawPath(pathB, brush = Brush.verticalGradient(listOf(imperialBlue, royalViolet.copy(alpha = 0.75f))))
    drawPath(pathB, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = r * 0.02f))

    // Right Floating Shard
    pathB.reset()
    pathB.moveTo(cx + r * 0.24f, crownY - r * 0.26f)
    pathB.lineTo(cx + r * 0.32f, crownY + r * 0.06f)
    pathB.lineTo(cx + r * 0.14f, crownY + r * 0.02f)
    pathB.close()
    drawPath(pathB, brush = Brush.verticalGradient(listOf(imperialBlue, royalViolet.copy(alpha = 0.75f))))
    drawPath(pathB, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = r * 0.02f))

    // Sovereign Luminance Eyes
    val shimmer = 0.72f + 0.28f * sin(cycle * 4f * PI.toFloat())
    drawOval(
        color = royalGold.copy(alpha = shimmer),
        topLeft = Offset(cx - r * 0.19f, cy + r * 0.04f),
        size = Size(r * 0.11f, r * 0.065f)
    )
    drawOval(
        color = imperialBlue.copy(alpha = shimmer),
        topLeft = Offset(cx + r * 0.08f, cy + r * 0.04f),
        size = Size(r * 0.11f, r * 0.065f)
    )
}
