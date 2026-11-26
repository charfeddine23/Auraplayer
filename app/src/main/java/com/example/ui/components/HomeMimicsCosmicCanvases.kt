package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val CosmicOledBlack = Color(0xFF000000)
private val CosmicTurquoise = Color(0xFF00F2FE)
private val CosmicTeal = Color(0xFF14B8A6)
private val CosmicPurple = Color(0xFFA855F7)
private val CosmicDeepViolet = Color(0xFF6D28D9)
private val CosmicSkyBlue = Color(0xFF38BDF8)
private val CosmicElectricBlue = Color(0xFF3B82F6)
private val CosmicSilver = Color(0xFFE2E8F0)
private val CosmicObsidianDark = Color(0xFF070A10)
private val CosmicObsidianRim = Color(0xFF283548)
private val CosmicGold = Color(0xFFFFD700)

private const val TAU = (2.0 * PI).toFloat()

/**
 * 1. Supernova Nebula Core
 * Miniature stellar core surrounded by soft, breathing purple/turquoise cosmic nebula clouds
 * pulsating gently with ambient light in an unbroken 60 FPS harmonic loop.
 */
@Composable
fun SupernovaNebulaCoreCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val phase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = isPlayerActive,
        tapImpulse = tapImpulse,
        baseCyclesPerSecond = 0.072f
    )
    val activity by animateFloatAsState(
        targetValue = if (isPlayerActive) 1f else 0f,
        animationSpec = tween(durationMillis = 480),
        label = "supernova_activity"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.45f
        val k = thawState.kineticFactor
        val boost = ((activity * 0.55f * k) + tapImpulse * 0.85f).coerceIn(0f, 1.35f)

        val breath1 = (0.5f + 0.5f * sin(TAU * 2f * phase)) * k
        val breath2 = (0.5f + 0.5f * cos(TAU * 3f * phase)) * k

        // Deep OLED cosmic void
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF0A0618), CosmicOledBlack, Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 1.12f
            ),
            radius = r * 1.12f,
            center = Offset(cx, cy)
        )

        // Drifting purple & turquoise nebula clouds (integer harmonics = zero loop seam)
        val cloudSpecs = listOf(
            Triple(0f, 1f, CosmicPurple),
            Triple(2.094f, 2f, CosmicTurquoise),
            Triple(4.188f, 1f, CosmicDeepViolet),
            Triple(1.047f, 2f, CosmicTeal),
            Triple(3.1415f, 1f, CosmicSkyBlue)
        )
        for ((idx, spec) in cloudSpecs.withIndex()) {
            val baseAngle = spec.first
            val harmonic = spec.second
            val cloudColor = spec.third
            val angle = baseAngle + TAU * harmonic * phase * (if (idx % 2 == 0) 1f else -1f)
            val orbitDist = r * (0.24f + 0.08f * sin(TAU * 2f * phase + idx))
            val cloudCenter = Offset(cx + cos(angle) * orbitDist, cy + sin(angle) * orbitDist)
            val cloudRadius = r * (0.62f + 0.12f * breath1 + 0.09f * boost)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        cloudColor.copy(alpha = (0.28f + 0.14f * breath2 + 0.14f * boost).coerceIn(0f, 0.72f)),
                        cloudColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = cloudCenter,
                    radius = cloudRadius
                ),
                radius = cloudRadius,
                center = cloudCenter
            )
        }

        // Luminous nebula spiral gas ribbons
        rotate(degrees = phase * 360f, pivot = Offset(cx, cy)) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        CosmicTurquoise.copy(alpha = 0.45f + 0.25f * boost),
                        CosmicPurple.copy(alpha = 0.55f + 0.25f * boost),
                        Color.Transparent
                    )
                ),
                startAngle = 20f,
                sweepAngle = 230f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.68f, cy - r * 0.68f),
                size = Size(r * 1.36f, r * 1.36f),
                style = Stroke(width = if (isCompact) 3.dp.toPx() else 5.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        rotate(degrees = -phase * 720f + 45f, pivot = Offset(cx, cy)) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        CosmicPurple.copy(alpha = 0.40f + 0.25f * boost),
                        CosmicTeal.copy(alpha = 0.50f + 0.25f * boost),
                        Color.Transparent
                    )
                ),
                startAngle = 120f,
                sweepAngle = 190f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.48f, cy - r * 0.48f),
                size = Size(r * 0.96f, r * 0.96f),
                style = Stroke(width = if (isCompact) 2.2.dp.toPx() else 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Starlight dust particles in harmonic orbits
        val dustCount = if (isCompact) 12 else 18
        for (i in 0 until dustCount) {
            val theta = (i.toFloat() / dustCount) * TAU + TAU * (if (i % 2 == 0) 1f else -1f) * phase
            val radialMod = r * (0.36f + ((i * 37) % 100) / 100f * 0.48f)
            val px = cx + cos(theta) * radialMod
            val py = cy + sin(theta) * radialMod * (0.78f + 0.14f * cos(TAU * phase + i))
            val twinkle = 0.35f + 0.65f * (0.5f + 0.5f * sin(TAU * 4f * phase + i * 1.7f))
            val dotColor = if (i % 3 == 0) Color.White else if (i % 2 == 0) CosmicTurquoise else CosmicPurple
            drawCircle(
                color = dotColor.copy(alpha = twinkle * (0.65f + 0.30f * boost)),
                radius = (if (isCompact) 1.0f else 1.7f).dp.toPx() * (0.7f + 0.5f * twinkle),
                center = Offset(px, py)
            )
        }

        // Frosted liquid glass outer containment ring
        drawCircle(
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.34f),
                    CosmicTurquoise.copy(alpha = 0.42f),
                    CosmicPurple.copy(alpha = 0.42f),
                    Color.White.copy(alpha = 0.12f)
                )
            ),
            radius = r * 0.92f,
            center = Offset(cx, cy),
            style = Stroke(width = if (isCompact) 1.1.dp.toPx() else 1.8.dp.toPx())
        )

        // Miniature supernova stellar core & ambient lens crosshairs
        val coreR = r * (0.19f + 0.045f * breath1 + 0.06f * boost)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color.White,
                    CosmicTurquoise.copy(alpha = 0.90f),
                    CosmicPurple.copy(alpha = 0.52f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = coreR * 2.5f
            ),
            radius = coreR * 2.5f,
            center = Offset(cx, cy)
        )

        rotate(degrees = phase * 360f, pivot = Offset(cx, cy)) {
            val flareLen = coreR * (1.85f + 0.7f * boost)
            val flareStroke = if (isCompact) 1.2.dp.toPx() else 2.dp.toPx()
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.85f), Color.Transparent),
                    startX = cx - flareLen,
                    endX = cx + flareLen
                ),
                start = Offset(cx - flareLen, cy),
                end = Offset(cx + flareLen, cy),
                strokeWidth = flareStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, CosmicTurquoise.copy(alpha = 0.85f), Color.Transparent),
                    startY = cy - flareLen,
                    endY = cy + flareLen
                ),
                start = Offset(cx, cy - flareLen),
                end = Offset(cx, cy + flareLen),
                strokeWidth = flareStroke,
                cap = StrokeCap.Round
            )
        }

        drawCircle(color = Color.White, radius = coreR * 0.48f, center = Offset(cx, cy))
        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = phase,
            isCompact = isCompact
        )
    }
}

