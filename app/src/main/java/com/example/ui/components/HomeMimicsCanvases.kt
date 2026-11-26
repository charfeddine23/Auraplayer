package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.data.model.HomeMimicItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private val MimicOledBlack = Color(0xFF000000)
private val MimicNeonCyan = Color(0xFF00F2FE)
private val MimicNeonBlue = Color(0xFF3B82F6)
private val MimicNeonViolet = Color(0xFFA855F7)
private val MimicNeonPink = Color(0xFFFF2E93)
private val MimicNeonEmerald = Color(0xFF10B981)
private val MimicNeonGold = Color(0xFFFFD700)

/**
 * Shared 60 FPS Canvas preview router for all Mimic centerpieces.
 * Used on Home (full size) as well as Inventory, The Forge, and Store cards (compact).
 */
@Composable
fun HomeMimicLiveCanvasPreview(
    mimicItem: HomeMimicItem,
    streakDays: Int = 0,
    dailyProgress: Float = 0.5f,
    isPlayerActive: Boolean = false,
    tapImpulse: Float = 0f,
    tapNormalizedPoint: Offset = Offset.Zero,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    when (mimicItem) {
        HomeMimicItem.QUANTUM_REACTOR -> QuantumReactorCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.ACOUSTIC_SINGULARITY -> AcousticSingularityCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.HOLOGRAPHIC_CYBER_VINYL -> HolographicCyberVinylCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.LIQUID_TIME_CRYSTAL -> LiquidTimeCrystalHourglassCanvas(
            streakDays = streakDays,
            dailyProgress = dailyProgress,
            isPlayerActive = isPlayerActive,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.CYBERNETIC_NEURAL_WEB -> CyberneticNeuralWebCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            tapNormalizedPoint = tapNormalizedPoint,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.SUPERNOVA_NEBULA_CORE -> SupernovaNebulaCoreCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.QUANTUM_ORACLE_SPHERE -> QuantumOracleSphereCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.SUPERCONDUCTING_PLASMA_LATTICE -> SuperconductingPlasmaLatticeCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.FERROFLUIDIC_MAGNETIC_VORTEX -> FerrofluidicMagneticVortexCanvas(
            isPlayerActive = isPlayerActive,
            streakDays = streakDays,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.DIMENSIONAL_STARGATE_MONOLITH -> DimensionalStargateMonolithCanvas(
            streakDays = streakDays,
            isPlayerActive = isPlayerActive,
            tapImpulse = tapImpulse,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.DNA_CHROMOSOME -> DnaChromosomeCanvas(
            streakDays = streakDays,
            isCompact = isCompact,
            modifier = modifier
        )
        HomeMimicItem.STREAK_FLAME -> FlameMiniPreviewCanvas(
            streakDays = streakDays,
            modifier = modifier
        )
    }
}

@Composable
private fun FlameMiniPreviewCanvas(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val loopPhase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = false,
        tapImpulse = 0f,
        baseCyclesPerSecond = 0.62f
    )
    val pulse = 0.5f + 0.5f * sin(loopPhase * (2f * PI.toFloat()))
    val coreColor = androidx.compose.ui.graphics.lerp(
        MimicGlacialCyan,
        Color(0xFFFF6D00),
        thawState.thawProgress
    )
    val outerColor = androidx.compose.ui.graphics.lerp(
        Color(0xFF0284C7),
        MimicNeonGold,
        thawState.thawProgress
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.42f
        val amp = 0.25f + 0.75f * thawState.kineticFactor
        drawCircle(color = MimicOledBlack, radius = r * 1.1f, center = Offset(cx, cy))
        drawCircle(
            brush = Brush.radialGradient(
                listOf(coreColor.copy(alpha = 0.55f + 0.35f * pulse * amp), outerColor.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(cx, cy),
                radius = r * (0.95f + 0.15f * pulse * amp)
            ),
            radius = r * (0.95f + 0.15f * pulse * amp),
            center = Offset(cx, cy)
        )
        val tear = Path().apply {
            moveTo(cx, cy - r * (0.44f + 0.10f * thawState.thawProgress))
            cubicTo(cx + r * 0.36f, cy - r * 0.08f, cx + r * 0.34f, cy + r * 0.42f, cx, cy + r * 0.46f)
            cubicTo(cx - r * 0.34f, cy + r * 0.42f, cx - r * 0.36f, cy - r * 0.08f, cx, cy - r * (0.44f + 0.10f * thawState.thawProgress))
            close()
        }
        drawPath(
            path = tear,
            brush = Brush.verticalGradient(listOf(Color.White, coreColor, outerColor))
        )
        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = pulse,
            isCompact = true
        )
    }
}

/**
 * 1. Quantum Reactor Core
 * Central energy sphere with counter-rotating Liquid Glass outer rings using rememberInfiniteTransition,
 * and dynamic glow pulsing tied to player activity state.
 */
@Composable
fun QuantumReactorCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float = 0f,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val activeState by rememberUpdatedState(isPlayerActive)
    val kineticState by rememberUpdatedState(thawState.kineticFactor)
    val tapState by rememberUpdatedState(tapImpulse)

    var ringCw by remember { mutableFloatStateOf(0f) }
    var ringCcw by remember { mutableFloatStateOf(360f) }
    var outerSlow by remember { mutableFloatStateOf(0f) }
    var pulseClock by remember { mutableFloatStateOf(0f) }
    var smoothedActivity by remember { mutableFloatStateOf(if (isPlayerActive) 1.0f else if (streakDays > 0) 0.50f else 0.34f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val targetActivity = if (activeState) 1.0f else 0.38f + 0.32f * kineticState
                    smoothedActivity += (targetActivity - smoothedActivity) * (1f - exp(-dt * 4.5f))
                    val velFactor = (0.72f + smoothedActivity * 0.78f) * kineticState + tapState * 1.15f
                    ringCw = (ringCw + dt * 52f * velFactor) % 360f
                    ringCcw = (ringCcw - dt * 76f * velFactor + 3600f) % 360f
                    outerSlow = (outerSlow + dt * 32f * velFactor) % 360f
                    pulseClock = (pulseClock + dt * ((3.0f + 1.8f * smoothedActivity) * kineticState + tapState * 2.2f)) % (2f * PI.toFloat())
                }
                lastNanos = now
            }
        }
    }

    val pulsePhase = 0.5f + 0.5f * sin(pulseClock)
    val activityLevel = smoothedActivity

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseR = size.minDimension * 0.43f
        val boost = (activityLevel + tapImpulse * 0.75f).coerceIn(0.25f, 1.5f)
        val strokeBase = if (isCompact) 1.7.dp.toPx() else 2.6.dp.toPx()

        // OLED abyss sphere & frosted rim glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    MimicNeonCyan.copy(alpha = (0.38f * boost + 0.14f * pulsePhase).coerceIn(0f, 0.75f)),
                    MimicNeonViolet.copy(alpha = (0.20f * boost).coerceIn(0f, 0.45f)),
                    MimicOledBlack.copy(alpha = 0.92f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = baseR * 1.15f
            ),
            radius = baseR * 1.15f,
            center = Offset(cx, cy)
        )

        // Outer Liquid Glass containment ring 1 (clockwise)
        rotate(degrees = outerSlow, pivot = Offset(cx, cy)) {
            val r1 = baseR * 0.92f
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = r1,
                center = Offset(cx, cy),
                style = Stroke(width = strokeBase * 1.5f)
            )
            for (seg in 0 until 3) {
                val startA = seg * 120f + 10f
                drawArc(
                    brush = Brush.sweepGradient(listOf(MimicNeonCyan, MimicNeonViolet, MimicNeonCyan)),
                    startAngle = startA,
                    sweepAngle = 72f,
                    useCenter = false,
                    topLeft = Offset(cx - r1, cy - r1),
                    size = Size(r1 * 2f, r1 * 2f),
                    style = Stroke(width = strokeBase * 1.2f, cap = StrokeCap.Round)
                )
            }
        }

        // Middle counter-rotating Liquid Glass ring (counter-clockwise)
        rotate(degrees = ringCcw, pivot = Offset(cx, cy)) {
            val r2 = baseR * 0.73f
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = r2,
                center = Offset(cx, cy),
                style = Stroke(width = strokeBase * 1.9f)
            )
            for (seg in 0 until 4) {
                val a = seg * 90f
                drawArc(
                    color = if (seg % 2 == 0) MimicNeonCyan.copy(alpha = 0.92f) else MimicNeonPink.copy(alpha = 0.85f),
                    startAngle = a,
                    sweepAngle = 48f,
                    useCenter = false,
                    topLeft = Offset(cx - r2, cy - r2),
                    size = Size(r2 * 2f, r2 * 2f),
                    style = Stroke(width = strokeBase, cap = StrokeCap.Round)
                )
                val rad = (a + 24f) * (PI / 180.0)
                val nx = cx + cos(rad).toFloat() * r2
                val ny = cy + sin(rad).toFloat() * r2
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = if (isCompact) 1.6.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(nx, ny)
                )
            }
        }

        // Inner high-speed ring (clockwise)
        rotate(degrees = ringCw, pivot = Offset(cx, cy)) {
            val r3 = baseR * 0.54f
            drawArc(
                brush = Brush.sweepGradient(listOf(MimicNeonCyan, Color.White, MimicNeonViolet, Color.Transparent, MimicNeonCyan)),
                startAngle = 0f,
                sweepAngle = 295f,
                useCenter = false,
                topLeft = Offset(cx - r3, cy - r3),
                size = Size(r3 * 2f, r3 * 2f),
                style = Stroke(width = strokeBase * 0.95f, cap = StrokeCap.Round)
            )
        }

        // Central Reactor Energy Sphere
        val coreR = baseR * (0.31f + 0.055f * pulsePhase * boost + 0.06f * tapImpulse)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color.White,
                    MimicNeonCyan.copy(alpha = 0.95f),
                    MimicNeonBlue.copy(alpha = 0.72f),
                    MimicNeonViolet.copy(alpha = 0.32f),
                    MimicOledBlack
                ),
                center = Offset(cx, cy),
                radius = coreR * 1.45f
            ),
            radius = coreR * 1.45f,
            center = Offset(cx, cy)
        )

        // Liquid glass specular crescent on energy sphere
        drawArc(
            color = Color.White.copy(alpha = 0.52f),
            startAngle = 205f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(cx - coreR * 0.85f, cy - coreR * 0.85f),
            size = Size(coreR * 1.7f, coreR * 1.7f),
            style = Stroke(width = strokeBase * 0.75f, cap = StrokeCap.Round)
        )

        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = pulsePhase,
            isCompact = isCompact
        )
    }
}

