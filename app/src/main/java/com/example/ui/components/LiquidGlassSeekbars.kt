package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerSeekbarStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

// Unique Seekbar palette constants (avoid package-level collision with ExoVideoPlayer.kt)
private val SeekbarOledBlack = Color(0xFF000000)
private val SeekbarGlassRailDark = Color(0xCC060A14)
private val SeekbarGlassBorder = Color(0x42FFFFFF)
private val SeekbarNeonCyan = Color(0xFF00F2FE)
private val SeekbarNeonBlue = Color(0xFF3B82F6)
private val SeekbarNeonViolet = Color(0xFFA855F7)
private val SeekbarNeonPink = Color(0xFFFF2E93)
private val SeekbarNeonEmerald = Color(0xFF10B981)
private val SeekbarNeonMatrixBright = Color(0xFF34D399)
private val SeekbarNeonGold = Color(0xFFFFD700)
private val SeekbarLockedSlate = Color(0xFF64748B)

private data class SeekbarDeterministicParticle(
    val seedA: Float,
    val seedB: Float,
    val seedC: Float,
    val polarity: Int
)

private val SEEKBAR_PARTICLES_POOL: List<SeekbarDeterministicParticle> = List(28) { idx ->
    val a = ((idx * 37 + 13) % 100) / 100f
    val b = ((idx * 53 + 29) % 100) / 100f
    val c = ((idx * 71 + 41) % 100) / 100f
    SeekbarDeterministicParticle(
        seedA = a,
        seedB = b,
        seedC = c,
        polarity = if (idx % 2 == 0) 1 else -1
    )
}

/**
 * Unified dispatcher for the 5 Next-Gen Liquid Glass Dark Mode Seekbars in ExoVideoPlayer.
 */
@Composable
fun LiquidGlassSeekbarDispatcher(
    style: PlayerSeekbarStyle,
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (style) {
        PlayerSeekbarStyle.PLASMA_KATANA -> PlasmaKatanaSeekbar(
            progressRatioProvider = progressRatioProvider,
            durationMs = durationMs,
            isPlaying = isPlaying,
            isDragging = isDragging,
            isAntiCheatLocked = isAntiCheatLocked,
            bookmarks = bookmarks,
            thicknessScale = thicknessScale,
            glowScale = glowScale,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish,
            modifier = modifier
        )
        PlayerSeekbarStyle.QUANTUM_COLLIDER -> QuantumColliderSeekbar(
            progressRatioProvider = progressRatioProvider,
            durationMs = durationMs,
            isPlaying = isPlaying,
            isDragging = isDragging,
            isAntiCheatLocked = isAntiCheatLocked,
            bookmarks = bookmarks,
            thicknessScale = thicknessScale,
            glowScale = glowScale,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish,
            modifier = modifier
        )
        PlayerSeekbarStyle.DOUBLE_HELIX_GENE -> DoubleHelixGeneSeekbar(
            progressRatioProvider = progressRatioProvider,
            durationMs = durationMs,
            isPlaying = isPlaying,
            isDragging = isDragging,
            isAntiCheatLocked = isAntiCheatLocked,
            bookmarks = bookmarks,
            thicknessScale = thicknessScale,
            glowScale = glowScale,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish,
            modifier = modifier
        )
        PlayerSeekbarStyle.EVENT_HORIZON -> EventHorizonSeekbar(
            progressRatioProvider = progressRatioProvider,
            durationMs = durationMs,
            isPlaying = isPlaying,
            isDragging = isDragging,
            isAntiCheatLocked = isAntiCheatLocked,
            bookmarks = bookmarks,
            thicknessScale = thicknessScale,
            glowScale = glowScale,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish,
            modifier = modifier
        )
        PlayerSeekbarStyle.CYBER_MATRIX_CODE -> CyberMatrixCodeSeekbar(
            progressRatioProvider = progressRatioProvider,
            durationMs = durationMs,
            isPlaying = isPlaying,
            isDragging = isDragging,
            isAntiCheatLocked = isAntiCheatLocked,
            bookmarks = bookmarks,
            thicknessScale = thicknessScale,
            glowScale = glowScale,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish,
            modifier = modifier
        )
        else -> PlasmaKatanaSeekbar(
            progressRatioProvider = progressRatioProvider,
            durationMs = durationMs,
            isPlaying = isPlaying,
            isDragging = isDragging,
            isAntiCheatLocked = isAntiCheatLocked,
            bookmarks = bookmarks,
            thicknessScale = thicknessScale,
            glowScale = glowScale,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish,
            modifier = modifier
        )
    }
}

/**
 * Shared modifier providing hardware offscreen compositing plus zero-slop single-pass tap & drag scrubbing.
 */
private fun Modifier.liquidGlassScrubGesture(
    durationMs: Long,
    isAntiCheatLocked: Boolean,
    horizontalPadDp: Float = 10f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit
): Modifier = this
    .graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .pointerInput(durationMs, isAntiCheatLocked) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()
            if (isAntiCheatLocked) {
                onLockedAttempt()
                return@awaitEachGesture
            }
            val padPx = horizontalPadDp.dp.toPx()
            val usableW = (size.width - padPx * 2f).coerceAtLeast(1f)
            val initialRatio = ((down.position.x - padPx) / usableW).coerceIn(0f, 1f)
            onSeekStart(initialRatio)

            var pointerId = down.id
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId }
                    ?: event.changes.firstOrNull()
                    ?: break
                pointerId = change.id
                if (!change.pressed) {
                    change.consume()
                    break
                }
                change.consume()
                val dragRatio = ((change.position.x - padPx) / usableW).coerceIn(0f, 1f)
                onSeekUpdate(dragRatio)
            }
            onSeekFinish()
        }
    }

