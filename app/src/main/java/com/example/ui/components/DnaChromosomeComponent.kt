package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.aura
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private data class DnaBioParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float,
    val color: Color,
    val maxLife: Float,
    var life: Float = 0f,
    val isMeltDrop: Boolean = false
)

private data class HelixRungRender(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val zDepth: Float,
    val index: Int
)

private data class HelixNodeRender(
    val pos: Offset,
    val z: Float,
    val strand: Int, // 0 or 1
    val index: Int
)

/**
 * Reusable 60fps 3D-projected Rotating DNA & Chromosomes Canvas.
 * Bound strictly to consecutive Streak progression (0 -> 60 days):
 * - Day 0 (or on streak loss): Completely frozen in translucent ice crystals & stationary (angularVelocity = 0).
 * - Days 1..59: Ice progressively melts away with melt-water glints while spin velocity & glow awaken.
 * - Day 60+: 100% unfrozen with full spin velocity & luxury bioluminescent aura.
 */
@Composable
fun DnaChromosomeCanvas(
    streakDays: Int,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thawState = rememberMimicThawState(streakDays)
    val clampedDays = thawState.clampedDays
    val progression = thawState.thawProgress
    val isFrozen = thawState.isFullyFrozen

    // Smooth continuous ice encasement fading linearly from 1.0 at Day 0 down to 0.0 at Day 60
    val iceFactor = thawState.freezeFactor

    // Target angular velocity (rad/s): 0 when frozen, ramps smoothly along kineticFactor up to 6.4 rad/s at Day 60
    val targetAngularVelocity = 6.4f * thawState.kineticFactor
    val currentVelocity by rememberUpdatedState(targetAngularVelocity)
    val currentFrozen by rememberUpdatedState(thawState.kineticFactor <= 0.002f)

    // Continuous smooth rotation accumulator using vsync frame deltas (zero jumps when speed changes)
    var rotationRad by remember { mutableFloatStateOf(0.45f) }
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L && !currentFrozen) {
                    val dt = ((frameNanos - lastNanos).coerceIn(0L, 50_000_000L)) / 1_000_000_000f
                    rotationRad = (rotationRad + currentVelocity * dt) % (200f * PI.toFloat())
                }
                lastNanos = frameNanos
            }
        }
    }

    val pulsePhase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = false,
        tapImpulse = 0f,
        baseCyclesPerSecond = 0.72f
    )
    val corePulse = if (isFrozen) 0.85f else 0.72f + 0.43f * (0.5f + 0.5f * sin(pulsePhase * (2f * PI.toFloat())))

    // Bioluminescent particles & melting ice droplets
    val bioParticles = remember { mutableStateListOf<DnaBioParticle>() }
    val paletteColors = remember(clampedDays) {
        when {
            clampedDays == 0 -> listOf(
                Color(0xFFE0F7FA),
                Color(0xFF80DEEA),
                Color(0xFF00E5FF)
            )
            clampedDays < 30 -> listOf(
                Color(0xFF00F2FE),
                Color(0xFF38BDF8),
                Color(0xFF34D399),
                Color(0xFFB2EBF2)
            )
            clampedDays < 120 -> listOf(
                Color(0xFF00F2FE),
                Color(0xFFA855F7),
                Color(0xFFFF4FD8),
                Color(0xFF38BDF8)
            )
            clampedDays < 250 -> listOf(
                Color(0xFFFFD700),
                Color(0xFF00F2FE),
                Color(0xFFFF007F),
                Color(0xFFA855F7)
            )
            else -> listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFD700),
                Color(0xFF00F2FE),
                Color(0xFFFF007F),
                Color(0xFF9333EA)
            )
        }
    }

    LaunchedEffect(clampedDays, isCompact) {
        bioParticles.clear()
        if (clampedDays == 0) return@LaunchedEffect
        val maxCount = if (isCompact) 18 else (22 + (progression * 46).toInt())
        val random = Random(System.currentTimeMillis())
        while (isActive) {
            delay(20)
            if (bioParticles.size < maxCount) {
                val isMelt = iceFactor > 0.08f && random.nextFloat() < iceFactor * 0.65f
                val spreadX = if (isCompact) 54f else (75f + progression * 45f)
                bioParticles.add(
                    DnaBioParticle(
                        x = (random.nextFloat() - 0.5f) * spreadX,
                        y = if (isMelt) (random.nextFloat() - 0.2f) * 30f else (random.nextFloat() - 0.5f) * 65f,
                        vx = (random.nextFloat() - 0.5f) * (if (isMelt) 0.5f else (0.9f + progression * 1.8f)),
                        vy = if (isMelt) (1.2f + random.nextFloat() * 1.4f) else (-0.8f - random.nextFloat() * (1.2f + progression * 2.4f)),
                        radius = (if (isCompact) 1.6f else 2.4f) + random.nextFloat() * (2.2f + progression * 2.2f),
                        alpha = 0.9f,
                        color = if (isMelt) Color(0xFFB2EBF2) else paletteColors.random(random),
                        maxLife = 32f + random.nextFloat() * 26f,
                        isMeltDrop = isMelt
                    )
                )
            }
            val it = bioParticles.iterator()
            while (it.hasNext()) {
                val p = it.next()
                p.x += p.vx
                p.y += p.vy
                p.life += 1f
                p.alpha = (1f - p.life / p.maxLife).coerceIn(0f, 1f)
                if (p.life >= p.maxLife || p.alpha <= 0.04f) {
                    it.remove()
                }
            }
        }
    }

    val strandPrimary = when {
        progression < 0.35f -> lerp(Color(0xFF80DEEA), Color(0xFF00F2FE), progression / 0.35f)
        progression < 0.72f -> lerp(Color(0xFF00F2FE), Color(0xFFFFD700), (progression - 0.35f) / 0.37f)
        else -> lerp(Color(0xFFFFD700), Color(0xFF00F5FF), (progression - 0.72f) / 0.28f)
    }
    val strandSecondary = when {
        progression < 0.35f -> lerp(Color(0xFF38BDF8), Color(0xFFA855F7), progression / 0.35f)
        progression < 0.72f -> lerp(Color(0xFFA855F7), Color(0xFFFF007F), (progression - 0.35f) / 0.37f)
        else -> lerp(Color(0xFFFF007F), Color(0xFFFF2D95), (progression - 0.72f) / 0.28f)
    }
    val centromereCoreColor = when {
        progression < 0.45f -> lerp(Color(0xFFE0F7FA), Color(0xFFD946EF), progression / 0.45f)
        else -> lerp(Color(0xFFD946EF), Color(0xFFFFEA00), (progression - 0.45f) / 0.55f)
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val minDim = minOf(w, h)

        // 1. Deep Atmospheric & Centromere Radial Aura Glow
        val glowStrength = if (isFrozen) 0.28f else (0.38f + progression * 0.45f) * corePulse.coerceIn(0.8f, 1.15f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    centromereCoreColor.copy(alpha = glowStrength * 0.65f),
                    strandSecondary.copy(alpha = glowStrength * 0.42f),
                    strandPrimary.copy(alpha = glowStrength * 0.22f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = minDim * (0.48f + progression * 0.14f)
            ),
            center = Offset(cx, cy),
            radius = minDim * 0.62f
        )

        // 2. Luxury Orbital Gyroscope Halos (unlocked & intensified as streak grows)
        if (!isFrozen && progression > 0.04f) {
            val haloAlpha = (0.20f + progression * 0.65f).coerceIn(0f, 0.85f)
            val orbitRadius = minDim * (0.36f + progression * 0.08f)
            rotate(degrees = (rotationRad * 57.2958f) * 0.5f, pivot = Offset(cx, cy)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            strandPrimary.copy(alpha = haloAlpha),
                            Color.White.copy(alpha = haloAlpha * 0.85f),
                            strandSecondary.copy(alpha = haloAlpha),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy)
                    ),
                    startAngle = 0f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(cx - orbitRadius, cy - orbitRadius),
                    size = androidx.compose.ui.geometry.Size(orbitRadius * 2f, orbitRadius * 2f),
                    style = Stroke(width = (1.6f + progression * 1.4f).dp.toPx(), cap = StrokeCap.Round)
                )
            }
            if (progression >= 0.35f) {
                val innerOrbit = orbitRadius * 0.78f
                rotate(degrees = -(rotationRad * 57.2958f) * 0.8f, pivot = Offset(cx, cy)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFFFD700).copy(alpha = haloAlpha * 0.85f),
                                strandPrimary.copy(alpha = haloAlpha * 0.7f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy)
                        ),
                        startAngle = 90f,
                        sweepAngle = 175f,
                        useCenter = false,
                        topLeft = Offset(cx - innerOrbit, cy - innerOrbit),
                        size = androidx.compose.ui.geometry.Size(innerOrbit * 2f, innerOrbit * 2f),
                        style = Stroke(width = (1.2f + progression * 1.1f).dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 3. Bioluminescent Stardust & Melt Water Particles behind/around helix
        bioParticles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(cx + p.x, cy + p.y)
            )
        }

        // 4. Tilt the entire Chromosome-DNA Axis slightly (-14 deg) for iconic scientific aesthetics
        rotate(degrees = -14f, pivot = Offset(cx, cy)) {
            val helixHeight = minDim * (if (isCompact) 0.82f else 0.78f)
            val halfH = helixHeight / 2f
            val baseRadiusX = minDim * (if (isCompact) 0.19f else 0.18f)
            val steps = if (isCompact) 18 else 26
            val twists = 2.15f * PI.toFloat()

            // Subtle translucent Chromosome X-Chromatid envelope silhouette behind helix
            val xChromatidWidth = (if (isCompact) 3.5f else 5.5f).dp.toPx()
            val waistX = baseRadiusX * 0.34f
            val tipX = baseRadiusX * 1.16f
            val chromatidBrush = Brush.verticalGradient(
                listOf(
                    strandPrimary.copy(alpha = if (isFrozen) 0.22f else 0.26f + progression * 0.16f),
                    centromereCoreColor.copy(alpha = if (isFrozen) 0.28f else 0.36f + progression * 0.22f),
                    strandSecondary.copy(alpha = if (isFrozen) 0.22f else 0.26f + progression * 0.16f)
                )
            )
            // Left & right sister-chromatid arcs meeting at the Centromere
            val leftChromatid = Path().apply {
                moveTo(cx - tipX, cy - halfH * 0.96f)
                quadraticTo(cx - waistX * 0.6f, cy, cx - tipX, cy + halfH * 0.96f)
            }
            val rightChromatid = Path().apply {
                moveTo(cx + tipX, cy - halfH * 0.96f)
                quadraticTo(cx + waistX * 0.6f, cy, cx + tipX, cy + halfH * 0.96f)
            }
            drawPath(
                path = leftChromatid,
                brush = chromatidBrush,
                style = Stroke(width = xChromatidWidth, cap = StrokeCap.Round)
            )
            drawPath(
                path = rightChromatid,
                brush = chromatidBrush,
                style = Stroke(width = xChromatidWidth, cap = StrokeCap.Round)
            )

            // Precompute 3D helical points for Strand A & Strand B with Chromosome hourglass pinch at centromere
            val nodesA = ArrayList<HelixNodeRender>(steps + 1)
            val nodesB = ArrayList<HelixNodeRender>(steps + 1)
            val rungs = ArrayList<HelixRungRender>(steps + 1)

            for (i in 0..steps) {
                val t = i.toFloat() / steps.toFloat() // 0..1 top to bottom
                val yNorm = (t - 0.5f) * 2f // -1..+1
                val y = cy + yNorm * halfH

                // Chromosome waist pinch at centromere (yNorm == 0) flaring toward telomere tips
                val chromosomePinch = 0.42f + 0.72f * abs(yNorm).pow(1.25f)
                val localRadiusX = baseRadiusX * chromosomePinch

                val theta = rotationRad + t * twists
                val sinTheta = sin(theta)
                val cosTheta = cos(theta)

                // Mild perspective projection
                val perspA = 1f + 0.16f * cosTheta
                val perspB = 1f - 0.16f * cosTheta

                val xA = cx + localRadiusX * sinTheta * perspA
                val xB = cx - localRadiusX * sinTheta * perspB

                nodesA.add(HelixNodeRender(Offset(xA, y), cosTheta, 0, i))
                nodesB.add(HelixNodeRender(Offset(xB, y), -cosTheta, 1, i))

                // Draw base-pair rung on every 2nd step (or every step in high detail)
                if (i % 2 == 1) {
                    rungs.add(
                        HelixRungRender(
                            x1 = xA,
                            y1 = y,
                            x2 = xB,
                            y2 = y,
                            zDepth = abs(sinTheta) * 0.3f + (if (cosTheta > 0f) 0.1f else -0.1f),
                            index = i
                        )
                    )
                }
            }

            // Draw Base-Pair Rungs (A-T / G-C nucleotide bridges)
            val rungStrokePx = (if (isCompact) 1.8f else 2.5f).dp.toPx()
            rungs.forEach { rung ->
                val rungAlpha = if (isFrozen) 0.55f else (0.55f + 0.35f * progression).coerceIn(0.45f, 0.95f)
                val colA = if (rung.index % 4 == 1) strandPrimary else Color(0xFFFFD700).let { gold ->
                    if (isFrozen) Color(0xFFB2EBF2) else lerp(strandPrimary, gold, progression)
                }
                val colB = strandSecondary
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colA.copy(alpha = rungAlpha), colB.copy(alpha = rungAlpha)),
                        startX = minOf(rung.x1, rung.x2),
                        endX = maxOf(rung.x1, rung.x2).coerceAtLeast(minOf(rung.x1, rung.x2) + 1f)
                    ),
                    start = Offset(rung.x1, rung.y1),
                    end = Offset(rung.x2, rung.y2),
                    strokeWidth = rungStrokePx,
                    cap = StrokeCap.Round
                )
            }

            // Depth-sorted Strand Segments + Nucleotide Orbs so 3D front/back crossing looks genuinely volumetric!
            data class Segment3D(
                val p1: Offset,
                val p2: Offset,
                val zAvg: Float,
                val strand: Int
            )
            val segments = ArrayList<Segment3D>(steps * 2)
            for (i in 0 until steps) {
                segments.add(
                    Segment3D(
                        p1 = nodesA[i].pos,
                        p2 = nodesA[i + 1].pos,
                        zAvg = (nodesA[i].z + nodesA[i + 1].z) * 0.5f,
                        strand = 0
                    )
                )
                segments.add(
                    Segment3D(
                        p1 = nodesB[i].pos,
                        p2 = nodesB[i + 1].pos,
                        zAvg = (nodesB[i].z + nodesB[i + 1].z) * 0.5f,
                        strand = 1
                    )
                )
            }
            segments.sortBy { it.zAvg }

            val baseStrandW = (if (isCompact) 3.0f else 4.4f).dp.toPx()
            segments.forEach { seg ->
                val depthNorm = ((seg.zAvg + 1f) * 0.5f).coerceIn(0f, 1f) // 0 = back, 1 = front
                val baseColor = if (seg.strand == 0) strandPrimary else strandSecondary
                val segAlpha = (0.35f + 0.65f * depthNorm) * (if (isFrozen) 0.85f else 1f)
                val strokeW = baseStrandW * (0.65f + 0.55f * depthNorm)

                // Soft outer neon halo on foreground strands
                if (depthNorm > 0.45f && (!isFrozen || !isCompact)) {
                    drawLine(
                        color = baseColor.copy(alpha = segAlpha * (0.24f + progression * 0.25f)),
                        start = seg.p1,
                        end = seg.p2,
                        strokeWidth = strokeW * 2.2f,
                        cap = StrokeCap.Round
                    )
                }
                drawLine(
                    color = lerp(baseColor, Color.White, depthNorm * (0.28f + progression * 0.22f)).copy(alpha = segAlpha),
                    start = seg.p1,
                    end = seg.p2,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // Depth-sorted luminous nucleotide pearls at rung joints
            val allNodes = (nodesA + nodesB).filter { it.index % 2 == 1 }.sortedBy { it.z }
            val nodeBaseRadius = (if (isCompact) 2.5f else 3.8f).dp.toPx()
            allNodes.forEach { node ->
                val depthNorm = ((node.z + 1f) * 0.5f).coerceIn(0f, 1f)
                val baseCol = if (node.strand == 0) strandPrimary else strandSecondary
                val r = nodeBaseRadius * (0.68f + 0.52f * depthNorm)
                drawCircle(
                    color = baseCol.copy(alpha = 0.45f + 0.55f * depthNorm),
                    radius = r * 1.45f,
                    center = node.pos
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f + 0.45f * depthNorm),
                    radius = r * 0.62f,
                    center = node.pos
                )
            }

            // Glowing Telomere Tips (4 Chromosome Ends)
            val telomereEnds = listOf(nodesA.first().pos, nodesB.first().pos, nodesA.last().pos, nodesB.last().pos)
            val tipRadius = (if (isCompact) 3.4f else 5.2f).dp.toPx()
            telomereEnds.forEach { tip ->
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White, strandPrimary.copy(alpha = 0.85f), Color.Transparent),
                        center = tip,
                        radius = tipRadius * 2.1f
                    ),
                    radius = tipRadius * 2.1f,
                    center = tip
                )
            }

            // Central Centromere Energy Core
            val coreR = (if (isCompact) 7f else 11f).dp.toPx() * (0.9f + progression * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White,
                        centromereCoreColor.copy(alpha = 0.9f),
                        strandSecondary.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = coreR * 2.2f
                ),
                radius = coreR * 2.2f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = coreR * 0.48f,
                center = Offset(cx, cy)
            )
        }

        // 5. Progressive Crystalline Ice Encasement (100% solid at Day 0, melts away through Day 90)
        if (iceFactor > 0.02f) {
            val crystalRadius = minDim * (0.34f + 0.08f * iceFactor)
            val crystalAlpha = iceFactor.coerceIn(0f, 1f)

            // Translucent glacial hexagonal ice prism encasing the frozen DNA/Chromosomes
            val hexPath = Path().apply {
                for (k in 0 until 6) {
                    val a = (k * (PI / 3.0) + PI / 6.0).toFloat()
                    val rMul = if (k % 2 == 0) 1.04f else 0.92f
                    val px = cx + cos(a) * crystalRadius * 0.86f * rMul
                    val py = cy + sin(a) * crystalRadius * 1.15f * rMul
                    if (k == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = hexPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFE0F7FA).copy(alpha = 0.24f * crystalAlpha),
                        Color(0xFF80DEEA).copy(alpha = 0.14f * crystalAlpha),
                        Color(0xFF00E5FF).copy(alpha = 0.22f * crystalAlpha)
                    )
                )
            )
            drawPath(
                path = hexPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.85f * crystalAlpha),
                        Color(0xFF80DEEA).copy(alpha = 0.72f * crystalAlpha),
                        Color(0xFF00E5FF).copy(alpha = 0.82f * crystalAlpha)
                    )
                ),
                style = Stroke(
                    width = (if (isCompact) 1.4f else 2.2f).dp.toPx(),
                    join = StrokeJoin.Miter
                )
            )

            // Inner crystalline fracture facets & frost spikes
            for (shard in 0 until 6) {
                val a = (shard * (PI / 3.0) + 0.26).toFloat()
                val inner = crystalRadius * (0.32f + (1f - iceFactor) * 0.25f)
                val outer = crystalRadius * 0.96f
                drawLine(
                    color = Color(0xFFE0F7FA).copy(alpha = 0.62f * crystalAlpha),
                    start = Offset(cx + cos(a) * inner, cy + sin(a) * inner * 1.1f),
                    end = Offset(cx + cos(a) * outer, cy + sin(a) * outer * 1.1f),
                    strokeWidth = (if (isCompact) 1.0f else 1.5f).dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Full Interactive Home Screen Centerpiece for the Rotating DNA & Chromosomes Mimic.
 * Mirrors FlameParticleComponent's Streak progression (0..365) and 1-second Long-Press Vault access.
 */
@Composable
fun DnaChromosomeComponent(
    streakDays: Int,
    onLongPressToRoadmap: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val thawState = rememberMimicThawState(streakDays)
    val clampedDays = thawState.clampedDays
    val progression = thawState.thawProgress
    val isFrozen = thawState.isFullyFrozen

    val chargeProgress = remember { Animatable(0f) }
    var isPressing by remember { mutableStateOf(false) }

    val targetPrimaryTint = when {
        progression < 0.35f -> lerp(
            if (aura.isDark) Color(0xFF80DEEA) else Color(0xFF0284C7),
            if (aura.isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
            progression / 0.35f
        )
        progression < 0.72f -> lerp(
            if (aura.isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
            if (aura.isDark) Color(0xFFFFD700) else Color(0xFFB45309),
            (progression - 0.35f) / 0.37f
        )
        else -> lerp(
            if (aura.isDark) Color(0xFFFFD700) else Color(0xFFB45309),
            if (aura.isDark) Color(0xFF00F5FF) else Color(0xFF6D28D9),
            (progression - 0.72f) / 0.28f
        )
    }
    val primaryTint = lerp(
        if (aura.isDark) MimicGlacialIce else Color(0xFF0284C7),
        targetPrimaryTint,
        progression
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(210.dp)
                .testTag("dna_interactive_component")
                .pointerInput(Unit) {
                    var triggeredDuringPress = false
                    detectTapGestures(
                        onPress = {
                            isPressing = true
                            triggeredDuringPress = false
                            val job = scope.launch {
                                chargeProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                                )
                            }
                            tryAwaitRelease()
                            isPressing = false
                            if (!triggeredDuringPress && chargeProgress.value >= 0.98f) {
                                triggeredDuringPress = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressToRoadmap()
                            }
                            job.cancel()
                            chargeProgress.snapTo(0f)
                        },
                        onLongPress = {
                            if (!triggeredDuringPress) {
                                triggeredDuringPress = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressToRoadmap()
                            }
                        }
                    )
                }
        ) {
            // Circular Liquid Glass Cradle behind the Rotating DNA & Chromosomes
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (aura.isDark) {
                                when {
                                    isFrozen -> listOf(Color(0x3300E5FF), Color(0x1E081526), Color(0x8A070B14))
                                    clampedDays < 20 -> listOf(Color(0x2B00F2FE), Color(0x1E0D1B2A), Color(0x8A070B14))
                                    else -> listOf(Color(0x389333EA), Color(0x2400F2FE), Color(0x90070B14))
                                }
                            } else {
                                when {
                                    isFrozen -> listOf(Color(0x3838BDF8), Color(0xFFE0F2FE), Color(0xFFFFFFFF))
                                    else -> listOf(Color(0x286D28D9), Color(0xFFF5F3FF), Color(0xFFFFFFFF))
                                }
                            }
                        )
                    )
                    .border(
                        width = (1.1.dp + (progression * 1.4).dp),
                        brush = Brush.linearGradient(
                            if (isFrozen) {
                                listOf(Color(0xFFE0F7FA).copy(alpha = 0.75f), Color(0xFF00E5FF).copy(alpha = 0.45f))
                            } else {
                                listOf(primaryTint.copy(alpha = 0.85f), aura.accentViolet.copy(alpha = 0.65f))
                            }
                        ),
                        shape = CircleShape
                    )
            )

            // 1-Second Long-Press Charge-Up Ring (same Vault gesture as Flame)
            Canvas(modifier = Modifier.size(174.dp)) {
                if (chargeProgress.value > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            if (isFrozen) {
                                listOf(Color(0xFF00E5FF), Color(0xFFE0F7FA), Color(0xFF00E5FF))
                            } else {
                                listOf(Color(0xFF00F2FE), Color(0xFFA855F7), Color(0xFFFFD700), Color(0xFF00F2FE))
                            }
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * chargeProgress.value,
                        useCenter = false,
                        style = Stroke(width = (4.dp + (progression * 2).dp).toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 3D Rotating DNA & Chromosomes Canvas
            DnaChromosomeCanvas(
                streakDays = clampedDays,
                isCompact = false,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val thawBadge = formatMimicThawBadge(clampedDays, progression, isArabic)
        val statusText = if (isArabic) {
            when {
                clampedDays == 0 -> "حمض الـ DNA متجمد 0% • شاهد لإذابة الجليد"
                clampedDays < MIMIC_MAX_THAW_STREAK_DAYS -> "$thawBadge • سرعة متصاعدة 🧬"
                else -> "الذروة الأسطورية 100% • سرعة وتوهج أقصى 🧬✨ • $MIMIC_MAX_THAW_STREAK_DAYS / $MIMIC_MAX_THAW_STREAK_DAYS"
            }
        } else {
            when {
                clampedDays == 0 -> "DNA Frozen 0% • Watch to melt the ice"
                clampedDays < MIMIC_MAX_THAW_STREAK_DAYS -> "$thawBadge • Rising helix velocity 🧬"
                else -> "Mythic Peak 100% • Max Velocity & Glow 🧬✨ • $MIMIC_MAX_THAW_STREAK_DAYS / $MIMIC_MAX_THAW_STREAK_DAYS"
            }
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
                color = primaryTint,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isArabic) "اضغط مطولاً (1 ثانية) لفتح صفحة Vault" else "Hold 1s on DNA to open Vault",
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isFrozen && aura.isDark) Color(0xFFB2EBF2) else aura.accentCyan,
                fontSize = 10.sp,
                letterSpacing = 0.3.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