private data class AccretionParticle(
    val orbitFraction: Float,
    val baseAngle: Float,
    val speed: Float,
    val sizeScale: Float,
    val paletteIdx: Int
)

/**
 * 2. Acoustic Singularity (Black Hole)
 * Pure OLED black core with swirling neon accretion disk particles & audio-reactive wave distortion.
 */
@Composable
fun AcousticSingularityCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float = 0f,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val particles = remember {
        val rng = Random(7013)
        List(44) { idx ->
            AccretionParticle(
                orbitFraction = 0.44f + rng.nextFloat() * 0.54f,
                baseAngle = rng.nextFloat() * 360f,
                speed = 0.65f + (1f - (idx % 10) / 10f) * 1.15f,
                sizeScale = 0.6f + rng.nextFloat() * 0.9f,
                paletteIdx = idx % 3
            )
        }
    }

    val activeState by rememberUpdatedState(isPlayerActive)
    val kineticState by rememberUpdatedState(thawState.kineticFactor)
    val tapState by rememberUpdatedState(tapImpulse)
    val particleAngles = remember { FloatArray(particles.size) { particles[it].baseAngle } }
    var wavePhase by remember { mutableFloatStateOf(0f) }
    var reactiveBoost by remember { mutableFloatStateOf(if (isPlayerActive) 1.0f else 0.38f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val targetBoost = if (activeState) 1.0f else 0.38f
                    reactiveBoost += (targetBoost - reactiveBoost) * (1f - exp(-dt * 4.5f))
                    val spinVel = (38f + 46f * reactiveBoost) * kineticState + tapState * 95f
                    for (i in particles.indices) {
                        particleAngles[i] = (particleAngles[i] + dt * spinVel * particles[i].speed) % 360f
                    }
                    wavePhase = (wavePhase + dt * ((2.2f + 1.8f * reactiveBoost) * kineticState + tapState * 2.5f)) % (2f * PI.toFloat())
                }
                lastNanos = now
            }
        }
    }

    val wavePath = remember { Path() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = size.minDimension * 0.44f
        val horizonR = maxR * 0.34f
        val distortionAmp = (if (isCompact) 1.6.dp.toPx() else 3.2.dp.toPx()) * (reactiveBoost + tapImpulse * 1.4f)

        // Reactive acoustic wave distortion perimeter
        wavePath.reset()
        val steps = 72
        for (i in 0..steps) {
            val theta = (i.toFloat() / steps) * (2f * PI).toFloat()
            val harmonic = sin(theta * 6f + wavePhase * 2f) * 0.6f +
                cos(theta * 10f - wavePhase * 3f) * 0.4f
            val r = maxR * 0.88f + harmonic * distortionAmp
            val x = cx + cos(theta) * r
            val y = cy + sin(theta) * r
            if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
        }
        wavePath.close()
        drawPath(
            path = wavePath,
            brush = Brush.sweepGradient(
                listOf(
                    MimicNeonViolet.copy(alpha = 0.25f * reactiveBoost),
                    MimicNeonCyan.copy(alpha = 0.38f * reactiveBoost),
                    MimicNeonPink.copy(alpha = 0.30f * reactiveBoost),
                    MimicNeonViolet.copy(alpha = 0.25f * reactiveBoost)
                )
            ),
            style = Stroke(width = if (isCompact) 1.1.dp.toPx() else 1.8.dp.toPx())
        )

        // Tilted accretion disk space
        rotate(degrees = -19f, pivot = Offset(cx, cy)) {
            val tiltY = 0.34f

            // Back-half accretion particles (behind event horizon)
            for (i in particles.indices) {
                val p = particles[i]
                val angleDeg = particleAngles[i]
                val rad = angleDeg * (PI / 180.0)
                if (sin(rad) < 0.0) {
                    val rx = maxR * p.orbitFraction
                    val px = cx + cos(rad).toFloat() * rx
                    val py = cy + sin(rad).toFloat() * rx * tiltY
                    val color = when (p.paletteIdx) {
                        0 -> MimicNeonCyan
                        1 -> MimicNeonViolet
                        else -> MimicNeonPink
                    }
                    drawCircle(
                        color = color.copy(alpha = 0.45f),
                        radius = (if (isCompact) 1.2f else 1.9f).dp.toPx() * p.sizeScale,
                        center = Offset(px, py)
                    )
                }
            }

            // Soft accretion disk ribbon rings
            for (band in 0..2) {
                val bandR = horizonR * (1.42f + band * 0.42f)
                val ovalH = bandR * 2f * tiltY
                drawOval(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MimicNeonViolet.copy(alpha = 0.38f),
                            MimicNeonCyan.copy(alpha = 0.55f),
                            MimicNeonPink.copy(alpha = 0.45f)
                        )
                    ),
                    topLeft = Offset(cx - bandR, cy - ovalH / 2f),
                    size = Size(bandR * 2f, ovalH),
                    style = Stroke(width = (if (isCompact) 1.3f else 2.2f).dp.toPx())
                )
            }
        }

        // Gravitational lensing top/bottom halo arcs around OLED black sphere
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to MimicOledBlack,
                0.56f to MimicOledBlack,
                0.70f to MimicNeonViolet.copy(alpha = 0.65f + tapImpulse * 0.25f),
                0.82f to MimicNeonCyan.copy(alpha = 0.32f),
                1.0f to Color.Transparent,
                center = Offset(cx, cy),
                radius = horizonR * 1.55f
            ),
            radius = horizonR * 1.55f,
            center = Offset(cx, cy)
        )

        // Pure OLED #000000 Black Hole Core
        drawCircle(color = MimicOledBlack, radius = horizonR, center = Offset(cx, cy))

        // Razor-sharp photon ring
        drawCircle(
            brush = Brush.sweepGradient(listOf(MimicNeonCyan, Color.White, MimicNeonPink, MimicNeonViolet, MimicNeonCyan)),
            radius = horizonR * 1.03f,
            center = Offset(cx, cy),
            style = Stroke(width = if (isCompact) 1.2.dp.toPx() else 2.0.dp.toPx())
        )

        // Front-half accretion disk particles crossing in front of black hole
        rotate(degrees = -19f, pivot = Offset(cx, cy)) {
            val tiltY = 0.34f
            for (i in particles.indices) {
                val p = particles[i]
                val angleDeg = particleAngles[i]
                val rad = angleDeg * (PI / 180.0)
                if (sin(rad) >= 0.0) {
                    val rx = maxR * p.orbitFraction
                    val px = cx + cos(rad).toFloat() * rx
                    val py = cy + sin(rad).toFloat() * rx * tiltY
                    val color = when (p.paletteIdx) {
                        0 -> MimicNeonCyan
                        1 -> MimicNeonViolet
                        else -> MimicNeonPink
                    }
                    drawCircle(
                        color = color.copy(alpha = 0.92f),
                        radius = (if (isCompact) 1.5f else 2.4f).dp.toPx() * p.sizeScale,
                        center = Offset(px, py)
                    )
                }
            }
        }

        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = wavePhase / (2f * PI.toFloat()),
            isCompact = isCompact
        )
    }
}