/**
 * 2. Quantum Oracle Sphere
 * Floating semi-transparent frosted glass orb enclosing a multi-faceted geometric rune-matrix
 * that rotates and shifts dynamically at 60 FPS.
 */
@Composable
fun QuantumOracleSphereCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val phase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = isPlayerActive,
        tapImpulse = tapImpulse,
        baseCyclesPerSecond = 0.085f
    )
    val activity by animateFloatAsState(
        targetValue = if (isPlayerActive) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "oracle_activity"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val k = thawState.kineticFactor
        val hoverOffset = if (isCompact) 0f else sin(TAU * 2f * phase) * 3.dp.toPx() * k
        val cx = size.width / 2f
        val cy = size.height / 2f + hoverOffset
        val orbR = size.minDimension * 0.43f
        val boost = (activity * 0.55f * k + tapImpulse * 0.85f).coerceIn(0f, 1.3f)
        val matrixShift = sin(TAU * 3f * phase) * 0.08f * k + tapImpulse * 0.14f

        // Soft aura behind the floating orb
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    CosmicSkyBlue.copy(alpha = 0.24f + 0.18f * boost),
                    CosmicPurple.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = orbR * 1.24f
            ),
            radius = orbR * 1.24f,
            center = Offset(cx, cy)
        )

        // Clipped translucent frosted glass orb interior
        val orbClip = Path().apply {
            addOval(Rect(cx - orbR, cy - orbR, cx + orbR, cy + orbR))
        }
        clipPath(orbClip) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF0A1428), Color(0xFF040710), CosmicOledBlack),
                    center = Offset(cx - orbR * 0.2f, cy - orbR * 0.2f),
                    radius = orbR * 1.15f
                ),
                radius = orbR,
                center = Offset(cx, cy)
            )

            // Rotating outer octagon rune ring
            rotate(degrees = phase * 360f, pivot = Offset(cx, cy)) {
                drawRegularPolygon(
                    center = Offset(cx, cy),
                    radius = orbR * (0.78f + matrixShift * 0.3f),
                    sides = 8,
                    color = CosmicSkyBlue.copy(alpha = 0.55f + 0.28f * boost),
                    strokeWidth = if (isCompact) 1.0.dp.toPx() else 1.5.dp.toPx()
                )
                // Inscribed cyber-rune tick marks
                val runeTicks = 16
                for (i in 0 until runeTicks) {
                    val a = (i.toFloat() / runeTicks) * TAU
                    val innerTick = orbR * (if (i % 2 == 0) 0.66f else 0.72f)
                    val outerTick = orbR * 0.84f
                    val pulseAlpha = 0.35f + 0.65f * (0.5f + 0.5f * sin(TAU * 4f * phase + i * 0.8f))
                    drawLine(
                        color = (if (i % 2 == 0) CosmicTurquoise else CosmicPurple).copy(alpha = pulseAlpha),
                        start = Offset(cx + cos(a) * innerTick, cy + sin(a) * innerTick),
                        end = Offset(cx + cos(a) * outerTick, cy + sin(a) * outerTick),
                        strokeWidth = if (isCompact) 1.0.dp.toPx() else 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Counter-rotating sacred dual-triangle matrix
            rotate(degrees = -phase * 360f, pivot = Offset(cx, cy)) {
                drawRegularPolygon(
                    center = Offset(cx, cy),
                    radius = orbR * (0.56f - matrixShift * 0.2f),
                    sides = 3,
                    color = CosmicPurple.copy(alpha = 0.78f),
                    strokeWidth = if (isCompact) 1.1.dp.toPx() else 1.7.dp.toPx()
                )
            }
            rotate(degrees = phase * 720f + 180f, pivot = Offset(cx, cy)) {
                drawRegularPolygon(
                    center = Offset(cx, cy),
                    radius = orbR * (0.56f + matrixShift * 0.25f),
                    sides = 3,
                    color = CosmicTurquoise.copy(alpha = 0.75f),
                    strokeWidth = if (isCompact) 1.1.dp.toPx() else 1.7.dp.toPx()
                )
            }

            // Inner shifting geometric diamond prism
            rotate(degrees = -phase * 720f, pivot = Offset(cx, cy)) {
                drawRegularPolygon(
                    center = Offset(cx, cy),
                    radius = orbR * (0.30f + 0.08f * boost),
                    sides = 4,
                    color = Color.White.copy(alpha = 0.85f),
                    strokeWidth = if (isCompact) 1.0.dp.toPx() else 1.5.dp.toPx()
                )
            }

            // Glowing central oracle eye
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White, CosmicTurquoise.copy(alpha = 0.85f), CosmicPurple.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = orbR * (0.24f + 0.08f * boost)
                ),
                radius = orbR * (0.24f + 0.08f * boost),
                center = Offset(cx, cy)
            )

            // Frosted upper-left glass specular crescent
            drawArc(
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.42f), Color.Transparent),
                    start = Offset(cx - orbR, cy - orbR),
                    end = Offset(cx, cy)
                ),
                startAngle = 195f,
                sweepAngle = 92f,
                useCenter = false,
                topLeft = Offset(cx - orbR * 0.86f, cy - orbR * 0.86f),
                size = Size(orbR * 1.72f, orbR * 1.72f),
                style = Stroke(width = if (isCompact) 2.2.dp.toPx() else 4.2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Frosted glass spherical outer rim
        drawCircle(
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.55f),
                    CosmicSkyBlue.copy(alpha = 0.65f),
                    CosmicPurple.copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.18f)
                )
            ),
            radius = orbR,
            center = Offset(cx, cy),
            style = Stroke(width = if (isCompact) 1.3.dp.toPx() else 2.2.dp.toPx())
        )
        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = phase,
            isCompact = isCompact
        )
    }
}

