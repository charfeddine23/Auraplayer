package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val MimicGlacialCyan = Color(0xFF00E5FF)
val MimicGlacialIce = Color(0xFF80DEEA)
val MimicGlacialWhite = Color(0xFFE0F7FA)

const val MIMIC_MAX_THAW_STREAK_DAYS = 60

@Immutable
data class MimicThawState(
    val clampedDays: Int,
    val thawProgress: Float,    // 0.0f at Day 0 -> 1.0f at Day 60
    val freezeFactor: Float,    // 1.0f at Day 0 -> 0.0f at Day 60
    val kineticFactor: Float,   // 0.0f at Day 0 (frozen still) -> 1.0f at Day 60 (full speed)
    val isFullyFrozen: Boolean,
    val isFullyThawed: Boolean
)

/**
 * Computes a silky-smooth 0..60 consecutive-strike Freeze & Thaw progression shared by every Mimic in Aura Pulse.
 * - Day 0 (or after losing streak/level): 100% frozen in glacial ice crystals & motionless (kineticFactor = 0.0f).
 * - Days 1..59: Smoothly melts ice crystal shells & frost shards while accelerating speed & glow (0.05f -> 0.984f).
 * - Day 60+: 100% unfrozen, zero ice encasement, full cosmic kinetic velocity (kineticFactor = 1.0f).
 * - Dropping streakDays smoothly re-freezes the Mimic and decelerates motion back down.
 */
@Composable
fun rememberMimicThawState(streakDays: Int): MimicThawState {
    val clamped = streakDays.coerceIn(0, MIMIC_MAX_THAW_STREAK_DAYS)
    val rawThaw = clamped / MIMIC_MAX_THAW_STREAK_DAYS.toFloat()
    val rawKinetic = if (clamped == 0) 0f else (0.05f + 0.95f * rawThaw)

    val animatedThaw by animateFloatAsState(
        targetValue = rawThaw,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "mimic_smooth_thaw_progress"
    )
    val animatedKinetic by animateFloatAsState(
        targetValue = rawKinetic,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "mimic_smooth_kinetic_factor"
    )

    val thaw = animatedThaw.coerceIn(0f, 1f)
    val freeze = (1f - thaw).coerceIn(0f, 1f)
    val kinetic = animatedKinetic.coerceIn(0f, 1f)

    return MimicThawState(
        clampedDays = clamped,
        thawProgress = thaw,
        freezeFactor = freeze,
        kineticFactor = kinetic,
        isFullyFrozen = clamped == 0,
        isFullyThawed = clamped >= MIMIC_MAX_THAW_STREAK_DAYS
    )
}

/**
 * Continuous 60 FPS phase integrator whose angular/loop velocity scales smoothly with [kineticFactor]
 * (0f = frozen stationary at Day 0, ramping smoothly to 1f = full speed at Day 60) without seam jumps.
 */
@Composable
fun rememberThawScaledLoopPhase(
    kineticFactor: Float,
    isPlayerActive: Boolean = false,
    tapImpulse: Float = 0f,
    baseCyclesPerSecond: Float = 0.09f
): Float {
    val kineticState by rememberUpdatedState(kineticFactor)
    val activeState by rememberUpdatedState(isPlayerActive)
    val tapState by rememberUpdatedState(tapImpulse)
    var phase by remember { mutableFloatStateOf(0.18f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val playbackBoost = if (activeState) 1.55f else 1.0f
                    val speedMultiplier = kineticState * playbackBoost + tapState * 0.55f
                    if (speedMultiplier > 0.0005f) {
                        phase = (phase + dt * baseCyclesPerSecond * speedMultiplier) % 1f
                    }
                }
                lastNanos = now
            }
        }
    }

    return phase
}

/**
 * Draws the smooth, crystalline glacial ice encasement over any Mimic Canvas.
 * As `freezeFactor` glides smoothly from 1.0 (Day 0) to 0.0 (Day 365):
 * - Ice mist alpha fades smoothly
 * - Translucent hexagonal & octagonal crystal facets recede & thin out
 * - Radial frost cracks retreat cleanly from core toward outer rim (melting from the inside out)
 */