/**
 * 3. Holographic Cyber-Vinyl
 * 3D-angled floating futuristic vinyl record with holographic reflection shaders and smooth
 * velocity-integrated spin animation (idles slow, accelerates smoothly on media playback).
 */
@Composable
fun HolographicCyberVinylCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float = 0f,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val activeState by rememberUpdatedState(isPlayerActive)
    val kineticState by rememberUpdatedState(thawState.kineticFactor)
    val tapState by rememberUpdatedState(tapImpulse)
    var spinDegrees by remember { mutableFloatStateOf(0f) }
    var floatPhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var currentSpeed = 26f * kineticState
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val targetSpeed = (if (activeState) 176f else 28f) * kineticState + tapState * 220f
                    currentSpeed += (targetSpeed - currentSpeed) * (1f - exp(-dt * 4.2f))
                    spinDegrees = (spinDegrees + currentSpeed * dt) % 360f
                    floatPhase = (floatPhase + dt * ((1.85f + if (activeState) 0.45f else 0f) * kineticState + tapState * 1.2f)) % (2f * PI.toFloat())
                }
                lastNanos = now
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val hoverY = sin(floatPhase) * (if (isCompact) 2.2.dp.toPx() else 4.2.dp.toPx())
        val cy = size.height / 2f + hoverY
        val discR = size.minDimension * 0.43f
        val tiltScaleY = 0.62f

        // Frosted under-glow pedestal shadow
        drawOval(
            brush = Brush.radialGradient(
                listOf(
                    MimicNeonCyan.copy(alpha = if (isPlayerActive) 0.36f else 0.16f),
                    MimicNeonViolet.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(cx, size.height * 0.78f),
                radius = discR * 0.95f
            ),
            topLeft = Offset(cx - discR * 0.92f, size.height * 0.67f),
            size = Size(discR * 1.84f, discR * 0.44f)
        )

        // 3D angled floating vinyl platter
        withTransform({
            translate(left = cx, top = cy)
            rotate(degrees = -10f)
            scale(scaleX = 1f, scaleY = tiltScaleY)
        }) {
            val edgeDepth = if (isCompact) 5.dp.toPx() else 9.dp.toPx()

            // Lower 3D thickness extrusion rim
            drawCircle(
                brush = Brush.verticalGradient(listOf(Color(0xFF1F2937), Color(0xFF0A0F1C))),
                radius = discR,
                center = Offset(0f, edgeDepth)
            )

            // Main OLED #000000 vinyl disc
            drawCircle(color = MimicOledBlack, radius = discR, center = Offset.Zero)

            // Holographic reflection sweep shader spinning smoothly
            rotate(degrees = spinDegrees) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.0f to MimicNeonCyan.copy(alpha = 0.28f),
                        0.18f to Color.White.copy(alpha = 0.20f),
                        0.36f to MimicNeonViolet.copy(alpha = 0.28f),
                        0.52f to Color.Transparent,
                        0.68f to MimicNeonPink.copy(alpha = 0.26f),
                        0.85f to MimicNeonGold.copy(alpha = 0.22f),
                        1.0f to MimicNeonCyan.copy(alpha = 0.28f)
                    ),
                    radius = discR * 0.97f,
                    center = Offset.Zero
                )
            }

            // Concentric vinyl micro-grooves
            val grooveCount = if (isCompact) 6 else 10
            for (g in 1..grooveCount) {
                val gr = discR * (0.38f + (g.toFloat() / grooveCount) * 0.56f)
                drawCircle(
                    color = Color.White.copy(alpha = if (g % 3 == 0) 0.20f else 0.08f),
                    radius = gr,
                    center = Offset.Zero,
                    style = Stroke(width = if (isCompact) 0.7.dp.toPx() else 1.0.dp.toPx())
                )
            }

            // Outer liquid glass neon ring
            drawCircle(
                brush = Brush.linearGradient(listOf(MimicNeonCyan, MimicNeonViolet, MimicNeonPink)),
                radius = discR * 0.99f,
                center = Offset.Zero,
                style = Stroke(width = if (isCompact) 1.4.dp.toPx() else 2.2.dp.toPx())
            )

            // Center glowing holo label + rotating cue marker
            val labelR = discR * 0.33f
            drawCircle(
                brush = Brush.radialGradient(listOf(MimicNeonCyan, MimicNeonViolet, Color(0xFF090D16))),
                radius = labelR,
                center = Offset.Zero
            )
            rotate(degrees = spinDegrees) {
                drawLine(
                    color = MimicNeonGold,
                    start = Offset(labelR * 0.3f, 0f),
                    end = Offset(labelR * 0.92f, 0f),
                    strokeWidth = if (isCompact) 1.5.dp.toPx() else 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawCircle(color = MimicOledBlack, radius = discR * 0.085f, center = Offset.Zero)
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = discR * 0.085f,
                center = Offset.Zero,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Floating futuristic holographic laser tonearm
        val armStart = Offset(cx + discR * 0.76f, cy - discR * 0.55f)
        val armPivot = Offset(cx + discR * 0.62f, cy - discR * 0.08f)
        val stylusTip = Offset(cx + discR * 0.28f, cy + discR * 0.14f)
        drawLine(
            brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.75f), MimicNeonCyan)),
            start = armStart,
            end = armPivot,
            strokeWidth = if (isCompact) 1.8.dp.toPx() else 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            brush = Brush.linearGradient(listOf(MimicNeonCyan, MimicNeonGold)),
            start = armPivot,
            end = stylusTip,
            strokeWidth = if (isCompact) 1.4.dp.toPx() else 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = if (isPlayerActive) MimicNeonCyan else MimicNeonGold,
            radius = if (isCompact) 2.4.dp.toPx() else 3.8.dp.toPx(),
            center = stylusTip
        )

        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = floatPhase / (2f * PI.toFloat()),
            isCompact = isCompact
        )
    }
}