/**
 * 3. Superconducting Plasma Lattice
 * Interlocking dark obsidian rings enclosing a suspended, humming core of soft cyan cyber-lightning nodes.
 */
@Composable
fun SuperconductingPlasmaLatticeCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val phase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = isPlayerActive,
        tapImpulse = tapImpulse,
        baseCyclesPerSecond = 0.10f
    )
    val activity by animateFloatAsState(
        targetValue = if (isPlayerActive) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "lattice_activity"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.44f
        val k = thawState.kineticFactor
        val boost = (activity * 0.65f * k + tapImpulse * 0.9f).coerceIn(0f, 1.4f)
        val ringThickness = if (isCompact) 4.2.dp.toPx() else 7.8.dp.toPx()

        // OLED backdrop glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(CosmicTurquoise.copy(alpha = 0.22f + 0.18f * boost), CosmicOledBlack, Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 1.08f
            ),
            radius = r * 1.08f,
            center = Offset(cx, cy)
        )

        // 3 interlocking dark obsidian gyroscopic rings
        val ringAngles = listOf(phase * 360f, -phase * 360f + 60f, phase * 720f + 120f)
        val ringSquash = listOf(0.54f, 0.48f, 0.88f)
        for (idx in ringAngles.indices) {
            rotate(degrees = ringAngles[idx], pivot = Offset(cx, cy)) {
                scale(scaleX = 1f, scaleY = ringSquash[idx], pivot = Offset(cx, cy)) {
                    val ringR = r * (0.86f - idx * 0.08f)
                    // Dark obsidian ring body
                    drawCircle(
                        brush = Brush.linearGradient(
                            listOf(CosmicObsidianDark, CosmicObsidianRim, Color(0xFF0B111C), CosmicObsidianDark)
                        ),
                        radius = ringR,
                        center = Offset(cx, cy),
                        style = Stroke(width = ringThickness)
                    )
                    // Superconducting bevel cyan rim highlight
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                CosmicTurquoise.copy(alpha = 0.72f),
                                Color.White.copy(alpha = 0.38f),
                                CosmicElectricBlue.copy(alpha = 0.52f),
                                CosmicTurquoise.copy(alpha = 0.72f)
                            )
                        ),
                        radius = ringR,
                        center = Offset(cx, cy),
                        style = Stroke(width = if (isCompact) 0.9.dp.toPx() else 1.4.dp.toPx())
                    )
                }
            }
        }

        // Suspended cyber-lightning nodes & humming filaments
        val nodeCount = 6
        val nodePoints = Array(nodeCount) { i ->
            val a = (i.toFloat() / nodeCount) * TAU + TAU * phase
            val orbitR = r * (0.38f + 0.05f * sin(TAU * 3f * phase + i))
            Offset(cx + cos(a) * orbitR, cy + sin(a) * orbitR)
        }

        val arcStroke = if (isCompact) 1.0.dp.toPx() else 1.7.dp.toPx()
        for (i in 0 until nodeCount) {
            val node = nodePoints[i]
            val next = nodePoints[(i + 1) % nodeCount]

            // Humming filament from center core to node
            drawCyberLightningArc(
                start = Offset(cx, cy),
                end = node,
                phase = phase,
                seed = i + 1,
                amplitude = (if (isCompact) 2.4f else 4.5f).dp.toPx() * (0.65f + 0.7f * boost),
                color = CosmicTurquoise.copy(alpha = 0.78f + 0.2f * boost),
                strokeWidth = arcStroke
            )
            // Outer lattice cross-link arc
            drawCyberLightningArc(
                start = node,
                end = next,
                phase = phase,
                seed = i + 11,
                amplitude = (if (isCompact) 1.8f else 3.2f).dp.toPx() * (0.55f + 0.5f * boost),
                color = CosmicElectricBlue.copy(alpha = 0.58f + 0.25f * boost),
                strokeWidth = arcStroke * 0.85f
            )

            // Node halo + pinpoint
            val nodeR = (if (isCompact) 2.2f else 3.8f).dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(CosmicTurquoise.copy(alpha = 0.85f), Color.Transparent),
                    center = node,
                    radius = nodeR * 2.4f
                ),
                radius = nodeR * 2.4f,
                center = node
            )
            drawCircle(color = Color.White, radius = nodeR * 0.6f, center = node)
        }

        // Suspended humming central plasma nucleus
        val coreHum = 0.5f + 0.5f * sin(TAU * 6f * phase)
        val nucleusR = r * (0.16f + 0.04f * coreHum + 0.05f * boost)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, CosmicTurquoise.copy(alpha = 0.92f), CosmicElectricBlue.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(cx, cy),
                radius = nucleusR * 2.4f
            ),
            radius = nucleusR * 2.4f,
            center = Offset(cx, cy)
        )
        drawCircle(color = Color.White, radius = nucleusR * 0.55f, center = Offset(cx, cy))
        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = phase,
            isCompact = isCompact
        )
    }
}