private fun DrawScope.drawSeekbarBookmarkDiamonds(
    bookmarks: List<Long>,
    durationMs: Long,
    padPx: Float,
    trackWidth: Float,
    centerY: Float,
    thicknessScale: Float,
    glowScale: Float
) {
    if (durationMs <= 0L || bookmarks.isEmpty()) return
    val diamondHalf = 3.2.dp.toPx() * thicknessScale
    bookmarks.forEach { bmMs ->
        val bmRatio = (bmMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        val bx = padPx + bmRatio * trackWidth
        drawCircle(
            color = SeekbarNeonGold.copy(alpha = (0.34f * glowScale).coerceIn(0.12f, 0.7f)),
            radius = 5.6.dp.toPx() * thicknessScale,
            center = Offset(bx, centerY)
        )
        val diamond = Path().apply {
            moveTo(bx, centerY - diamondHalf)
            lineTo(bx + diamondHalf, centerY)
            lineTo(bx, centerY + diamondHalf)
            lineTo(bx - diamondHalf, centerY)
            close()
        }
        drawPath(diamond, color = SeekbarNeonGold)
    }
}

// ---------------------------------------------------------------------------
// 1. ⚡ PLASMA KATANA SEEKBAR
// Glowing liquid-glass neon blade across the timeline with spark bursts and
// dynamic scrub light motion trail.
// ---------------------------------------------------------------------------
@Composable
fun PlasmaKatanaSeekbar(
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestPlaying by rememberUpdatedState(isPlaying)
    val latestDragging by rememberUpdatedState(isDragging)
    val latestRatioProvider by rememberUpdatedState(progressRatioProvider)

    var shimmerPhase by remember { mutableFloatStateOf(0f) }
    var pulseClock by remember { mutableFloatStateOf(0f) }
    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }
    var dragBoost by remember { mutableFloatStateOf(if (isDragging) 1f else if (isPlaying) 0.35f else 0f) }
    var dragVelocityBoost by remember { mutableFloatStateOf(0f) }
    val sparkAges = remember { FloatArray(10) { SEEKBAR_PARTICLES_POOL[it].seedA } }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        var lastRatio = latestRatioProvider().coerceIn(0f, 1f)
        smoothedRatio = lastRatio
        while (true) {
            withFrameNanos { frameNs ->
                if (lastNs != 0L) {
                    val dt = ((frameNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    val targetRatio = latestRatioProvider().coerceIn(0f, 1f)
                    if (latestDragging) {
                        val vel = (abs(targetRatio - lastRatio) / dt) * 0.7f
                        dragVelocityBoost = max(dragVelocityBoost * exp(-dt * 6f), vel.coerceIn(0f, 0.8f))
                        smoothedRatio = targetRatio
                    } else {
                        dragVelocityBoost *= exp(-dt * 5.5f)
                        val diff = targetRatio - smoothedRatio
                        smoothedRatio = if (abs(diff) > 0.08f) targetRatio else smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }
                    lastRatio = targetRatio

                    val targetBoost = (if (latestDragging) 0.85f else if (latestPlaying) 0.38f else 0.08f) + dragVelocityBoost
                    dragBoost += (targetBoost.coerceIn(0f, 1.35f) - dragBoost) * (1f - exp(-dt * 8f))

                    shimmerPhase = (shimmerPhase + dt * (0.66f + 0.55f * dragBoost)) % 1f
                    pulseClock = (pulseClock + dt * (3.4f + 2.2f * dragBoost)) % (2f * PI.toFloat())
                    val sparkSpeed = 0.85f + 1.15f * dragBoost
                    for (i in sparkAges.indices) {
                        sparkAges[i] = (sparkAges[i] + dt * sparkSpeed) % 1f
                    }
                }
                lastNs = frameNs
            }
        }
    }

    val pulsePhase = 0.65f + 0.35f * (0.5f + 0.5f * sin(pulseClock))
    val bladePath = remember { Path() }
    val corePath = remember { Path() }

    Canvas(
        modifier = modifier.liquidGlassScrubGesture(
            durationMs = durationMs,
            isAntiCheatLocked = isAntiCheatLocked,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish
        )
    ) {
        val ratio = smoothedRatio.coerceIn(0f, 1f)
        val padPx = 10.dp.toPx()
        val trackW = (size.width - padPx * 2f).coerceAtLeast(1f)
        val cy = size.height / 2f
        val tipX = padPx + trackW * ratio
        val primary = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonCyan
        val secondary = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonBlue
        val glowAlpha = (glowScale * (0.65f + 0.35f * pulsePhase + 0.3f * dragBoost)).coerceIn(0.2f, 1f)

        // OLED Liquid Glass Sheath Track
        val sheathH = (6.5.dp.toPx() * thicknessScale).coerceIn(4.dp.toPx(), size.height * 0.45f)
        drawRoundRect(
            color = SeekbarGlassRailDark,
            topLeft = Offset(padPx, cy - sheathH / 2f),
            size = Size(trackW, sheathH),
            cornerRadius = CornerRadius(sheathH / 2f, sheathH / 2f)
        )
        drawRoundRect(
            color = SeekbarGlassBorder,
            topLeft = Offset(padPx, cy - sheathH / 2f),
            size = Size(trackW, sheathH),
            cornerRadius = CornerRadius(sheathH / 2f, sheathH / 2f),
            style = Stroke(width = 0.8.dp.toPx())
        )
        // Frosted glass top specular glint
        drawLine(
            color = Color.White.copy(alpha = 0.16f),
            start = Offset(padPx + 6.dp.toPx(), cy - sheathH * 0.28f),
            end = Offset(padPx + trackW - 6.dp.toPx(), cy - sheathH * 0.28f),
            strokeWidth = 0.7.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawSeekbarBookmarkDiamonds(bookmarks, durationMs, padPx, trackW, cy, thicknessScale, glowScale)

        // Active Plasma Katana Blade
        val bladeLen = (tipX - padPx).coerceAtLeast(0f)
        if (bladeLen > 2f) {
            val bladeHalf = (3.4.dp.toPx() * thicknessScale) * (1f + 0.22f * dragBoost)
            val bevelLen = (11.dp.toPx() * thicknessScale).coerceAtMost(bladeLen * 0.4f)

            // Motion trail wake when scrubbing
            if (!isAntiCheatLocked && dragBoost > 0.08f) {
                val trailSpan = (38.dp.toPx() + 34.dp.toPx() * dragBoost).coerceAtMost(bladeLen)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, primary.copy(alpha = 0.45f * dragBoost), Color.White.copy(alpha = 0.55f * dragBoost)),
                        startX = tipX - trailSpan,
                        endX = tipX
                    ),
                    start = Offset(tipX - trailSpan, cy),
                    end = Offset(tipX + 4.dp.toPx(), cy),
                    strokeWidth = (11.dp.toPx() * thicknessScale * dragBoost).coerceAtLeast(2f),
                    cap = StrokeCap.Round
                )
            }

            // Wide neon aura
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        secondary.copy(alpha = 0.18f * glowAlpha),
                        primary.copy(alpha = 0.45f * glowAlpha),
                        Color.White.copy(alpha = 0.42f * glowAlpha)
                    ),
                    startX = padPx,
                    endX = tipX
                ),
                start = Offset(padPx, cy),
                end = Offset(tipX, cy),
                strokeWidth = sheathH * 2.15f,
                cap = StrokeCap.Round
            )

            // Chamfered Katana Silhouette
            bladePath.reset()
            bladePath.moveTo(padPx, cy - bladeHalf)
            bladePath.lineTo((tipX - bevelLen).coerceAtLeast(padPx), cy - bladeHalf)
            bladePath.lineTo(tipX + 2.dp.toPx(), cy - bladeHalf * 0.15f)
            bladePath.lineTo((tipX - bevelLen * 0.45f).coerceAtLeast(padPx), cy + bladeHalf * 0.85f)
            bladePath.lineTo(padPx, cy + bladeHalf * 0.85f)
            bladePath.close()

            drawPath(
                path = bladePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(secondary, primary, Color.White),
                    startX = padPx,
                    endX = tipX
                )
            )

            // Travelling plasma pulse wave inside the blade
            val pulseX = padPx + bladeLen * shimmerPhase
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.8f * glowAlpha), primary.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(pulseX, cy),
                    radius = 16.dp.toPx()
                ),
                radius = 16.dp.toPx(),
                center = Offset(pulseX, cy)
            )

            // Razor white inner edge
            corePath.reset()
            corePath.moveTo(padPx + 2.dp.toPx(), cy - bladeHalf * 0.28f)
            corePath.lineTo(tipX, cy - bladeHalf * 0.22f)
            drawPath(
                path = corePath,
                color = Color.White.copy(alpha = 0.92f),
                style = Stroke(width = 1.4.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
            )

            // Continuous-loop sparks along the katana tip (zero resets)
            if (!isAntiCheatLocked) {
                val sparkCount = if (isDragging) 10 else 5
                for (i in 0 until sparkCount) {
                    val p = SEEKBAR_PARTICLES_POOL[i]
                    val life = sparkAges[i]
                    val dirY = p.polarity * (0.4f + p.seedB * 0.8f)
                    val travelX = -life * (14.dp.toPx() + 26.dp.toPx() * dragBoost) + 4.dp.toPx()
                    val travelY = dirY * life * (8.dp.toPx() + 10.dp.toPx() * dragBoost)
                    val sparkStart = Offset(tipX + travelX, cy + travelY)
                    val sparkEnd = Offset(sparkStart.x - 4.dp.toPx(), sparkStart.y + dirY * 2.dp.toPx())
                    val alpha = (1f - life) * (0.55f + 0.45f * dragBoost)
                    drawLine(
                        color = lerp(primary, Color.White, 1f - life).copy(alpha = alpha.coerceIn(0f, 1f)),
                        start = sparkStart,
                        end = sparkEnd,
                        strokeWidth = (1.6.dp.toPx() * (1f - life * 0.5f)).coerceAtLeast(0.8f),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Liquid Glass Katana Tip Cursor
        val tipRadius = (5.2.dp.toPx() * thicknessScale) * (1f + 0.24f * dragBoost)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.6f * glowAlpha), Color.Transparent),
                center = Offset(tipX, cy),
                radius = tipRadius * 2.8f
            ),
            radius = tipRadius * 2.8f,
            center = Offset(tipX, cy)
        )
        drawCircle(color = SeekbarOledBlack, radius = tipRadius, center = Offset(tipX, cy))
        drawCircle(color = primary, radius = tipRadius, center = Offset(tipX, cy), style = Stroke(width = 1.6.dp.toPx()))
        drawCircle(color = Color.White, radius = tipRadius * 0.45f, center = Offset(tipX, cy))
    }
}