/**
 * 4. Liquid Time Crystal Hourglass
 * Futuristic geometric hourglass with glowing neon liquid & droplets flowing downward,
 * synced directly with streak and daily watch progress.
 */
@Composable
fun LiquidTimeCrystalHourglassCanvas(
    streakDays: Int,
    dailyProgress: Float,
    isPlayerActive: Boolean = false,
    tapImpulse: Float = 0f,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val activeState by rememberUpdatedState(isPlayerActive)
    val kineticState by rememberUpdatedState(thawState.kineticFactor)
    val tapState by rememberUpdatedState(tapImpulse)
    var dropPhase by remember { mutableFloatStateOf(0f) }
    var upperRipplePhase by remember { mutableFloatStateOf(0f) }
    var lowerRipplePhase by remember { mutableFloatStateOf(0f) }
    var smoothedFlowBoost by remember { mutableFloatStateOf(if (isPlayerActive) 1f else 0.4f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val targetFlow = (if (activeState) 1.0f else 0.42f) * kineticState + tapState * 1.15f
                    smoothedFlowBoost += (targetFlow - smoothedFlowBoost) * (1f - exp(-dt * 4.5f))
                    dropPhase = (dropPhase + dt * ((0.52f + 0.36f * smoothedFlowBoost) * kineticState + tapState * 0.4f)) % 1f
                    upperRipplePhase = (upperRipplePhase + dt * (2.6f * kineticState + 1.1f * smoothedFlowBoost)) % (2f * PI.toFloat())
                    lowerRipplePhase = (lowerRipplePhase + dt * (3.38f * kineticState + 1.45f * smoothedFlowBoost)) % (2f * PI.toFloat())
                }
                lastNanos = now
            }
        }
    }

    val syncedLowerRatio by animateFloatAsState(
        targetValue = (0.18f + dailyProgress.coerceIn(0f, 1f) * 0.68f +
            thawState.thawProgress * 0.14f).coerceIn(0.18f, 0.96f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "hourglass_fill"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val halfH = size.minDimension * 0.39f
        val halfW = size.minDimension * 0.27f
        val waistW = halfW * 0.16f
        val shoulderY = halfH * 0.32f
        val strokeW = if (isCompact) 1.5.dp.toPx() else 2.4.dp.toPx()

        val primaryNeon = if (dailyProgress >= 0.99f) MimicNeonEmerald else MimicNeonCyan
        val secondaryNeon = if (streakDays >= 30) MimicNeonGold else MimicNeonViolet

        // Geometric faceted diamond hourglass contour
        val upperBulb = Path().apply {
            moveTo(cx - halfW, cy - halfH)
            lineTo(cx + halfW, cy - halfH)
            lineTo(cx + halfW * 0.92f, cy - shoulderY)
            lineTo(cx + waistW, cy - 2.dp.toPx())
            lineTo(cx - waistW, cy - 2.dp.toPx())
            lineTo(cx - halfW * 0.92f, cy - shoulderY)
            close()
        }
        val lowerBulb = Path().apply {
            moveTo(cx - waistW, cy + 2.dp.toPx())
            lineTo(cx + waistW, cy + 2.dp.toPx())
            lineTo(cx + halfW * 0.92f, cy + shoulderY)
            lineTo(cx + halfW, cy + halfH)
            lineTo(cx - halfW, cy + halfH)
            lineTo(cx - halfW * 0.92f, cy + shoulderY)
            close()
        }

        // Ambient liquid crystal glow halo
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    primaryNeon.copy(alpha = if (isPlayerActive) 0.32f else 0.20f + tapImpulse * 0.22f),
                    secondaryNeon.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = halfH * 1.28f
            ),
            radius = halfH * 1.28f,
            center = Offset(cx, cy)
        )

        // OLED black glass interior
        drawPath(upperBulb, color = MimicOledBlack.copy(alpha = 0.94f))
        drawPath(lowerBulb, color = MimicOledBlack.copy(alpha = 0.94f))

        // Upper liquid reservoir (drains as daily progress grows)
        val upperRemain = (1f - syncedLowerRatio * 0.85f).coerceIn(0.10f, 0.85f)
        clipPath(upperBulb) {
            val topWaterY = cy - (halfH * upperRemain)
            val waterPath = Path().apply {
                moveTo(cx - halfW * 1.1f, cy)
                lineTo(cx - halfW * 1.1f, topWaterY)
                for (s in 0..16) {
                    val x = (cx - halfW * 1.1f) + (halfW * 2.2f) * (s / 16f)
                    val wy = topWaterY + sin(upperRipplePhase + s * 0.6f) * 2.dp.toPx()
                    lineTo(x, wy)
                }
                lineTo(cx + halfW * 1.1f, cy)
                close()
            }
            drawPath(
                path = waterPath,
                brush = Brush.verticalGradient(listOf(primaryNeon.copy(alpha = 0.92f), secondaryNeon.copy(alpha = 0.72f)))
            )
        }

        // Lower liquid reservoir (fills with daily watch + streak energy)
        clipPath(lowerBulb) {
            val fillH = halfH * syncedLowerRatio
            val poolTopY = cy + halfH - fillH
            val poolPath = Path().apply {
                moveTo(cx - halfW * 1.1f, cy + halfH + 4f)
                lineTo(cx - halfW * 1.1f, poolTopY)
                for (s in 0..16) {
                    val x = (cx - halfW * 1.1f) + (halfW * 2.2f) * (s / 16f)
                    val wy = poolTopY + cos(lowerRipplePhase + s * 0.55f) * 2.2.dp.toPx()
                    lineTo(x, wy)
                }
                lineTo(cx + halfW * 1.1f, cy + halfH + 4f)
                close()
            }
            drawPath(
                path = poolPath,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), primaryNeon, secondaryNeon))
            )
        }

        // Falling glowing neon droplets through crystal pinch
        val poolSurfaceY = cy + halfH * (1f - syncedLowerRatio)
        val dropCount = 4
        for (d in 0 until dropCount) {
            val t = (dropPhase + d.toFloat() / dropCount) % 1f
            val dy = (cy - 4.dp.toPx()) + t * ((poolSurfaceY - cy).coerceAtLeast(18f))
            val dr = (if (isCompact) 1.6.dp.toPx() else 2.6.dp.toPx()) * (1f - 0.25f * t)
            drawCircle(
                color = if (d % 2 == 0) Color.White else primaryNeon,
                radius = dr,
                center = Offset(cx, dy)
            )
        }

        // Frosted Liquid Glass faceted outlines
        val glassBorderBrush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.75f), primaryNeon, secondaryNeon))
        drawPath(upperBulb, brush = glassBorderBrush, style = Stroke(width = strokeW, join = StrokeJoin.Round))
        drawPath(lowerBulb, brush = glassBorderBrush, style = Stroke(width = strokeW, join = StrokeJoin.Round))

        // Top & bottom geometric metallic caps
        val capH = if (isCompact) 3.dp.toPx() else 4.5.dp.toPx()
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(secondaryNeon, Color.White.copy(alpha = 0.85f), primaryNeon)),
            topLeft = Offset(cx - halfW * 1.08f, cy - halfH - capH * 0.7f),
            size = Size(halfW * 2.16f, capH)
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(primaryNeon, Color.White.copy(alpha = 0.85f), secondaryNeon)),
            topLeft = Offset(cx - halfW * 1.08f, cy + halfH - capH * 0.3f),
            size = Size(halfW * 2.16f, capH)
        )

        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = dropPhase,
            isCompact = isCompact
        )
    }
}