/**
 * 4. Ferrofluidic Magnetic Vortex
 * Center ring of organic, morphing black liquid metal (ferrofluid) reacting fluidly to media rhythm.
 */
@Composable
fun FerrofluidicMagneticVortexCanvas(
    isPlayerActive: Boolean,
    streakDays: Int = 0,
    tapImpulse: Float,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val phase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = isPlayerActive,
        tapImpulse = tapImpulse,
        baseCyclesPerSecond = 0.115f
    )
    val activity by animateFloatAsState(
        targetValue = if (isPlayerActive) 1f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "ferrofluid_activity"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.44f
        val rhythmGain = ((0.18f + 0.82f * thawState.kineticFactor) * (0.35f + activity * 0.65f) + tapImpulse * 0.9f).coerceIn(0.12f, 1.45f)

        // Subtle cool steel-cyan magnetic induction rings in OLED space
        for (ringIdx in 1..3) {
            val waveProgress = (phase * 2f + ringIdx * 0.333f) % 1f
            val fieldR = r * (0.58f + waveProgress * 0.44f)
            drawCircle(
                color = CosmicTurquoise.copy(alpha = (1f - waveProgress) * (0.20f + 0.25f * activity + 0.3f * tapImpulse)),
                radius = fieldR,
                center = Offset(cx, cy),
                style = Stroke(width = if (isCompact) 0.9.dp.toPx() else 1.4.dp.toPx())
            )
        }

        // Build morphing outer & inner ferrofluid crown paths via Fourier radial harmonics
        val steps = if (isCompact) 72 else 108
        val outerPath = Path()
        val innerPath = Path()

        for (i in 0..steps) {
            val theta = (i.toFloat() / steps) * TAU
            // Multiple integer-harmonic magnetic spikes rotating cleanly in opposite directions
            val spike1 = sin(6f * theta - TAU * 2f * phase)
            val spike2 = cos(9f * theta + TAU * 3f * phase) * 0.6f
            val spike3 = sin(12f * theta - TAU * 4f * phase) * 0.35f
            val harmonicSpike = (spike1 + spike2 + spike3) * 0.52f

            val outerRad = r * (0.64f + 0.14f * rhythmGain * harmonicSpike + 0.05f * tapImpulse)
            val innerRad = r * (0.26f + 0.05f * rhythmGain * sin(5f * theta + TAU * 2f * phase))

            val ox = cx + cos(theta) * outerRad
            val oy = cy + sin(theta) * outerRad
            val ix = cx + cos(theta) * innerRad
            val iy = cy + sin(theta) * innerRad

            if (i == 0) {
                outerPath.moveTo(ox, oy)
                innerPath.moveTo(ix, iy)
            } else {
                outerPath.lineTo(ox, oy)
                innerPath.lineTo(ix, iy)
            }
        }
        outerPath.close()
        innerPath.close()

        // Glow rim behind black ferrofluid silhouette so pitch-black liquid pops off OLED #000000
        drawPath(
            path = outerPath,
            brush = Brush.sweepGradient(
                listOf(
                    CosmicTurquoise.copy(alpha = 0.55f),
                    CosmicSilver.copy(alpha = 0.65f),
                    CosmicElectricBlue.copy(alpha = 0.50f),
                    CosmicTurquoise.copy(alpha = 0.55f)
                )
            ),
            style = Stroke(
                width = if (isCompact) 3.4.dp.toPx() else 6.2.dp.toPx(),
                join = StrokeJoin.Round
            )
        )

        // Liquid metal black body
        drawPath(
            path = outerPath,
            brush = Brush.radialGradient(
                listOf(Color(0xFF1E293B), Color(0xFF090D16), CosmicOledBlack),
                center = Offset(cx - r * 0.14f, cy - r * 0.18f),
                radius = r * 0.95f
            )
        )

        // Glossy mercury/chrome edge highlight
        drawPath(
            path = outerPath,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.85f),
                    CosmicTurquoise.copy(alpha = 0.72f),
                    Color(0xFF475569).copy(alpha = 0.45f),
                    CosmicSilver.copy(alpha = 0.78f)
                )
            ),
            style = Stroke(
                width = if (isCompact) 1.2.dp.toPx() else 2.0.dp.toPx(),
                join = StrokeJoin.Round
            )
        )

        // Vortex singularity hollow eye inside the ferrofluid ring
        drawPath(path = innerPath, color = CosmicOledBlack)
        drawPath(
            path = innerPath,
            brush = Brush.sweepGradient(
                listOf(CosmicTurquoise, CosmicSilver.copy(alpha = 0.6f), CosmicElectricBlue, CosmicTurquoise)
            ),
            style = Stroke(width = if (isCompact) 1.1.dp.toPx() else 1.9.dp.toPx())
        )

        // Orbiting ferrofluid micro-beads pulled by magnetic flux
        val beadCount = 6
        for (b in 0 until beadCount) {
            val angle = (b.toFloat() / beadCount) * TAU + TAU * phase
            val radialOsc = r * (0.83f + 0.06f * sin(TAU * 3f * phase + b * 1.4f) * rhythmGain)
            val bx = cx + cos(angle) * radialOsc
            val by = cy + sin(angle) * radialOsc
            val beadR = (if (isCompact) 2.2f else 3.8f).dp.toPx()
            drawCircle(color = CosmicOledBlack, radius = beadR, center = Offset(bx, by))
            drawCircle(
                color = CosmicSilver.copy(alpha = 0.85f),
                radius = beadR,
                center = Offset(bx, by),
                style = Stroke(width = if (isCompact) 0.9.dp.toPx() else 1.3.dp.toPx())
            )
        }
        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = phase,
            isCompact = isCompact
        )
    }
}