// ---------------------------------------------------------------------------
// 2. ⚛️ QUANTUM COLLIDER SEEKBAR
// Horizontal transparent liquid glass tube with counter-streaming particles
// colliding into expanding photon burst flashes at the playback cursor.
// ---------------------------------------------------------------------------
@Composable
fun QuantumColliderSeekbar(
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestPlaying by rememberUpdatedState(isPlaying)
    val latestDragging by rememberUpdatedState(isDragging)
    val latestRatioProvider by rememberUpdatedState(progressRatioProvider)

    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }
    var burstPhase by remember { mutableFloatStateOf(0f) }
    var rayAngle by remember { mutableFloatStateOf(0f) }
    var dragBoost by remember { mutableFloatStateOf(if (isDragging) 1f else if (isPlaying) 0.4f else 0f) }
    val streamProgress = remember { FloatArray(12) { SEEKBAR_PARTICLES_POOL[it].seedA } }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            withFrameNanos { frameNs ->
                if (lastNs != 0L) {
                    val dt = ((frameNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    val targetRatio = latestRatioProvider().coerceIn(0f, 1f)
                    if (latestDragging) {
                        smoothedRatio = targetRatio
                    } else {
                        val diff = targetRatio - smoothedRatio
                        smoothedRatio = if (abs(diff) > 0.08f) targetRatio else smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }

                    val targetBoost = if (latestDragging) 1.0f else if (latestPlaying) 0.42f else 0.08f
                    dragBoost += (targetBoost - dragBoost) * (1f - exp(-dt * 7.5f))

                    val streamSpeed = 0.78f + 1.05f * dragBoost
                    for (i in streamProgress.indices) {
                        streamProgress[i] = (streamProgress[i] + dt * streamSpeed) % 1f
                    }
                    burstPhase = (burstPhase + dt * (1.1f + 0.8f * dragBoost)) % 1f
                    rayAngle = (rayAngle + dt * (1.6f + 1.8f * dragBoost)) % (2f * PI.toFloat())
                }
                lastNs = frameNs
            }
        }
    }

    Canvas(
        modifier = modifier.liquidGlassScrubGesture(
            durationMs = durationMs,
            isAntiCheatLocked = isAntiCheatLocked,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish
        )
    ) {
        val ratio = smoothedRatio.coerceIn(0f, 1f)
        val padPx = 10.dp.toPx()
        val trackW = (size.width - padPx * 2f).coerceAtLeast(1f)
        val cy = size.height / 2f
        val collisionX = padPx + trackW * ratio
        val cyan = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonCyan
        val violet = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonViolet
        val pink = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonPink
        val glowAlpha = (glowScale * (0.7f + 0.3f * dragBoost)).coerceIn(0.25f, 1f)

        val tubeH = (11.dp.toPx() * thicknessScale).coerceIn(7.dp.toPx(), size.height * 0.65f)
        val tubeTop = cy - tubeH / 2f

        // Transparent Glass Accelerator Chamber
        drawRoundRect(
            color = SeekbarGlassRailDark,
            topLeft = Offset(padPx, tubeTop),
            size = Size(trackW, tubeH),
            cornerRadius = CornerRadius(tubeH / 2f, tubeH / 2f)
        )

        // Subtle containment rings every ~20%
        for (i in 1..4) {
            val ringX = padPx + trackW * (i / 5f)
            drawLine(
                color = Color.White.copy(alpha = 0.14f),
                start = Offset(ringX, tubeTop + 1.dp.toPx()),
                end = Offset(ringX, tubeTop + tubeH - 1.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Active Left-to-Right Beam Glow inside tube
        if (collisionX > padPx) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(cyan.copy(alpha = 0.18f), cyan.copy(alpha = 0.45f * glowAlpha)),
                    startX = padPx,
                    endX = collisionX
                ),
                topLeft = Offset(padPx, tubeTop + 1.5.dp.toPx()),
                size = Size((collisionX - padPx).coerceAtLeast(1f), tubeH - 3.dp.toPx()),
                cornerRadius = CornerRadius(tubeH / 2f, tubeH / 2f)
            )
        }

        // Colliding Particle Streams (continuous infinite loop)
        if (!isAntiCheatLocked) {
            val particleCount = 12
            for (i in 0 until particleCount) {
                val p = SEEKBAR_PARTICLES_POOL[i]
                val progressT = streamProgress[i]
                val laneY = cy + p.polarity * (tubeH * 0.2f * (0.35f + p.seedB * 0.65f))

                if (i % 2 == 0) {
                    val leftSpan = (collisionX - padPx).coerceAtLeast(0f)
                    if (leftSpan > 4f) {
                        val px = padPx + leftSpan * progressT
                        val tailLen = (8.dp.toPx() + 10.dp.toPx() * dragBoost).coerceAtMost(px - padPx)
                        drawLine(
                            color = cyan.copy(alpha = (0.45f + 0.55f * progressT) * glowAlpha),
                            start = Offset(px - tailLen, laneY),
                            end = Offset(px, laneY),
                            strokeWidth = 1.8.dp.toPx() * thicknessScale,
                            cap = StrokeCap.Round
                        )
                        drawCircle(Color.White, radius = 1.3.dp.toPx(), center = Offset(px, laneY))
                    }
                } else {
                    val rightEnd = padPx + trackW
                    val rightSpan = (rightEnd - collisionX).coerceAtLeast(0f)
                    if (rightSpan > 4f) {
                        val px = rightEnd - rightSpan * progressT
                        val tailLen = (8.dp.toPx() + 10.dp.toPx() * dragBoost).coerceAtMost(rightEnd - px)
                        drawLine(
                            color = violet.copy(alpha = (0.4f + 0.5f * progressT) * glowAlpha),
                            start = Offset(px + tailLen, laneY),
                            end = Offset(px, laneY),
                            strokeWidth = 1.6.dp.toPx() * thicknessScale,
                            cap = StrokeCap.Round
                        )
                        drawCircle(pink, radius = 1.2.dp.toPx(), center = Offset(px, laneY))
                    }
                }
            }
        }

        // Glass Outer Border & Top Specular Reflection
        drawRoundRect(
            color = Color.White.copy(alpha = 0.30f),
            topLeft = Offset(padPx, tubeTop),
            size = Size(trackW, tubeH),
            cornerRadius = CornerRadius(tubeH / 2f, tubeH / 2f),
            style = Stroke(width = 1.dp.toPx())
        )
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(padPx + 6.dp.toPx(), tubeTop + 1.8.dp.toPx()),
            end = Offset(padPx + trackW - 6.dp.toPx(), tubeTop + 1.8.dp.toPx()),
            strokeWidth = 0.9.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawSeekbarBookmarkDiamonds(bookmarks, durationMs, padPx, trackW, cy, thicknessScale, glowScale)

        // Photon Burst Collision Flash at Active Cursor
        val burstR = (6.dp.toPx() + burstPhase * (11.dp.toPx() + 8.dp.toPx() * dragBoost)) * thicknessScale
        val burstAlpha = ((1f - burstPhase) * (0.55f + 0.45f * dragBoost) * glowAlpha).coerceIn(0f, 1f)
        drawCircle(
            color = cyan.copy(alpha = burstAlpha),
            radius = burstR,
            center = Offset(collisionX, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = pink.copy(alpha = burstAlpha * 0.75f),
            radius = burstR * 0.65f,
            center = Offset(collisionX, cy),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // 6-ray photon burst flare rotating continuously
        if (!isAntiCheatLocked) {
            val rayLen = (6.5.dp.toPx() + 6.dp.toPx() * dragBoost) * thicknessScale
            for (rIdx in 0 until 6) {
                val angle = (rIdx * (PI / 3.0) + rayAngle).toFloat()
                val ex = collisionX + cos(angle) * rayLen
                val ey = cy + sin(angle) * rayLen
                drawLine(
                    color = Color.White.copy(alpha = 0.45f + 0.45f * dragBoost),
                    start = Offset(collisionX, cy),
                    end = Offset(ex, ey),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Photon Collider Cursor Core
        val coreR = (4.8.dp.toPx() * thicknessScale) * (1f + 0.22f * dragBoost)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, cyan, violet.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(collisionX, cy),
                radius = coreR * 2.5f
            ),
            radius = coreR * 2.5f,
            center = Offset(collisionX, cy)
        )
        drawCircle(color = Color.White, radius = coreR * 0.68f, center = Offset(collisionX, cy))
    }
}

// ---------------------------------------------------------------------------
// 3. 🧬 DOUBLE HELIX GENE SEEKBAR
// Intertwined luminous digital DNA strands along the timeline whose wave
// expansion and spin rate dynamically accelerate when scrubbing.
// ---------------------------------------------------------------------------
@Composable
fun DoubleHelixGeneSeekbar(
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestPlaying by rememberUpdatedState(isPlaying)
    val latestDragging by rememberUpdatedState(isDragging)
    val latestRatioProvider by rememberUpdatedState(progressRatioProvider)

    var helixPhase by remember { mutableFloatStateOf(0f) }
    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }
    var dragBoost by remember { mutableFloatStateOf(if (isDragging) 1f else if (isPlaying) 0.4f else 0f) }
    var smoothedAngVel by remember { mutableFloatStateOf(if (isPlaying) 4.2f else 1.4f) }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            withFrameNanos { frameNs ->
                if (lastNs != 0L) {
                    val dt = ((frameNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    val targetRatio = latestRatioProvider().coerceIn(0f, 1f)
                    if (latestDragging) {
                        smoothedRatio = targetRatio
                    } else {
                        val diff = targetRatio - smoothedRatio
                        smoothedRatio = if (abs(diff) > 0.08f) targetRatio else smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }
                    val targetBoost = if (latestDragging) 1.0f else if (latestPlaying) 0.40f else 0.0f
                    dragBoost += (targetBoost - dragBoost) * (1f - exp(-dt * 8f))

                    val targetVel = when {
                        latestDragging -> 9.5f
                        latestPlaying -> 4.2f
                        else -> 1.4f
                    }
                    smoothedAngVel += (targetVel - smoothedAngVel) * (1f - exp(-dt * 7f))
                    helixPhase = (helixPhase + dt * smoothedAngVel) % (2f * PI.toFloat())
                }
                lastNs = frameNs
            }
        }
    }

    val strandAPath = remember { Path() }
    val strandBPath = remember { Path() }

    Canvas(
        modifier = modifier.liquidGlassScrubGesture(
            durationMs = durationMs,
            isAntiCheatLocked = isAntiCheatLocked,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish
        )
    ) {
        val ratio = smoothedRatio.coerceIn(0f, 1f)
        val padPx = 10.dp.toPx()
        val trackW = (size.width - padPx * 2f).coerceAtLeast(1f)
        val cy = size.height / 2f
        val thumbX = padPx + trackW * ratio
        val cyan = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonCyan
        val pink = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonPink
        val glowAlpha = (glowScale * (0.72f + 0.28f * dragBoost)).coerceIn(0.25f, 1f)

        // OLED Liquid Glass Central Spine
        drawLine(
            color = SeekbarGlassRailDark,
            start = Offset(padPx, cy),
            end = Offset(padPx + trackW, cy),
            strokeWidth = 4.dp.toPx() * thicknessScale,
            cap = StrokeCap.Round
        )
        drawLine(
            color = SeekbarGlassBorder,
            start = Offset(padPx, cy - 1.5.dp.toPx()),
            end = Offset(padPx + trackW, cy - 1.5.dp.toPx()),
            strokeWidth = 0.7.dp.toPx()
        )

        val maxAmp = (size.height * 0.34f).coerceAtMost(9.5.dp.toPx() * thicknessScale)
        val helixAmp = maxAmp * (0.64f + 0.42f * dragBoost)
        val stepCount = 52
        val rungStep = 4

        // Draw Base-Pair Rungs first so strands layer cleanly on top
        for (i in 0..stepCount step rungStep) {
            val t = i / stepCount.toFloat()
            val x = padPx + trackW * t
            val angle = t * 5.2f * PI.toFloat() + helixPhase
            val yA = cy + sin(angle) * helixAmp
            val yB = cy - sin(angle) * helixAmp
            val isPlayed = x <= thumbX
            val rungAlpha = if (isPlayed) 0.55f * glowAlpha else 0.16f
            drawLine(
                color = lerp(cyan, pink, (cos(angle) * 0.5f + 0.5f)).copy(alpha = rungAlpha),
                start = Offset(x, yA),
                end = Offset(x, yB),
                strokeWidth = (1.3.dp.toPx() * thicknessScale),
                cap = StrokeCap.Round
            )
            if (isPlayed && !isAntiCheatLocked) {
                drawCircle(cyan.copy(alpha = 0.75f * glowAlpha), radius = 1.6.dp.toPx(), center = Offset(x, yA))
                drawCircle(pink.copy(alpha = 0.75f * glowAlpha), radius = 1.6.dp.toPx(), center = Offset(x, yB))
            }
        }

        // Build full unplayed + played sinusoidal paths
        strandAPath.reset()
        strandBPath.reset()
        for (i in 0..stepCount) {
            val t = i / stepCount.toFloat()
            val x = padPx + trackW * t
            val angle = t * 5.2f * PI.toFloat() + helixPhase
            val yA = cy + sin(angle) * helixAmp
            val yB = cy - sin(angle) * helixAmp
            if (i == 0) {
                strandAPath.moveTo(x, yA)
                strandBPath.moveTo(x, yB)
            } else {
                strandAPath.lineTo(x, yA)
                strandBPath.lineTo(x, yB)
            }
        }

        // Faint unplayed background strands
        drawPath(
            path = strandAPath,
            color = cyan.copy(alpha = 0.22f),
            style = Stroke(width = 1.8.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
        )
        drawPath(
            path = strandBPath,
            color = pink.copy(alpha = 0.22f),
            style = Stroke(width = 1.8.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
        )

        // Glowing active strands up to thumbX
        val activeSteps = ((ratio * stepCount).toInt()).coerceIn(0, stepCount)
        if (activeSteps > 0) {
            strandAPath.reset()
            strandBPath.reset()
            for (i in 0..activeSteps) {
                val t = (i / stepCount.toFloat()).coerceAtMost(ratio)
                val x = padPx + trackW * t
                val angle = t * 5.2f * PI.toFloat() + helixPhase
                val yA = cy + sin(angle) * helixAmp
                val yB = cy - sin(angle) * helixAmp
                if (i == 0) {
                    strandAPath.moveTo(x, yA)
                    strandBPath.moveTo(x, yB)
                } else {
                    strandAPath.lineTo(x, yA)
                    strandBPath.lineTo(x, yB)
                }
            }
            // Glow + crisp core for Strand A & Strand B
            drawPath(
                path = strandAPath,
                color = cyan.copy(alpha = 0.35f * glowAlpha),
                style = Stroke(width = 5.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
            )
            drawPath(
                path = strandBPath,
                color = pink.copy(alpha = 0.35f * glowAlpha),
                style = Stroke(width = 5.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
            )
            drawPath(
                path = strandAPath,
                color = cyan,
                style = Stroke(width = 2.2.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
            )
            drawPath(
                path = strandBPath,
                color = pink,
                style = Stroke(width = 2.2.dp.toPx() * thicknessScale, cap = StrokeCap.Round)
            )
        }

        drawSeekbarBookmarkDiamonds(bookmarks, durationMs, padPx, trackW, cy, thicknessScale, glowScale)

        // Genetic Node Cursor
        val cursorR = (5.4.dp.toPx() * thicknessScale) * (1f + 0.24f * dragBoost)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(cyan.copy(alpha = 0.55f * glowAlpha), pink.copy(alpha = 0.25f), Color.Transparent),
                center = Offset(thumbX, cy),
                radius = cursorR * 2.6f
            ),
            radius = cursorR * 2.6f,
            center = Offset(thumbX, cy)
        )
        drawCircle(color = SeekbarOledBlack, radius = cursorR, center = Offset(thumbX, cy))
        drawCircle(
            brush = Brush.sweepGradient(listOf(cyan, pink, cyan), center = Offset(thumbX, cy)),
            radius = cursorR,
            center = Offset(thumbX, cy),
            style = Stroke(width = 1.8.dp.toPx())
        )
        drawCircle(color = Color.White, radius = cursorR * 0.42f, center = Offset(thumbX, cy))
    }
}

// ---------------------------------------------------------------------------
// 4. 🕳️ EVENT HORIZON GRAVITATIONAL SEEKBAR
// Gravitational lensing distortion around the playback cursor, pulling
// background star particles inward with real-time physics feedback.
// ---------------------------------------------------------------------------
@Composable
fun EventHorizonSeekbar(
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestPlaying by rememberUpdatedState(isPlaying)
    val latestDragging by rememberUpdatedState(isDragging)
    val latestRatioProvider by rememberUpdatedState(progressRatioProvider)

    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }
    var dragBoost by remember { mutableFloatStateOf(if (isDragging) 1f else if (isPlaying) 0.35f else 0f) }
    var breathClock by remember { mutableFloatStateOf(0f) }
    var twinkleClock by remember { mutableFloatStateOf(0f) }
    val starPulls = remember { FloatArray(18) { SEEKBAR_PARTICLES_POOL[it].seedC } }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            withFrameNanos { frameNs ->
                if (lastNs != 0L) {
                    val dt = ((frameNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    val targetRatio = latestRatioProvider().coerceIn(0f, 1f)
                    if (latestDragging) {
                        smoothedRatio = targetRatio
                    } else {
                        val diff = targetRatio - smoothedRatio
                        smoothedRatio = if (abs(diff) > 0.08f) targetRatio else smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }
                    val targetBoost = if (latestDragging) 1.0f else if (latestPlaying) 0.38f else 0.08f
                    dragBoost += (targetBoost - dragBoost) * (1f - exp(-dt * 7.5f))

                    breathClock = (breathClock + dt * (2.6f + 1.5f * dragBoost)) % (2f * PI.toFloat())
                    twinkleClock = (twinkleClock + dt * 3.1f) % (2f * PI.toFloat())
                    val pullRate = 0.55f + 0.95f * dragBoost
                    for (i in starPulls.indices) {
                        starPulls[i] = (starPulls[i] + dt * pullRate) % 1f
                    }
                }
                lastNs = frameNs
            }
        }
    }

    val lensBreath = 0.85f + 0.20f * (0.5f + 0.5f * sin(breathClock))
    val topWarpPath = remember { Path() }
    val bottomWarpPath = remember { Path() }

    Canvas(
        modifier = modifier.liquidGlassScrubGesture(
            durationMs = durationMs,
            isAntiCheatLocked = isAntiCheatLocked,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish
        )
    ) {
        val ratio = smoothedRatio.coerceIn(0f, 1f)
        val padPx = 10.dp.toPx()
        val trackW = (size.width - padPx * 2f).coerceAtLeast(1f)
        val cy = size.height / 2f
        val holeX = padPx + trackW * ratio
        val violet = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonViolet
        val cyan = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonCyan
        val glowAlpha = (glowScale * (0.72f + 0.28f * dragBoost)).coerceIn(0.25f, 1f)

        val lensRadius = (16.dp.toPx() + 9.dp.toPx() * dragBoost) * thicknessScale
        val warpStrength = (6.5.dp.toPx() + 5.5.dp.toPx() * dragBoost) * thicknessScale

        // Spacetime Horizon Lattice Lines that bend symmetrically around holeX
        topWarpPath.reset()
        bottomWarpPath.reset()
        val baseOffset = 3.2.dp.toPx() * thicknessScale
        val samples = 44
        for (i in 0..samples) {
            val t = i / samples.toFloat()
            val x = padPx + trackW * t
            val dx = abs(x - holeX)
            val influence = if (dx < lensRadius * 1.9f) {
                val u = 1f - (dx / (lensRadius * 1.9f))
                u * u
            } else 0f
            val yTop = cy - baseOffset - influence * warpStrength
            val yBot = cy + baseOffset + influence * warpStrength
            if (i == 0) {
                topWarpPath.moveTo(x, yTop)
                bottomWarpPath.moveTo(x, yBot)
            } else {
                topWarpPath.lineTo(x, yTop)
                bottomWarpPath.lineTo(x, yBot)
            }
        }

        // Dark Void Base Track
        drawLine(
            color = SeekbarGlassRailDark,
            start = Offset(padPx, cy),
            end = Offset(padPx + trackW, cy),
            strokeWidth = 5.5.dp.toPx() * thicknessScale,
            cap = StrokeCap.Round
        )
        if (holeX > padPx) {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(violet.copy(alpha = 0.28f), cyan.copy(alpha = 0.72f * glowAlpha)),
                    startX = padPx,
                    endX = holeX
                ),
                start = Offset(padPx, cy),
                end = Offset(holeX, cy),
                strokeWidth = 3.2.dp.toPx() * thicknessScale,
                cap = StrokeCap.Round
            )
        }

        drawPath(
            path = topWarpPath,
            color = cyan.copy(alpha = 0.34f * glowAlpha),
            style = Stroke(width = 1.1.dp.toPx())
        )
        drawPath(
            path = bottomWarpPath,
            color = violet.copy(alpha = 0.34f * glowAlpha),
            style = Stroke(width = 1.1.dp.toPx())
        )

        // Gravitational Star & Dot Particles actively pulled toward holeX in continuous loop
        if (!isAntiCheatLocked) {
            val captureZone = 62.dp.toPx() + 36.dp.toPx() * dragBoost
            for (i in 0 until 18) {
                val p = SEEKBAR_PARTICLES_POOL[i]
                val starBaseX = padPx + trackW * p.seedA
                val starBaseY = cy + p.polarity * (size.height * 0.42f * (0.25f + p.seedB * 0.75f))
                val dist = abs(starBaseX - holeX)
                if (dist < captureZone) {
                    val pullPhase = starPulls[i]
                    val pullStrength = (1f - dist / captureZone).coerceIn(0f, 1f)
                    val curX = starBaseX + (holeX - starBaseX) * pullPhase * pullStrength
                    val spiralAngle = pullPhase * 3.8f + p.seedB * 6.28f
                    val curY = starBaseY + (cy - starBaseY) * pullPhase * pullStrength +
                        sin(spiralAngle) * 2.5.dp.toPx() * (1f - pullPhase)
                    val streakX = curX + (holeX - curX) * 0.24f
                    val streakY = curY + (cy - curY) * 0.24f
                    val alpha = (0.35f + 0.65f * pullStrength) * (1f - pullPhase * 0.4f) * glowAlpha
                    drawLine(
                        color = lerp(Color.White, if (i % 2 == 0) cyan else violet, pullStrength).copy(alpha = alpha.coerceIn(0f, 1f)),
                        start = Offset(curX, curY),
                        end = Offset(streakX, streakY),
                        strokeWidth = (1.3.dp.toPx() + 0.8.dp.toPx() * dragBoost),
                        cap = StrokeCap.Round
                    )
                } else {
                    val twinkle = 0.25f + 0.35f * abs(sin(twinkleClock + p.seedC * 2f * PI.toFloat()))
                    drawCircle(
                        color = Color.White.copy(alpha = twinkle),
                        radius = (1.1.dp.toPx() + 0.7.dp.toPx() * p.seedB),
                        center = Offset(starBaseX, starBaseY)
                    )
                }
            }
        }

        drawSeekbarBookmarkDiamonds(bookmarks, durationMs, padPx, trackW, cy, thicknessScale, glowScale)

        // Gravitational Lensing Rings & Pitch-Black Singularity Core
        val coreR = (5.4.dp.toPx() * thicknessScale) * (1f + 0.22f * dragBoost)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SeekbarOledBlack,
                    violet.copy(alpha = 0.68f * glowAlpha),
                    cyan.copy(alpha = 0.34f * glowAlpha),
                    Color.Transparent
                ),
                center = Offset(holeX, cy),
                radius = lensRadius * lensBreath
            ),
            radius = lensRadius * lensBreath,
            center = Offset(holeX, cy)
        )
        drawCircle(
            color = SeekbarOledBlack,
            radius = coreR,
            center = Offset(holeX, cy)
        )
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(violet, cyan, Color.White, violet),
                center = Offset(holeX, cy)
            ),
            radius = coreR * 1.15f,
            center = Offset(holeX, cy),
            style = Stroke(width = 1.7.dp.toPx() * thicknessScale)
        )
    }
}

