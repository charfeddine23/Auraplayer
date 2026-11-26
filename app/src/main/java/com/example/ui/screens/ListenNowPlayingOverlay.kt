package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.GraphicEq
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseViolet
import com.example.ui.theme.TextDark
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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

    val infiniteTransition = rememberInfiniteTransition(label = "audio_glow")
    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_glow_alpha"
    )

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
        val artAlpha = if (aura.isDark) 0.35f else 0.18f
        when {
            coverBitmap != null && !coverBitmap!!.isRecycled -> {
                Image(
                    bitmap = coverBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = artAlpha
                )
            }
            currentTrack.albumArtUri != null -> {
                AsyncImage(
                    model = currentTrack.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = artAlpha
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = currentTrack.albumCoverRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = artAlpha
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (aura.isDark) {
                            listOf(
                                aura.background.copy(alpha = 0.68f),
                                aura.background.copy(alpha = 0.87f),
                                aura.background.copy(alpha = 0.96f),
                                aura.background
                            )
                        } else {
                            listOf(
                                aura.background.copy(alpha = 0.85f),
                                aura.background.copy(alpha = 0.92f),
                                aura.background.copy(alpha = 0.98f),
                                aura.background
                            )
                        }
                    )
                )
        )
        AuraAtmosphericLayer()

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
            // Top bar with collapse button + quick tools
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("listen_collapse_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isArabic) "العودة للمكتبة" else "Back to Library",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = aura.accentCyanSoft,
                            border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onOpenEqualizer()
                                }
                                .testTag("listen_equalizer_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Equalizer",
                                    tint = aura.accentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "معادل الصوت" else "EQ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        val isTimerActive = sleepTimerRemainingSeconds > 0 || sleepTimerMinutes == -1
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTimerActive) aura.accentGoldBg else aura.unselectedChipBg,
                            border = BorderStroke(1.dp, if (isTimerActive) aura.accentGold else aura.borderSubtle),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onOpenSleepTimer()
                                }
                                .testTag("listen_sleep_timer_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Sleep Timer",
                                    tint = if (isTimerActive) aura.accentGold else aura.accentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val timerLabel = when {
                                    sleepTimerMinutes == -1 -> if (isArabic) "نهاية" else "End"
                                    sleepTimerRemainingSeconds > 0 -> "${sleepTimerRemainingSeconds / 60}m"
                                    else -> if (isArabic) "مؤقت" else "Timer"
                                }
                                Text(
                                    text = timerLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isTimerActive) aura.accentGold else aura.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = aura.accentCyanSoft,
                            border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onOpenFiles()
                                }
                                .testTag("listen_open_files_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open Audio",
                                    tint = aura.accentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "ملفات" else "Files",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Album Artwork Card (press & drag down smoothly returns to song list)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PulseCyan.copy(alpha = neonGlowAlpha * 0.4f),
                                    Color(0x2210B981),
                                    Color.Transparent
                                ),
                                radius = size.minDimension * 0.75f
                            )
                        )
                    }
                    .border(
                        border = BorderStroke(
                            width = 1.8.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    PulseCyan,
                                    Color(0xFF10B981),
                                    PulseViolet,
                                    PulseCyan
                                )
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(Color(0xD9121826))
                    .testTag("listen_album_art_card"),
                contentAlignment = Alignment.Center
            ) {
                when {
                    coverBitmap != null && !coverBitmap!!.isRecycled -> {
                        Image(
                            bitmap = coverBitmap!!.asImageBitmap(),
                            contentDescription = currentTrack.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    currentTrack.albumArtUri != null -> {
                        AsyncImage(
                            model = currentTrack.albumArtUri,
                            contentDescription = currentTrack.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = currentTrack.albumCoverRes),
                            contentDescription = currentTrack.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x44000000),
                                    Color(0x99000000)
                                )
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC000000),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = PulseCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentTrack.isFromDevice) "MEDIA3 • LOCAL STORAGE" else "320 KBPS • LOSSLESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track Info + Favorite + Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = aura.textPrimary,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${currentTrack.artist} • ${currentTrack.album}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = aura.accentCyan,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFavorite()
                        },
                        modifier = Modifier.testTag("listen_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) aura.accentGold else aura.textSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenOptions()
                        },
                        modifier = Modifier.testTag("listen_more_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Song Options",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lyrics Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenLyrics()
                    }
                    .testTag("listen_lyrics_preview")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(aura.accentCyanSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = aura.accentCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentTrack.lyricsLines.firstOrNull()
                                ?: (if (isArabic) "انقر لعرض الكلمات ومعلومات المقطع..." else "Tap to view lyrics & metadata..."),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textPrimary,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = aura.surfaceSubtle
                    ) {
                        Text(
                            text = if (isArabic) "الكلمات" else "LYRICS",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seekbar + Elapsed / Remaining
            Column(modifier = Modifier.fillMaxWidth()) {
                val duration = currentDurationMs.coerceAtLeast(1000f)
                val sliderValue = currentPositionMs.coerceIn(0f, duration)

                Slider(
                    value = sliderValue,
                    onValueChange = onSeekDrag,
                    onValueChangeFinished = onSeekFinish,
                    valueRange = 0f..duration,
                    colors = SliderDefaults.colors(
                        thumbColor = aura.accentCyan,
                        activeTrackColor = aura.accentCyan,
                        inactiveTrackColor = aura.progressTrack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .testTag("listen_seekbar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = String.format(Locale.US, "-%02d:%02d", remainingSec / 60, remainingSec % 60),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSkipPrevious()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("listen_skip_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Skip Previous",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        aura.accentCyan.copy(alpha = 0.5f),
                                        aura.accentEmerald.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.9f
                                )
                            )
                        }
                        .background(
                            Brush.linearGradient(
                                listOf(aura.accentCyan, aura.accentEmerald)
                            )
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
                        tint = if (aura.isDark) TextDark else Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSkipNext()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("listen_skip_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Next",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Utility Bar (no bottom fade glass overlay)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onDeviceClick)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = aura.accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = castDeviceName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenLyrics,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Lyrics",
                                tint = aura.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onCastClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = aura.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenQueue,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Queue",
                                tint = aura.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