fun DrawScope.drawSmoothMimicIceOverlay(
    freezeFactor: Float,
    shimmerPhase: Float = 0f,
    isCompact: Boolean = false
) {
    val factor = freezeFactor.coerceIn(0f, 1f)
    if (factor <= 0.008f) return

    val cx = size.width / 2f
    val cy = size.height / 2f
    val minDim = size.minDimension
    val thaw = 1f - factor
    val crystalR = minDim * (0.37f + 0.09f * factor)

    // 1. Glacial frost mist aura tinting the frozen core
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                MimicGlacialWhite.copy(alpha = 0.16f * factor),
                MimicGlacialIce.copy(alpha = 0.14f * factor),
                MimicGlacialCyan.copy(alpha = 0.09f * factor),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = crystalR * 1.18f
        ),
        radius = crystalR * 1.18f,
        center = Offset(cx, cy)
    )

    // 2. Outer translucent hexagonal ice crystal casing
    val hexPath = Path().apply {
        for (k in 0 until 6) {
            val angle = (k * (PI / 3.0) + PI / 6.0).toFloat()
            val facetMod = if (k % 2 == 0) 1.04f else 0.94f
            val px = cx + cos(angle) * crystalR * facetMod
            val py = cy + sin(angle) * crystalR * facetMod
            if (k == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    drawPath(
        path = hexPath,
        brush = Brush.linearGradient(
            listOf(
                MimicGlacialWhite.copy(alpha = 0.22f * factor),
                MimicGlacialIce.copy(alpha = 0.10f * factor),
                MimicGlacialCyan.copy(alpha = 0.20f * factor)
            ),
            start = Offset(cx - crystalR, cy - crystalR),
            end = Offset(cx + crystalR, cy + crystalR)
        )
    )

    drawPath(
        path = hexPath,
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.85f * factor),
                MimicGlacialIce.copy(alpha = 0.72f * factor),
                MimicGlacialCyan.copy(alpha = 0.80f * factor)
            )
        ),
        style = Stroke(
            width = ((if (isCompact) 1.25f else 2.1f) * (0.35f + 0.65f * factor)).dp.toPx(),
            join = StrokeJoin.Miter
        )
    )

    // 3. Secondary counter-angled frost prism facet (thin & shimmering)
    rotate(degrees = 15f + thaw * 12f, pivot = Offset(cx, cy)) {
        val innerPrismR = crystalR * (0.86f + 0.08f * thaw)
        val innerHex = Path().apply {
            for (k in 0 until 6) {
                val angle = (k * (PI / 3.0)).toFloat()
                val px = cx + cos(angle) * innerPrismR
                val py = cy + sin(angle) * innerPrismR
                if (k == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(
            path = innerHex,
            color = MimicGlacialWhite.copy(alpha = 0.36f * factor * factor),
            style = Stroke(
                width = (if (isCompact) 0.7f else 1.15f).dp.toPx(),
                join = StrokeJoin.Miter
            )
        )
    }

    // 4. Retreating crystalline frost fractures & ice needles (melt from center outward as thaw 0 -> 1)
    val shardCount = if (isCompact) 6 else 12
    val innerTip = crystalR * (0.20f + 0.72f * thaw)
    val outerTip = crystalR * 0.99f
    if (outerTip > innerTip + 1f) {
        for (i in 0 until shardCount) {
            val angle = (i.toFloat() / shardCount) * (2f * PI.toFloat()) + 0.22f
            val isMajor = i % 2 == 0
            val startR = if (isMajor) innerTip else (innerTip + (outerTip - innerTip) * 0.35f)
            val sx = cx + cos(angle) * startR
            val sy = cy + sin(angle) * startR
            val ex = cx + cos(angle) * outerTip
            val ey = cy + sin(angle) * outerTip
            val glint = 0.75f + 0.25f * sin(shimmerPhase * (2f * PI.toFloat()) + i)
            drawLine(
                brush = Brush.linearGradient(
                    listOf(
                        Color.Transparent,
                        MimicGlacialWhite.copy(alpha = 0.78f * factor * glint)
                    ),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey)
                ),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = (if (isCompact) 0.85f else 1.45f).dp.toPx() * (if (isMajor) 1f else 0.7f),
                cap = StrokeCap.Round
            )
        }
    }

    // 5. Delicate frost sparkles around crystal vertices
    val sparkleCount = 6
    for (s in 0 until sparkleCount) {
        val a = (s * (PI / 3.0) + PI / 6.0).toFloat()
        val px = cx + cos(a) * crystalR * (if (s % 2 == 0) 1.04f else 0.94f)
        val py = cy + sin(a) * crystalR * (if (s % 2 == 0) 1.04f else 0.94f)
        val twinkle = 0.55f + 0.45f * cos(shimmerPhase * (2f * PI.toFloat()) + s * 1.1f)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f * factor * twinkle),
            radius = (if (isCompact) 1.2f else 2.0f).dp.toPx() * (0.4f + 0.6f * factor),
            center = Offset(px, py)
        )
    }
}

fun formatMimicThawBadge(
    streakDays: Int,
    thawProgress: Float,
    isArabic: Boolean
): String {
    val clamped = streakDays.coerceIn(0, MIMIC_MAX_THAW_STREAK_DAYS)
    val pct = (thawProgress.coerceIn(0f, 1f) * 100f).roundToInt()
    return when {
        clamped == 0 -> if (isArabic) "❄️ متجمد 0% • 0 / $MIMIC_MAX_THAW_STREAK_DAYS" else "❄️ Frozen 0% • 0 / $MIMIC_MAX_THAW_STREAK_DAYS"
        clamped < MIMIC_MAX_THAW_STREAK_DAYS -> if (isArabic) "🧊 إزالة التجمد ${pct.coerceAtLeast(1)}% • $clamped / $MIMIC_MAX_THAW_STREAK_DAYS" else "🧊 Thawing ${pct.coerceAtLeast(1)}% • $clamped / $MIMIC_MAX_THAW_STREAK_DAYS"
        else -> if (isArabic) "✨ ذوبان كامل 100% • $MIMIC_MAX_THAW_STREAK_DAYS / $MIMIC_MAX_THAW_STREAK_DAYS" else "✨ Unfrozen 100% • $MIMIC_MAX_THAW_STREAK_DAYS / $MIMIC_MAX_THAW_STREAK_DAYS"
    }
}