// ---------------------------------------------------------------------------
// 5. 🟩 CYBER MATRIX CODE SEEKBAR
// Digital stream of cascading green & cyan geometric binary code glyphs flowing
// along the OLED frosted timeline, accelerating smoothly during scrubbing.
// ---------------------------------------------------------------------------
@Composable
fun CyberMatrixCodeSeekbar(
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestPlaying by rememberUpdatedState(isPlaying)
    val latestDragging by rememberUpdatedState(isDragging)
    val latestRatioProvider by rememberUpdatedState(progressRatioProvider)

    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }
    var dragBoost by remember { mutableFloatStateOf(if (isDragging) 1f else if (isPlaying) 0.42f else 0f) }
    var streamClock by remember { mutableFloatStateOf(0f) }
    var smoothedSpeed by remember { mutableFloatStateOf(if (isPlaying) 1.15f else 0.45f) }
    val columnCount = 26
    val colCycles = remember { FloatArray(columnCount) { (it * 0.37f) % 1f } }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            withFrameNanos { frameNs ->
                if (lastNs != 0L) {
                    val dt = ((frameNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    val targetRatio = latestRatioProvider().coerceIn(0f, 1f)
                    if (latestDragging) {
                        smoothedRatio = targetRatio
                    } else {
                        val diff = targetRatio - smoothedRatio
                        smoothedRatio = if (abs(diff) > 0.08f) targetRatio else smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }
                    val targetBoost = if (latestDragging) 1.0f else if (latestPlaying) 0.42f else 0.0f
                    dragBoost += (targetBoost - dragBoost) * (1f - exp(-dt * 8f))

                    val targetSpeed = when {
                        latestDragging -> 2.8f
                        latestPlaying -> 1.15f
                        else -> 0.45f
                    }
                    smoothedSpeed += (targetSpeed - smoothedSpeed) * (1f - exp(-dt * 7f))
                    streamClock = (streamClock + dt * smoothedSpeed) % 1000f
                    for (c in 0 until columnCount) {
                        colCycles[c] = (colCycles[c] + dt * smoothedSpeed * (0.85f + (c % 3) * 0.28f)) % 1f
                    }
                }
                lastNs = frameNs
            }
        }
    }

    Canvas(
        modifier = modifier.liquidGlassScrubGesture(
            durationMs = durationMs,
            isAntiCheatLocked = isAntiCheatLocked,
            onLockedAttempt = onLockedAttempt,
            onSeekStart = onSeekStart,
            onSeekUpdate = onSeekUpdate,
            onSeekFinish = onSeekFinish
        )
    ) {
        val ratio = smoothedRatio.coerceIn(0f, 1f)
        val padPx = 10.dp.toPx()
        val trackW = (size.width - padPx * 2f).coerceAtLeast(1f)
        val cy = size.height / 2f
        val headX = padPx + trackW * ratio
        val matrixGreen = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonEmerald
        val matrixBright = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonMatrixBright
        val cyan = if (isAntiCheatLocked) SeekbarLockedSlate else SeekbarNeonCyan
        val glowAlpha = (glowScale * (0.72f + 0.28f * dragBoost)).coerceIn(0.25f, 1f)

        val railH = (14.dp.toPx() * thicknessScale).coerceIn(9.dp.toPx(), size.height * 0.72f)
        val railTop = cy - railH / 2f

        // Frosted OLED Terminal Rail
        drawRoundRect(
            color = SeekbarOledBlack.copy(alpha = 0.88f),
            topLeft = Offset(padPx, railTop),
            size = Size(trackW, railH),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(matrixGreen.copy(alpha = 0.45f), cyan.copy(alpha = 0.45f))
            ),
            topLeft = Offset(padPx, railTop),
            size = Size(trackW, railH),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
            style = Stroke(width = 0.9.dp.toPx())
        )

        // Active Played Neon Terminal Under-Glow
        if (headX > padPx) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(matrixGreen.copy(alpha = 0.24f), cyan.copy(alpha = 0.40f * glowAlpha)),
                    startX = padPx,
                    endX = headX
                ),
                topLeft = Offset(padPx + 1.dp.toPx(), railTop + 1.dp.toPx()),
                size = Size((headX - padPx - 1.dp.toPx()).coerceAtLeast(1f), railH - 2.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        // Cascading Geometric Binary Glyphs (0 rings & 1 bars for crisp 60 FPS rendering)
        val colStep = trackW / columnCount
        val glyphW = (colStep * 0.44f).coerceIn(2.2.dp.toPx(), 4.2.dp.toPx())
        val glyphH = (railH * 0.44f).coerceIn(3.5.dp.toPx(), 6.dp.toPx())

        for (col in 0 until columnCount) {
            val baseX = padPx + (col + 0.5f) * colStep
            val isPlayed = baseX <= headX
            val verticalCycle = colCycles[col]
            val yOffset = ((verticalCycle - 0.5f) * (railH * 0.52f))
            val gy = cy + yOffset
            val isOne = ((col + (streamClock * 4f).toInt()) % 2 == 0)
            val glyphAlpha = if (isPlayed) {
                (0.55f + 0.45f * (1f - abs(yOffset) / (railH * 0.45f))) * glowAlpha
            } else {
                0.22f
            }.coerceIn(0.12f, 1f)
            val glyphColor = when {
                isAntiCheatLocked -> SeekbarLockedSlate.copy(alpha = glyphAlpha)
                isPlayed && abs(baseX - headX) < colStep * 2.2f -> Color.White.copy(alpha = glyphAlpha)
                isPlayed && col % 3 == 0 -> cyan.copy(alpha = glyphAlpha)
                isPlayed -> matrixBright.copy(alpha = glyphAlpha)
                else -> matrixGreen.copy(alpha = glyphAlpha)
            }

            if (isOne) {
                // Crisp vertical '1' laser glyph
                drawLine(
                    color = glyphColor,
                    start = Offset(baseX, gy - glyphH * 0.45f),
                    end = Offset(baseX, gy + glyphH * 0.45f),
                    strokeWidth = (1.5.dp.toPx() * thicknessScale),
                    cap = StrokeCap.Round
                )
            } else {
                // Crisp hollow '0' binary capsule
                drawRoundRect(
                    color = glyphColor,
                    topLeft = Offset(baseX - glyphW * 0.42f, gy - glyphH * 0.42f),
                    size = Size(glyphW * 0.84f, glyphH * 0.84f),
                    cornerRadius = CornerRadius(glyphW * 0.35f, glyphW * 0.35f),
                    style = Stroke(width = 1.25.dp.toPx() * thicknessScale)
                )
            }
        }

        drawSeekbarBookmarkDiamonds(bookmarks, durationMs, padPx, trackW, cy, thicknessScale, glowScale)

        // Holographic Matrix Scanner Cursor
        val scannerHalfH = (railH * 0.78f).coerceAtLeast(8.dp.toPx())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(cyan.copy(alpha = 0.55f * glowAlpha), matrixGreen.copy(alpha = 0.25f), Color.Transparent),
                center = Offset(headX, cy),
                radius = scannerHalfH * 1.4f
            ),
            radius = scannerHalfH * 1.4f,
            center = Offset(headX, cy)
        )
        drawRoundRect(
            color = SeekbarOledBlack,
            topLeft = Offset(headX - 3.dp.toPx(), cy - scannerHalfH / 2f),
            size = Size(6.dp.toPx(), scannerHalfH),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(cyan, matrixBright)),
            topLeft = Offset(headX - 3.dp.toPx(), cy - scannerHalfH / 2f),
            size = Size(6.dp.toPx(), scannerHalfH),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawLine(
            color = Color.White,
            start = Offset(headX, cy - scannerHalfH * 0.28f),
            end = Offset(headX, cy + scannerHalfH * 0.28f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Reusable live animated preview of any of the 5 Liquid Glass Seekbars for Store, Inventory,
 * Forge, and Watch Settings cards (interactive when onSeekUpdate != null, or auto-gliding).
 */
@Composable
fun LiquidGlassSeekbarPreview(
    style: PlayerSeekbarStyle,
    progressRatio: Float = 0.68f,
    isPlaying: Boolean = true,
    isDragging: Boolean = false,
    isLocked: Boolean = false,
    onProgressChange: ((Float) -> Unit)? = null,
    onDraggingChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val provider: () -> Float = { progressRatio.coerceIn(0f, 1f) }
    val noopLocked: () -> Unit = {}
    val noopFloat: (Float) -> Unit = {}
    val noopFinish: () -> Unit = {}
    val handleStart: (Float) -> Unit = if (onProgressChange != null) {
        { ratio ->
            onDraggingChange?.invoke(true)
            onProgressChange(ratio)
        }
    } else noopFloat
    val handleUpdate: (Float) -> Unit = if (onProgressChange != null) {
        { ratio ->
            onProgressChange(ratio)
        }
    } else noopFloat
    val handleFinish: () -> Unit = if (onProgressChange != null) {
        {
            onDraggingChange?.invoke(false)
        }
    } else noopFinish

    LiquidGlassSeekbarDispatcher(
        style = style,
        progressRatioProvider = provider,
        durationMs = 100_000L,
        isPlaying = isPlaying,
        isDragging = isDragging,
        isAntiCheatLocked = isLocked,
        bookmarks = emptyList(),
        thicknessScale = 0.92f,
        glowScale = if (isLocked) 0.45f else 1.0f,
        onLockedAttempt = noopLocked,
        onSeekStart = handleStart,
        onSeekUpdate = handleUpdate,
        onSeekFinish = handleFinish,
        modifier = modifier
    )
}
