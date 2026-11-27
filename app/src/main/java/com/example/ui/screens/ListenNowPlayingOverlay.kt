package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LocalAudioTrack
import com.example.data.util.LocalMediaScanner
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListenNowPlayingOverlay(
    isArabic: Boolean,
    currentTrack: LocalAudioTrack,
    isPlaying: Boolean,
    isFavorite: Boolean,
    currentPositionMs: Float,
    currentDurationMs: Float,
    sleepTimerMinutes: Int,
    sleepTimerRemainingSeconds: Int,
    castDeviceName: String,
    onCollapse: () -> Unit,
    onSeekDrag: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenFiles: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenOptions: () -> Unit,
    onOpenQueue: () -> Unit,
    onDeviceClick: () -> Unit,
    onCastClick: () -> Unit
) {
    BackHandler(onBack = onCollapse)

    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var containerHeightPx by remember { mutableFloatStateOf(2000f) }
    val dragOffsetY = remember { Animatable(0f) }
    val dragProgress = (dragOffsetY.value / (containerHeightPx * 0.45f).coerceAtLeast(320f)).coerceIn(0f, 1f)

    var coverBitmap by remember(currentTrack.id, currentTrack.audioUri, currentTrack.albumArtUri) {
        mutableStateOf<Bitmap?>(null)
    }
    LaunchedEffect(currentTrack.id, currentTrack.audioUri, currentTrack.albumArtUri) {
        coverBitmap = if (currentTrack.isFromDevice && (currentTrack.audioUri != null || currentTrack.albumArtUri != null)) {
            withContext(Dispatchers.IO) {
                LocalMediaScanner.extractAudioCoverArt(
                    context = context,
                    audioUriStr = currentTrack.audioUri,
                    albumArtUriStr = currentTrack.albumArtUri
                )
            }
        } else {
            null
        }
    }

    val dynamicPalette = rememberAnimatedAlbumPalette(
        context = context,
        track = currentTrack,
        coverBitmap = coverBitmap,
        aura = aura
    )

    val infiniteTransition = rememberInfiniteTransition(label = "player_reactive_pulse")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cover_pulse_ratio"
    )
    val playRingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = CubicBezierEasing(0.18f, 0.85f, 0.32f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "play_halo_phase"
    )

    val coverScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "cover_playing_scale"
    )

    val subtitleTint = if (aura.isDark) dynamicPalette.glowHighlight else dynamicPalette.vibrantPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                if (size.height > 0) containerHeightPx = size.height.toFloat()
            }
            .graphicsLayer {
                translationY = dragOffsetY.value
                val scale = 1f - dragProgress * 0.04f
                scaleX = scale
                scaleY = scale
                alpha = (1f - dragProgress * 0.25f).coerceIn(0.65f, 1f)
                shape = RoundedCornerShape((dragProgress * 28f).dp)
                clip = dragOffsetY.value > 1f
            }
            .background(aura.background)
            .testTag("listen_now_playing_screen")
    ) {
        LiquidGlassAtmosphericBackdrop(
            currentTrack = currentTrack,
            coverBitmap = coverBitmap,
            palette = dynamicPalette,
            isPlaying = isPlaying,
            aura = aura
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .stableStatusBarsPadding()
                .widthIn(max = 600.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Liquid Glass Collapse Orb + Dynamic Glass Tool Capsules
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LiquidGlassSurface(
                    palette = dynamicPalette,
                    isDark = aura.isDark,
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCollapse()
                        }
                        .testTag("listen_collapse_player_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isArabic) "العودة للمكتبة" else "Back to Library",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlassTopToolChip(
                        icon = Icons.Default.Tune,
                        label = if (isArabic) "معادل الصوت" else "EQ",
                        contentDescription = "Equalizer",
                        iconTint = dynamicPalette.vibrantPrimary,
                        labelColor = aura.textPrimary,
                        palette = dynamicPalette,
                        isDark = aura.isDark,
                        testTag = "listen_equalizer_button",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenEqualizer()
                        }
                    )

                    val isTimerActive = sleepTimerRemainingSeconds > 0 || sleepTimerMinutes == -1
                    val timerLabel = when {
                        sleepTimerMinutes == -1 -> if (isArabic) "نهاية" else "End"
                        sleepTimerRemainingSeconds > 0 -> "${sleepTimerRemainingSeconds / 60}m"
                        else -> if (isArabic) "مؤقت" else "Timer"
                    }
                    LiquidGlassTopToolChip(
                        icon = Icons.Default.Timer,
                        label = timerLabel,
                        contentDescription = "Sleep Timer",
                        iconTint = if (isTimerActive) aura.accentGold else dynamicPalette.vibrantSecondary,
                        labelColor = if (isTimerActive) aura.accentGold else aura.textPrimary,
                        palette = dynamicPalette,
                        isDark = aura.isDark,
                        testTag = "listen_sleep_timer_button",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenSleepTimer()
                        }
                    )

                    LiquidGlassTopToolChip(
                        icon = Icons.Default.FolderOpen,
                        label = if (isArabic) "ملفات" else "Files",
                        contentDescription = "Open Audio",
                        iconTint = dynamicPalette.glowHighlight,
                        labelColor = aura.textPrimary,
                        palette = dynamicPalette,
                        isDark = aura.isDark,
                        testTag = "listen_open_files_button",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenFiles()
                        }
                    )
                }
            }

            // Frameless Album Artwork with Rich Multi-Layered Ambient Shadows Behind It
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .aspectRatio(1f)
                    .behindCoverAmbientShadows(
                        palette = dynamicPalette,
                        isPlaying = isPlaying,
                        isDark = aura.isDark,
                        pulseRatio = pulseRatio
                    )
                    .graphicsLayer {
                        scaleX = coverScale
                        scaleY = coverScale
                    }
                    .pointerInput(onCollapse) {
                        val velocityTracker = VelocityTracker()
                        val dismissThresholdPx = 128.dp.toPx()
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            velocityTracker.resetTracking()
                            var prevVirtualY = down.position.y + dragOffsetY.value
                            velocityTracker.addPosition(
                                down.uptimeMillis,
                                Offset(down.position.x, prevVirtualY)
                            )
                            var isDraggingDown = false
                            val touchSlop = viewConfiguration.touchSlop
                            val pointerId = down.id
                            var totalDy = 0f

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (change.changedToUpIgnoreConsumed()) {
                                    break
                                }
                                val currentVirtualY = change.position.y + dragOffsetY.value
                                val dy = currentVirtualY - prevVirtualY
                                prevVirtualY = currentVirtualY
                                velocityTracker.addPosition(
                                    change.uptimeMillis,
                                    Offset(change.position.x, currentVirtualY)
                                )

                                if (!isDraggingDown) {
                                    totalDy += dy
                                    if (totalDy > touchSlop) {
                                        isDraggingDown = true
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else if (totalDy < -touchSlop) {
                                        break
                                    }
                                }

                                if (isDraggingDown) {
                                    change.consume()
                                    val nextOffset = (dragOffsetY.value + dy).coerceAtLeast(0f)
                                    scope.launch { dragOffsetY.snapTo(nextOffset) }
                                }
                            }

                            if (isDraggingDown) {
                                val vy = velocityTracker.calculateVelocity().y
                                val currentOffset = dragOffsetY.value
                                if (currentOffset > dismissThresholdPx || (vy > 620f && currentOffset > 32.dp.toPx())) {
                                    scope.launch {
                                        dragOffsetY.animateTo(
                                            targetValue = containerHeightPx,
                                            animationSpec = tween(
                                                durationMillis = 220,
                                                easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                                            )
                                        )
                                        onCollapse()
                                    }
                                } else {
                                    scope.launch {
                                        dragOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.78f,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val artShape = RoundedCornerShape(28.dp)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = if (isPlaying) 28.dp else 16.dp,
                            shape = artShape,
                            ambientColor = dynamicPalette.deepShadowTint,
                            spotColor = dynamicPalette.vibrantPrimary
                        )
                        .clip(artShape)
                        .testTag("listen_album_art_card"),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        coverBitmap != null && !coverBitmap!!.isRecycled -> {
                            Image(
                                bitmap = coverBitmap!!.asImageBitmap(),
                                contentDescription = currentTrack.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        currentTrack.albumArtUri != null -> {
                            AsyncImage(
                                model = currentTrack.albumArtUri,
                                contentDescription = currentTrack.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = currentTrack.albumCoverRes),
                                contentDescription = currentTrack.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Delicate top glass gloss reflection over upper corner only (no frame, no heavy scrim)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.14f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.24f)
                                    )
                                )
                            )
                    )

                    // Floating Liquid Glass audio badge + live reactive spectrum wave
                    LiquidGlassSurface(
                        palette = dynamicPalette,
                        isDark = true,
                        shape = RoundedCornerShape(16.dp),
                        tintStrength = 0.9f,
                        shadowElevation = 6.dp,
                        borderWidth = 0.8.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReactiveAudioWaveBars(
                                isPlaying = isPlaying,
                                palette = dynamicPalette,
                                barCount = 10,
                                modifier = Modifier
                                    .width(34.dp)
                                    .height(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentTrack.isFromDevice) "MEDIA3 • HI-RES" else "320 KBPS • LOSSLESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.94f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track Info + Glass Favorite & More Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = aura.textPrimary,
                            fontSize = 21.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentTrack.artist} • ${currentTrack.album}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = subtitleTint,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlassSurface(
                        palette = dynamicPalette,
                        isDark = aura.isDark,
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleFavorite()
                            }
                            .testTag("listen_favorite_button")
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) aura.accentGold else aura.textPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }

                    LiquidGlassSurface(
                        palette = dynamicPalette,
                        isDark = aura.isDark,
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenOptions()
                            }
                            .testTag("listen_more_options_button")
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Song Options",
                                tint = aura.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liquid Glass Lyrics Preview Card
            LiquidGlassSurface(
                palette = dynamicPalette,
                isDark = aura.isDark,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenLyrics()
                    }
                    .testTag("listen_lyrics_preview")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            dynamicPalette.vibrantPrimary.copy(alpha = 0.28f),
                                            dynamicPalette.vibrantSecondary.copy(alpha = 0.22f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = subtitleTint,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentTrack.lyricsLines.firstOrNull()
                                ?: (if (isArabic) "انقر لعرض الكلمات ومعلومات المقطع..." else "Tap to view lyrics & metadata..."),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textPrimary.copy(alpha = 0.92f),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(dynamicPalette.vibrantPrimary.copy(alpha = if (aura.isDark) 0.22f else 0.14f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isArabic) "الكلمات" else "LYRICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = subtitleTint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Luminous Liquid Glass Seekbar + Elapsed / Remaining
            Column(modifier = Modifier.fillMaxWidth()) {
                val duration = currentDurationMs.coerceAtLeast(1000f)
                val sliderValue = currentPositionMs.coerceIn(0f, duration)

                Slider(
                    value = sliderValue,
                    onValueChange = onSeekDrag,
                    onValueChangeFinished = onSeekFinish,
                    valueRange = 0f..duration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("listen_seekbar"),
                    thumb = {
                        Box(
                            modifier = Modifier.size(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    dynamicPalette.glowHighlight.copy(alpha = 0.65f),
                                                    dynamicPalette.vibrantPrimary.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                ),
                                                radius = size.width * 0.85f
                                            )
                                        )
                                    }
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        ambientColor = dynamicPalette.vibrantPrimary,
                                        spotColor = dynamicPalette.vibrantPrimary
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White,
                                                dynamicPalette.glowHighlight
                                            )
                                        )
                                    )
                                    .border(1.2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dynamicPalette.vibrantPrimary)
                                )
                            }
                        }
                    },
                    track = { sliderState ->
                        val rangeSpan = (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                            .coerceAtLeast(1f)
                        val fraction = ((sliderState.value - sliderState.valueRange.start) / rangeSpan)
                            .coerceIn(0f, 1f)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        ) {
                            val trackHeight = 6.dp.toPx()
                            val topY = (size.height - trackHeight) * 0.5f
                            val radius = CornerRadius(trackHeight * 0.5f, trackHeight * 0.5f)
                            val inactiveColor = if (aura.isDark) {
                                Color.White.copy(alpha = 0.16f)
                            } else {
                                Color(0xFF0F172A).copy(alpha = 0.14f)
                            }

                            drawRoundRect(
                                color = inactiveColor,
                                topLeft = Offset(0f, topY),
                                size = Size(size.width, trackHeight),
                                cornerRadius = radius
                            )

                            val activeWidth = (size.width * fraction).coerceAtLeast(0f)
                            if (activeWidth > 1f) {
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            dynamicPalette.vibrantPrimary.copy(alpha = 0.28f),
                                            dynamicPalette.vibrantSecondary.copy(alpha = 0.24f)
                                        ),
                                        startX = 0f,
                                        endX = activeWidth
                                    ),
                                    topLeft = Offset(0f, topY - 2.dp.toPx()),
                                    size = Size(activeWidth, trackHeight + 4.dp.toPx()),
                                    cornerRadius = CornerRadius(trackHeight, trackHeight)
                                )
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            dynamicPalette.vibrantPrimary,
                                            dynamicPalette.glowHighlight,
                                            dynamicPalette.vibrantSecondary
                                        ),
                                        startX = 0f,
                                        endX = activeWidth
                                    ),
                                    topLeft = Offset(0f, topY),
                                    size = Size(activeWidth, trackHeight),
                                    cornerRadius = radius
                                )
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentSecTotal = (sliderValue / 1000).toInt()
                    val currentMin = currentSecTotal / 60
                    val currentSec = currentSecTotal % 60
                    val totalSecTotal = (duration / 1000).toInt()
                    val remainingSec = (totalSecTotal - currentSecTotal).coerceAtLeast(0)

                    Text(
                        text = String.format(Locale.US, "%02d:%02d", currentMin, currentSec),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                    ReactiveAudioWaveBars(
                        isPlaying = isPlaying,
                        palette = dynamicPalette,
                        barCount = 16,
                        modifier = Modifier
                            .width(64.dp)
                            .height(12.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "-%02d:%02d", remainingSec / 60, remainingSec % 60),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liquid Glass Transport Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassSurface(
                    palette = dynamicPalette,
                    isDark = aura.isDark,
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipPrevious()
                        }
                        .testTag("listen_skip_previous_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Skip Previous",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Luminous Play / Pause Crystal Orb with Reactive Halo
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .drawBehind {
                            val baseRadius = size.minDimension * 0.44f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        dynamicPalette.vibrantPrimary.copy(alpha = if (isPlaying) 0.56f else 0.30f),
                                        dynamicPalette.vibrantSecondary.copy(alpha = if (isPlaying) 0.28f else 0.14f),
                                        Color.Transparent
                                    ),
                                    radius = size.minDimension * 0.54f
                                )
                            )
                            if (isPlaying) {
                                val ringRadius = baseRadius + (size.minDimension * 0.16f) * playRingPhase
                                val ringAlpha = (1f - playRingPhase) * 0.48f
                                drawCircle(
                                    color = dynamicPalette.glowHighlight.copy(alpha = ringAlpha),
                                    radius = ringRadius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = 18.dp,
                                shape = CircleShape,
                                ambientColor = dynamicPalette.deepShadowTint,
                                spotColor = dynamicPalette.vibrantPrimary
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        dynamicPalette.vibrantPrimary,
                                        dynamicPalette.vibrantSecondary
                                    )
                                )
                            )
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.34f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.16f)
                                    )
                                )
                            )
                            .border(
                                width = 1.25.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.78f),
                                        dynamicPalette.glowHighlight.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.12f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPlayPause()
                            }
                            .testTag("listen_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = dynamicPalette.onPrimaryAccent,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                LiquidGlassSurface(
                    palette = dynamicPalette,
                    isDark = aura.isDark,
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipNext()
                        }
                        .testTag("listen_skip_next_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Next",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Liquid Glass Utility Dock
            LiquidGlassSurface(
                palette = dynamicPalette,
                isDark = aura.isDark,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onDeviceClick)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = subtitleTint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = castDeviceName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenLyrics,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Lyrics",
                                tint = aura.textPrimary.copy(alpha = 0.80f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onCastClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = aura.textPrimary.copy(alpha = 0.80f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenQueue,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Queue",
                                tint = subtitleTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassTopToolChip(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    iconTint: Color,
    labelColor: Color,
    palette: AlbumDynamicPalette,
    isDark: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            palette = palette,
            isDark = isDark,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = labelColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                )
            }
        }
    }
}