/**
 * 5. Dimensional Stargate Monolith
 * Vertical floating frosted glass shards that part open to reveal an infinite digital horizon glow
 * tied to thaw progression (streakDays / 60f).
 */
@Composable
fun DimensionalStargateMonolithCanvas(
    streakDays: Int,
    isPlayerActive: Boolean,
    tapImpulse: Float,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val phase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = isPlayerActive,
        tapImpulse = tapImpulse,
        baseCyclesPerSecond = 0.095f
    )
    val activity by animateFloatAsState(
        targetValue = if (isPlayerActive) 1f else 0f,
        animationSpec = tween(durationMillis = 460),
        label = "stargate_activity"
    )

    val roadmapProgress = thawState.thawProgress
    val animatedAperture = (0.14f + roadmapProgress * 0.72f + tapImpulse * 0.14f + activity * 0.08f * thawState.kineticFactor).coerceIn(0.14f, 0.96f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.45f
        val boost = (activity * 0.5f * thawState.kineticFactor + tapImpulse * 0.85f).coerceIn(0f, 1.3f)

        // Portal aperture width driven smoothly by roadmapProgress + subtle hover breath
        val floatBreath = sin(TAU * 2f * phase) * (if (isCompact) 1.2.dp.toPx() else 2.4.dp.toPx())
        val gapHalfWidth = r * 0.46f * animatedAperture
        val shardHeight = r * 1.66f
        val topY = cy - shardHeight * 0.5f
        val bottomY = cy + shardHeight * 0.5f

        // Infinite digital horizon glow revealed inside the parted stargate
        val portalClip = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - gapHalfWidth - r * 0.08f,
                    top = topY + r * 0.06f,
                    right = cx + gapHalfWidth + r * 0.08f,
                    bottom = bottomY - r * 0.06f,
                    cornerRadius = CornerRadius(r * 0.18f, r * 0.18f)
                )
            )
        }

        clipPath(portalClip) {
            drawRect(color = CosmicOledBlack)

            // Horizon dawn glow (Cyan -> Gold at higher roadmap progress)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = 0.92f),
                        CosmicTurquoise.copy(alpha = 0.78f),
                        CosmicGold.copy(alpha = 0.25f + 0.45f * roadmapProgress),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r * (0.68f + 0.22f * roadmapProgress + 0.12f * boost)
                ),
                radius = r * 0.95f,
                center = Offset(cx, cy)
            )

            // Telescoping digital horizon grid lines
            val rayCount = 7
            for (i in -3..3) {
                val endX = cx + i * (r * 0.22f)
                drawLine(
                    brush = Brush.verticalGradient(
                        listOf(
                            CosmicTurquoise.copy(alpha = 0.55f),
                            CosmicGold.copy(alpha = 0.35f + 0.35f * roadmapProgress),
                            CosmicTurquoise.copy(alpha = 0.55f)
                        )
                    ),
                    start = Offset(cx + i * 2.dp.toPx(), cy),
                    end = Offset(endX, topY),
                    strokeWidth = if (isCompact) 0.7.dp.toPx() else 1.1.dp.toPx()
                )
                drawLine(
                    brush = Brush.verticalGradient(
                        listOf(
                            CosmicTurquoise.copy(alpha = 0.55f),
                            CosmicGold.copy(alpha = 0.35f + 0.35f * roadmapProgress),
                            CosmicTurquoise.copy(alpha = 0.55f)
                        )
                    ),
                    start = Offset(cx + i * 2.dp.toPx(), cy),
                    end = Offset(endX, bottomY),
                    strokeWidth = if (isCompact) 0.7.dp.toPx() else 1.1.dp.toPx()
                )
            }

            // Infinite moving perspective rings streaming outward continuously
            val ringLayers = 5
            for (k in 0 until ringLayers) {
                val p = ((phase * 3f + k.toFloat() / ringLayers) % 1f)
                val eased = p * p
                val frameW = (gapHalfWidth * 2f + r * 0.14f) * eased
                val frameH = shardHeight * 0.85f * eased
                drawOval(
                    color = (if (k % 2 == 0) CosmicTurquoise else CosmicGold).copy(alpha = (1f - p) * (0.75f + 0.2f * boost)),
                    topLeft = Offset(cx - frameW * 0.5f, cy - frameH * 0.5f),
                    size = Size(frameW.coerceAtLeast(1f), frameH.coerceAtLeast(1f)),
                    style = Stroke(width = if (isCompact) 0.9.dp.toPx() else 1.4.dp.toPx())
                )
            }
        }

        // Left & Right vertical floating frosted glass monolith shards
        val shardWidth = r * 0.44f
        val leftShard = Path().apply {
            moveTo(cx - gapHalfWidth - shardWidth * 0.72f, topY + r * 0.14f + floatBreath)
            lineTo(cx - gapHalfWidth, topY + floatBreath)
            lineTo(cx - gapHalfWidth, bottomY - floatBreath)
            lineTo(cx - gapHalfWidth - shardWidth * 0.85f, bottomY - r * 0.12f - floatBreath)
            lineTo(cx - gapHalfWidth - shardWidth, cy)
            close()
        }
        val rightShard = Path().apply {
            moveTo(cx + gapHalfWidth, topY - floatBreath)
            lineTo(cx + gapHalfWidth + shardWidth * 0.72f, topY + r * 0.14f - floatBreath)
            lineTo(cx + gapHalfWidth + shardWidth, cy)
            lineTo(cx + gapHalfWidth + shardWidth * 0.85f, bottomY - r * 0.12f + floatBreath)
            lineTo(cx + gapHalfWidth, bottomY + floatBreath)
            close()
        }

        for ((index, shardPath) in listOf(leftShard, rightShard).withIndex()) {
            drawPath(
                path = shardPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF142238).copy(alpha = 0.86f),
                        Color(0xFF0A111D).copy(alpha = 0.92f),
                        Color(0xFF1A2B44).copy(alpha = 0.78f)
                    )
                )
            )
            drawPath(
                path = shardPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.52f),
                        CosmicTurquoise.copy(alpha = 0.65f),
                        CosmicGold.copy(alpha = 0.35f + 0.45f * roadmapProgress)
                    )
                ),
                style = Stroke(
                    width = if (isCompact) 1.1.dp.toPx() else 1.8.dp.toPx(),
                    join = StrokeJoin.Round
                )
            )

            // Frosted rune progress notch on inner shard edge
            val edgeX = if (index == 0) cx - gapHalfWidth - r * 0.10f else cx + gapHalfWidth + r * 0.10f
            val notchFill = (shardHeight * 0.55f) * roadmapProgress.coerceAtLeast(0.12f)
            drawLine(
                brush = Brush.verticalGradient(listOf(CosmicTurquoise, CosmicGold)),
                start = Offset(edgeX, cy + notchFill * 0.5f),
                end = Offset(edgeX, cy - notchFill * 0.5f),
                strokeWidth = if (isCompact) 1.5.dp.toPx() else 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Flanking miniature floating glass splinters
        val splinterFloat = sin(TAU * 2f * phase + 1.1f) * (if (isCompact) 2.dp.toPx() else 4.dp.toPx())
        val leftSplinterX = cx - gapHalfWidth - shardWidth - r * 0.12f
        val rightSplinterX = cx + gapHalfWidth + shardWidth + r * 0.12f
        drawLine(
            color = CosmicTurquoise.copy(alpha = 0.65f),
            start = Offset(leftSplinterX, cy - r * 0.28f + splinterFloat),
            end = Offset(leftSplinterX, cy + r * 0.24f + splinterFloat),
            strokeWidth = if (isCompact) 1.4.dp.toPx() else 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = CosmicGold.copy(alpha = 0.65f),
            start = Offset(rightSplinterX, cy - r * 0.24f - splinterFloat),
            end = Offset(rightSplinterX, cy + r * 0.28f - splinterFloat),
            strokeWidth = if (isCompact) 1.4.dp.toPx() else 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawSmoothMimicIceOverlay(
            freezeFactor = thawState.freezeFactor,
            shimmerPhase = phase,
            isCompact = isCompact
        )
    }
}

private fun DrawScope.drawRegularPolygon(
    center: Offset,
    radius: Float,
    sides: Int,
    color: Color,
    strokeWidth: Float
) {
    if (sides < 3) return
    val path = Path()
    for (i in 0 until sides) {
        val angle = (i.toFloat() / sides) * TAU - (PI / 2.0).toFloat()
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawCyberLightningArc(
    start: Offset,
    end: Offset,
    phase: Float,
    seed: Int,
    amplitude: Float,
    color: Color,
    strokeWidth: Float
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
    val nx = -dy / len
    val ny = dx / len

    val segments = 5
    val path = Path().apply { moveTo(start.x, start.y) }
    for (s in 1 until segments) {
        val t = s.toFloat() / segments
        val envelope = sin(PI.toFloat() * t)
        val jitter = sin(TAU * 4f * phase + seed * 1.9f + s * 2.3f) * amplitude * envelope
        val px = start.x + dx * t + nx * jitter
        val py = start.y + dy * t + ny * jitter
        path.lineTo(px, py)
    }
    path.lineTo(end.x, end.y)
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