private data class NeuralNode(
    val x: Float,
    val y: Float,
    val depth: Float,
    val phaseOffset: Float
)

private data class NeuralEdge(
    val a: Int,
    val b: Int,
    val speed: Float,
    val offset: Float
)

/**
 * 5. Cybernetic Neural Web
 * Interactive matrix of glowing digital nodes & synaptic lines pulsing with organic-cybernetic signals.
 */
@Composable
fun CyberneticNeuralWebCanvas(
    isPlayerActive: Boolean = false,
    streakDays: Int = 0,
    tapImpulse: Float = 0f,
    tapNormalizedPoint: Offset = Offset.Zero,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val (nodes, edges) = remember {
        val nodeList = listOf(
            NeuralNode(0.00f, 0.00f, 1.0f, 0.0f),
            NeuralNode(-0.48f, -0.34f, 0.85f, 0.7f),
            NeuralNode(0.46f, -0.36f, 0.90f, 1.4f),
            NeuralNode(-0.56f, 0.24f, 0.82f, 2.1f),
            NeuralNode(0.54f, 0.28f, 0.88f, 2.8f),
            NeuralNode(0.02f, -0.64f, 0.92f, 3.5f),
            NeuralNode(-0.04f, 0.62f, 0.92f, 4.2f),
            NeuralNode(-0.24f, -0.14f, 0.95f, 0.4f),
            NeuralNode(0.24f, 0.14f, 0.95f, 1.9f),
            NeuralNode(-0.22f, 0.42f, 0.78f, 2.6f),
            NeuralNode(0.28f, -0.18f, 0.80f, 3.9f),
            NeuralNode(-0.70f, -0.06f, 0.68f, 5.1f),
            NeuralNode(0.70f, -0.02f, 0.70f, 4.7f)
        )
        val edgeList = listOf(
            NeuralEdge(0, 7, 1.1f, 0.0f),
            NeuralEdge(0, 8, 1.3f, 0.25f),
            NeuralEdge(0, 9, 0.95f, 0.55f),
            NeuralEdge(0, 10, 1.2f, 0.75f),
            NeuralEdge(7, 1, 1.0f, 0.12f),
            NeuralEdge(7, 5, 1.15f, 0.40f),
            NeuralEdge(7, 3, 0.9f, 0.80f),
            NeuralEdge(10, 2, 1.25f, 0.18f),
            NeuralEdge(10, 5, 1.0f, 0.60f),
            NeuralEdge(8, 4, 1.3f, 0.32f),
            NeuralEdge(8, 6, 0.95f, 0.72f),
            NeuralEdge(9, 3, 1.1f, 0.48f),
            NeuralEdge(9, 6, 1.2f, 0.88f),
            NeuralEdge(1, 11, 0.85f, 0.15f),
            NeuralEdge(3, 11, 0.9f, 0.50f),
            NeuralEdge(2, 12, 0.85f, 0.35f),
            NeuralEdge(4, 12, 1.05f, 0.68f),
            NeuralEdge(1, 2, 0.75f, 0.22f),
            NeuralEdge(3, 4, 0.80f, 0.62f)
        )
        nodeList to edgeList
    }

    val activeState by rememberUpdatedState(isPlayerActive)
    val kineticState by rememberUpdatedState(thawState.kineticFactor)
    val tapState by rememberUpdatedState(tapImpulse)
    val edgeProgress = remember { FloatArray(edges.size) { edges[it].offset } }
    var driftPhaseX by remember { mutableFloatStateOf(0f) }
    var driftPhaseY by remember { mutableFloatStateOf(0f) }
    var smoothedSpeedBoost by remember { mutableFloatStateOf(if (isPlayerActive) 1.40f else 1.0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val targetBoost = (if (activeState) 1.45f else 0.95f) * kineticState + tapState * 1.10f
                    smoothedSpeedBoost += (targetBoost - smoothedSpeedBoost) * (1f - exp(-dt * 4.5f))
                    for (e in edges.indices) {
                        edgeProgress[e] = (edgeProgress[e] + dt * edges[e].speed * 0.42f * smoothedSpeedBoost) % 1f
                    }
                    driftPhaseX = (driftPhaseX + dt * ((1.18f + 0.30f * smoothedSpeedBoost) * kineticState + tapState * 1.2f)) % (2f * PI.toFloat())
                    driftPhaseY = (driftPhaseY + dt * ((1.06f + 0.26f * smoothedSpeedBoost) * kineticState + tapState * 1.1f)) % (2f * PI.toFloat())
                }
                lastNanos = now
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val spanR = size.minDimension * 0.43f
        val strokeBase = if (isCompact) 1.0.dp.toPx() else 1.55.dp.toPx()

        // OLED black frosted glass containment backdrop
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF07111F), MimicOledBlack, Color.Transparent),
                center = Offset(cx, cy),
                radius = spanR * 1.08f
            ),
            radius = spanR * 1.08f,
            center = Offset(cx, cy)
        )

        // Compute live smoothly drifting positions in continuous infinite Lissajous loop
        val driftScale = if (isCompact) 2.dp.toPx() else 4.dp.toPx()
        val liveOffsets = Array(nodes.size) { i ->
            val n = nodes[i]
            val dx = sin(driftPhaseX + n.phaseOffset) * driftScale
            val dy = cos(driftPhaseY + n.phaseOffset * 1.3f) * driftScale
            Offset(cx + n.x * spanR + dx, cy + n.y * spanR + dy)
        }

        // Draw synaptic axons + continuously looping traveling signal packets
        val tapX = cx + tapNormalizedPoint.x * spanR
        val tapY = cy + tapNormalizedPoint.y * spanR

        for (edgeIdx in edges.indices) {
            val edge = edges[edgeIdx]
            val pA = liveOffsets[edge.a]
            val pB = liveOffsets[edge.b]
            val mid = Offset((pA.x + pB.x) * 0.5f, (pA.y + pB.y) * 0.5f)
            val distFromTap = hypot(mid.x - tapX, mid.y - tapY) / spanR.coerceAtLeast(1f)
            val impulseGlow = (tapImpulse * (1.25f - distFromTap * 0.7f)).coerceIn(0f, 1f)

            drawLine(
                brush = Brush.linearGradient(
                    listOf(
                        MimicNeonCyan.copy(alpha = 0.24f + 0.45f * impulseGlow),
                        MimicNeonViolet.copy(alpha = 0.28f + 0.40f * impulseGlow)
                    ),
                    start = pA,
                    end = pB
                ),
                start = pA,
                end = pB,
                strokeWidth = strokeBase * (1f + 0.55f * impulseGlow),
                cap = StrokeCap.Round
            )

            // Traveling cybernetic action potential dot
            val t = edgeProgress[edgeIdx]
            val sx = pA.x + (pB.x - pA.x) * t
            val sy = pA.y + (pB.y - pA.y) * t
            val signalColor = when {
                impulseGlow > 0.3f -> MimicNeonPink
                edge.a % 2 == 0 -> MimicNeonCyan
                else -> MimicNeonEmerald
            }
            drawCircle(
                color = signalColor.copy(alpha = 0.95f),
                radius = (if (isCompact) 1.6f else 2.5f).dp.toPx() * (1f + 0.35f * impulseGlow),
                center = Offset(sx, sy)
            )
        }

        // Interactive shockwave ring on tap
        if (tapImpulse > 0.02f) {
            val ringR = spanR * (1.15f - tapImpulse) * 0.95f
            drawCircle(
                color = MimicNeonCyan.copy(alpha = tapImpulse * 0.65f),
                radius = ringR,
                center = Offset(tapX, tapY),
                style = Stroke(width = if (isCompact) 1.4.dp.toPx() else 2.2.dp.toPx())
            )
        }

        // Draw glowing digital nodes
        for (i in nodes.indices) {
            val pos = liveOffsets[i]
            val n = nodes[i]
            val nodeR = (if (isCompact) 2.8f else 4.5f).dp.toPx() * (if (i == 0) 1.45f else n.depth)
            val nodeColor = when {
                i == 0 -> Color.White
                i % 3 == 0 -> MimicNeonPink
                i % 2 == 0 -> MimicNeonCyan
                else -> MimicNeonViolet
            }
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(nodeColor.copy(alpha = 0.72f), Color.Transparent),
                    center = pos,
                    radius = nodeR * 2.5f
                ),
                radius = nodeR * 2.5f,
                center = pos
            )
            drawCircle(color = nodeColor, radius = nodeR, center = pos)
        }

        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = driftPhaseX / (2f * PI.toFloat()),
            isCompact = isCompact
        )
    }
}
