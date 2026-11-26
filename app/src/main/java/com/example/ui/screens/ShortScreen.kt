package com.example.ui.screens

import android.net.Uri
import android.view.LayoutInflater
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.local.MediaPlaybackActivityBus
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MAX_SHORT_VIDEO_DURATION_MS
import com.example.data.model.effectiveDisplayDimensions
import com.example.data.model.isEligibleShortVideo
import com.example.data.preferences.StreakPreferencesRepository
import com.example.data.util.LocalMediaScanner
import com.example.ui.components.formatCleanTitle
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import com.example.ui.viewmodel.StreakViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private enum class ShortFeedFilter(val titleEn: String, val titleAr: String) {
    ALL("All", "الكل"),
    SAVED("Saved", "المحفوظة"),
    LIKED("Liked", "المفضلة")
}

private data class FloatingHeartBurst(
    val id: Long,
    val offset: Offset
)

@OptIn(UnstableApi::class)
@Composable
fun ShortScreen(
    isActiveTab: Boolean,
    streakViewModel: StreakViewModel,
    isArabic: Boolean,
    bottomBarPadding: Dp,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { StreakPreferencesRepository.getInstance(context) }

    var hasStoragePermission by remember {
        mutableStateOf(LocalMediaScanner.hasStorageOrVideoPermission(context))
    }
    var isScanning by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf(ShortFeedFilter.ALL) }

    val allEligibleShorts = remember { mutableStateListOf<LocalDeviceVideo>() }
    val thumbnailBitmaps = remember { mutableStateMapOf<String, ImageBitmap>() }
    var activeThumbJob by remember { mutableStateOf<Job?>(null) }

    var likedVideoKeys by remember { mutableStateOf(prefsRepo.getShortLikedVideoKeys()) }
    var savedVideoKeys by remember { mutableStateOf(prefsRepo.getShortSavedVideoKeys()) }

    val launchThumbExtraction: (List<LocalDeviceVideo>) -> Unit = { targets ->
        val pending = targets.filter {
            it.bitmap == null &&
                thumbnailBitmaps[it.id] == null &&
                !it.uriString.isNullOrBlank()
        }
        if (pending.isNotEmpty()) {
            activeThumbJob?.cancel()
            activeThumbJob = scope.launch {
                LocalMediaScanner.extractThumbnailsParallel(
                    context = context,
                    videos = pending,
                    concurrency = 4,
                    batchSize = 2
                ) { batch ->
                    if (batch.isEmpty()) return@extractThumbnailsParallel
                    val converted = withContext(Dispatchers.Default) {
                        batch.mapValues { (_, bmp) -> bmp.asImageBitmap() }
                    }
                    thumbnailBitmaps.putAll(converted)
                }
            }
        }
    }

    val scanShortsJob: () -> Unit = {
        scope.launch {
            isScanning = true
            activeThumbJob?.cancel()
            val scanned = withContext(Dispatchers.IO) {
                LocalMediaScanner.scanShortVerticalVideos(context)
            }
            val userPicked = allEligibleShorts.filter { it.id.startsWith("picked_") && it.isEligibleShortVideo() }
            val pickedUris = userPicked.mapNotNull { it.uriString }.toSet()
            val combined = (userPicked + scanned.filter { it.uriString !in pickedUris })
                .filter { it.isEligibleShortVideo() }
                .distinctBy { it.uriString ?: it.id }

            val cachedThumbs = withContext(Dispatchers.Default) {
                buildMap<String, ImageBitmap> {
                    for (vid in combined) {
                        val cached = vid.bitmap ?: LocalMediaScanner.getMemoryCachedThumbnail(vid.uriString)
                        if (cached != null) {
                            put(vid.id, cached.asImageBitmap())
                        }
                    }
                }
            }
            thumbnailBitmaps.putAll(cachedThumbs)
            allEligibleShorts.clear()
            allEligibleShorts.addAll(combined)
            isScanning = false
            launchThumbExtraction(combined)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val granted = LocalMediaScanner.hasStorageOrVideoPermission(context)
        hasStoragePermission = granted
        if (granted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scanShortsJob()
        }
    }

    val visualMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val parsed = withContext(Dispatchers.IO) {
                    uris.map { uri -> LocalMediaScanner.parseUriToLocalVideo(context, uri) }
                }
                val eligible = parsed.filter { it.isEligibleShortVideo() }
                val rejectedCount = parsed.size - eligible.size
                var addedCount = 0
                for (vid in eligible) {
                    if (allEligibleShorts.none { it.uriString == vid.uriString }) {
                        allEligibleShorts.add(0, vid)
                        addedCount++
                    }
                }
                if (addedCount > 0) {
                    launchThumbExtraction(allEligibleShorts.toList())
                    activeFilter = ShortFeedFilter.ALL
                }
                if (rejectedCount > 0) {
                    onShowMessage(
                        if (isArabic) {
                            "يتم عرض الفيديوهات الطولية التي لا تتعدى 30 دقيقة فقط (تم استبعاد $rejectedCount)"
                        } else {
                            "Only vertical videos (30 min max) are supported ($rejectedCount skipped)"
                        }
                    )
                } else if (addedCount > 0) {
                    onShowMessage(
                        if (isArabic) "تمت إضافة $addedCount فيديو طولي" else "Added $addedCount vertical video(s)"
                    )
                }
            }
        }
    }

    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission && allEligibleShorts.isEmpty()) {
            scanShortsJob()
        }
    }

    // Filter feed according to selected chip (All / Saved / Liked), preserving strict vertical <= 30m rule
    val displayedVideos = remember(allEligibleShorts.toList(), activeFilter, savedVideoKeys, likedVideoKeys) {
        val strictShorts = allEligibleShorts.filter { it.isEligibleShortVideo() }
        when (activeFilter) {
            ShortFeedFilter.ALL -> strictShorts
            ShortFeedFilter.SAVED -> {
                val savedSet = savedVideoKeys.toSet()
                strictShorts.filter { vid ->
                    savedSet.contains(prefsRepo.canonicalVideoKey(vid.id, vid.uriString))
                }
            }
            ShortFeedFilter.LIKED -> {
                strictShorts.filter { vid ->
                    likedVideoKeys.contains(prefsRepo.canonicalVideoKey(vid.id, vid.uriString))
                }
            }
        }
    }

    val savedCount = remember(allEligibleShorts.toList(), savedVideoKeys) {
        val savedSet = savedVideoKeys.toSet()
        allEligibleShorts.count { vid ->
            vid.isEligibleShortVideo() && savedSet.contains(prefsRepo.canonicalVideoKey(vid.id, vid.uriString))
        }
    }
    val likedCount = remember(allEligibleShorts.toList(), likedVideoKeys) {
        allEligibleShorts.count { vid ->
            vid.isEligibleShortVideo() && likedVideoKeys.contains(prefsRepo.canonicalVideoKey(vid.id, vid.uriString))
        }
    }

    // Lifecycle foreground/background state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLifecycleResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isLifecycleResumed = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> isLifecycleResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Single shared ExoPlayer for the Short Reels feed (prevents codec exhaustion during fast swiping)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    var isMuted by remember { mutableStateOf(false) }
    var isUserPaused by remember { mutableStateOf(false) }
    var isLongPressPaused by remember { mutableStateOf(false) }
    var isPlayerActuallyPlaying by remember { mutableStateOf(false) }
    var hasRenderedFirstFrame by remember { mutableStateOf(false) }
    var activeLoadedUri by remember { mutableStateOf<String?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var currentDurationMs by remember { mutableLongStateOf(0L) }
    var isScrubbing by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayerActuallyPlaying = isPlaying
                MediaPlaybackActivityBus.setVideoPlaying(isPlaying)
            }

            override fun onRenderedFirstFrame() {
                hasRenderedFirstFrame = true
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > MAX_SHORT_VIDEO_DURATION_MS) {
                        val badUri = activeLoadedUri
                        if (!badUri.isNullOrBlank()) {
                            allEligibleShorts.removeAll { it.uriString == badUri }
                            activeLoadedUri = null
                        }
                        exoPlayer.stop()
                    } else if (dur > 0L) {
                        currentDurationMs = dur
                    }
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    val badUri = activeLoadedUri ?: return
                    @Suppress("DEPRECATION")
                    val rot = ((videoSize.unappliedRotationDegrees % 360) + 360) % 360
                    val dispW = if (rot == 90 || rot == 270) videoSize.height else videoSize.width
                    val dispH = if (rot == 90 || rot == 270) videoSize.width else videoSize.height
                    if (dispH <= dispW) {
                        allEligibleShorts.removeAll { it.uriString == badUri }
                        activeLoadedUri = null
                        exoPlayer.stop()
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            MediaPlaybackActivityBus.setVideoPlaying(false)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { displayedVideos.size }
    )

    // Keep settled page in bounds when filter changes
    LaunchedEffect(displayedVideos.size, activeFilter) {
        if (displayedVideos.isNotEmpty() && pagerState.currentPage >= displayedVideos.size) {
            pagerState.scrollToPage(0)
        }
    }

    val settledVideo = displayedVideos.getOrNull(pagerState.settledPage)
    val shouldPlayNow = isActiveTab && isLifecycleResumed && !isUserPaused && !isLongPressPaused && settledVideo != null

    // Reset manual pause when swiping to a new reel
    LaunchedEffect(pagerState.settledPage, settledVideo?.id) {
        isUserPaused = false
        isLongPressPaused = false
    }

    // Bind settled video URI to the shared ExoPlayer
    LaunchedEffect(settledVideo?.uriString, isActiveTab) {
        val targetUri = settledVideo?.uriString
        if (!isActiveTab || targetUri.isNullOrBlank()) {
            exoPlayer.pause()
            if (!isActiveTab) {
                hasRenderedFirstFrame = false
                MediaPlaybackActivityBus.setVideoPlaying(false)
            }
            return@LaunchedEffect
        }
        if (activeLoadedUri != targetUri) {
            hasRenderedFirstFrame = false
            currentPositionMs = 0L
            currentDurationMs = settledVideo.durationMs.coerceAtLeast(0L)
            activeLoadedUri = targetUri
            val mediaItem = ExoMediaItem.fromUri(Uri.parse(targetUri))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = shouldPlayNow
        }
    }

    LaunchedEffect(shouldPlayNow) {
        if (shouldPlayNow && !activeLoadedUri.isNullOrBlank()) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
        }
    }

    // High-frequency position polling while visible
    LaunchedEffect(isActiveTab, settledVideo?.uriString, isScrubbing) {
        while (isActive) {
            if (isActiveTab && !isScrubbing && settledVideo != null) {
                val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration.takeIf { it > 0L } ?: settledVideo.durationMs
                currentPositionMs = pos
                if (dur > 0L) currentDurationMs = dur
            }
            delay(150L)
        }
    }

    // Genuine Watch XP & daily streak timer (1 tick per second while playing)
    LaunchedEffect(isActiveTab, isPlayerActuallyPlaying, isScrubbing) {
        if (isActiveTab && isPlayerActuallyPlaying && !isScrubbing) {
            while (isActive) {
                delay(1000L)
                if (isActiveTab && exoPlayer.isPlaying && !isScrubbing) {
                    streakViewModel.tickGenuineWatchTime(seconds = 1, isSeeking = false)
                }
            }
        }
    }

    // Center Play/Pause indicator flash
    var showPlayPausePulse by remember { mutableStateOf(false) }
    var pulseIsPauseIcon by remember { mutableStateOf(false) }
    LaunchedEffect(showPlayPausePulse, isUserPaused) {
        if (showPlayPausePulse && !isUserPaused) {
            delay(650L)
            showPlayPausePulse = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("short_screen_root")
    ) {
        when {
            !hasStoragePermission && allEligibleShorts.isEmpty() -> {
                ShortPermissionEmptyState(
                    isArabic = isArabic,
                    bottomBarPadding = bottomBarPadding,
                    onGrantPermission = {
                        permissionLauncher.launch(LocalMediaScanner.getRequiredPermissions())
                    },
                    onPickVideos = {
                        visualMediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )
            }

            isScanning && allEligibleShorts.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF2D55),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isArabic) {
                            "جاري البحث عن الفيديوهات الطولية (≤ 30 دقيقة)..."
                        } else {
                            "Scanning vertical videos (≤ 30 min)..."
                        },
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            displayedVideos.isEmpty() -> {
                ShortNoVideosEmptyState(
                    isArabic = isArabic,
                    activeFilter = activeFilter,
                    bottomBarPadding = bottomBarPadding,
                    onSwitchToAll = { activeFilter = ShortFeedFilter.ALL },
                    onRescan = { scanShortsJob() },
                    onPickVideos = {
                        visualMediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )
            }

            else -> {
                VerticalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    key = { index -> displayedVideos[index].uriString ?: displayedVideos[index].id },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("short_vertical_pager")
                ) { pageIndex ->
                    val video = displayedVideos[pageIndex]
                    val videoKey = prefsRepo.canonicalVideoKey(video.id, video.uriString)
                    val isLiked = likedVideoKeys.contains(videoKey)
                    val isSaved = savedVideoKeys.contains(videoKey)
                    val isSettledPage = pageIndex == pagerState.settledPage
                    val thumbBitmap = thumbnailBitmaps[video.id]
                        ?: video.bitmap?.asImageBitmap()
                        ?: LocalMediaScanner.getMemoryCachedThumbnail(video.uriString)?.asImageBitmap()

                    ShortReelPage(
                        video = video,
                        thumbBitmap = thumbBitmap,
                        exoPlayer = exoPlayer,
                        isSettledActivePage = isSettledPage && isActiveTab,
                        hasRenderedFirstFrame = hasRenderedFirstFrame && activeLoadedUri == video.uriString,
                        isPlaying = isSettledPage && isPlayerActuallyPlaying,
                        isUserPaused = isSettledPage && isUserPaused,
                        isLongPressPreview = isSettledPage && isLongPressPaused,
                        showPlayPauseIndicator = isSettledPage && (isUserPaused || showPlayPausePulse),
                        playPauseIsPaused = isSettledPage && isUserPaused,
                        isLiked = isLiked,
                        isSaved = isSaved,
                        isArabic = isArabic,
                        currentPositionMs = if (isSettledPage) currentPositionMs else 0L,
                        currentDurationMs = if (isSettledPage && currentDurationMs > 0L) currentDurationMs else video.durationMs,
                        bottomBarPadding = bottomBarPadding,
                        onSingleTap = {
                            if (isSettledPage) {
                                isUserPaused = !isUserPaused
                                pulseIsPauseIcon = isUserPaused
                                showPlayPausePulse = true
                            }
                        },
                        onDoubleTapLike = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            likedVideoKeys = prefsRepo.setShortLiked(video.id, video.uriString, liked = true)
                            streakViewModel.ensureShortLiked(videoKey)
                        },
                        onLongPressStart = {
                            if (isSettledPage) {
                                isLongPressPaused = true
                            }
                        },
                        onLongPressEnd = {
                            if (isSettledPage) {
                                isLongPressPaused = false
                            }
                        },
                        onToggleLike = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            likedVideoKeys = prefsRepo.toggleShortLike(video.id, video.uriString)
                        },
                        onToggleSave = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val updated = prefsRepo.toggleShortSaved(video.id, video.uriString)
                            savedVideoKeys = updated
                            val nowSaved = updated.contains(videoKey)
                            onShowMessage(
                                if (isArabic) {
                                    if (nowSaved) "تم حفظ الفيديو في المحفوظات ✓" else "تمت الإزالة من المحفوظات"
                                } else {
                                    if (nowSaved) "Saved to Short collection ✓" else "Removed from Saved"
                                }
                            )
                        },
                        onShare = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val shared = LocalMediaScanner.shareVideo(context, video)
                            if (!shared) {
                                onShowMessage(
                                    if (isArabic) "تعذر مشاركة هذا الفيديو" else "Unable to share this video"
                                )
                            }
                        },
                        onScrubStart = { isScrubbing = true },
                        onScrubProgress = { fraction ->
                            val dur = currentDurationMs.takeIf { it > 0L } ?: video.durationMs
                            if (dur > 0L) {
                                currentPositionMs = (dur * fraction.coerceIn(0f, 1f)).toLong()
                            }
                        },
                        onScrubEnd = { fraction ->
                            val dur = currentDurationMs.takeIf { it > 0L } ?: video.durationMs
                            if (dur > 0L) {
                                val targetMs = (dur * fraction.coerceIn(0f, 1f)).toLong()
                                currentPositionMs = targetMs
                                exoPlayer.seekTo(targetMs)
                            }
                            isScrubbing = false
                        }
                    )
                }
            }
        }

        // Top Reels Floating Header (fades smoothly during clean long-press preview)
        AnimatedVisibility(
            visible = !isLongPressPaused,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ShortTopOverlayBar(
                isArabic = isArabic,
                totalCount = allEligibleShorts.count { it.isEligibleShortVideo() },
                savedCount = savedCount,
                likedCount = likedCount,
                activeFilter = activeFilter,
                isMuted = isMuted,
                isScanning = isScanning,
                onSelectFilter = { activeFilter = it },
                onToggleMute = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isMuted = !isMuted
                },
                onPickVideo = {
                    visualMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onRescan = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (hasStoragePermission) {
                        scanShortsJob()
                    } else {
                        permissionLauncher.launch(LocalMediaScanner.getRequiredPermissions())
                    }
                }
            )
        }
    }
}

@Composable
private fun ShortTopOverlayBar(
    isArabic: Boolean,
    totalCount: Int,
    savedCount: Int,
    likedCount: Int,
    activeFilter: ShortFeedFilter,
    isMuted: Boolean,
    isScanning: Boolean,
    onSelectFilter: (ShortFeedFilter) -> Unit,
    onToggleMute: () -> Unit,
    onPickVideo: () -> Unit,
    onRescan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.72f),
                        Color.Black.copy(alpha = 0.28f),
                        Color.Transparent
                    )
                )
            )
            .stableStatusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF2D55).copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color(0xFFFF2D55).copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Short",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 6f
                                )
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "$totalCount",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isArabic) "طولي • ≤ 30 دقيقة" else "Vertical • ≤ 30m",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TopGlassCircleIconButton(
                    icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = if (isMuted) Color(0xFFFF6B6B) else Color.White,
                    onClick = onToggleMute,
                    modifier = Modifier.testTag("short_mute_button")
                )
                TopGlassCircleIconButton(
                    icon = Icons.Filled.Add,
                    contentDescription = if (isArabic) "إضافة فيديو" else "Pick vertical video",
                    tint = Color.White,
                    onClick = onPickVideo,
                    modifier = Modifier.testTag("short_pick_button")
                )
                TopGlassCircleIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = if (isArabic) "تحديث" else "Rescan",
                    tint = if (isScanning) Color(0xFF00F2FE) else Color.White,
                    onClick = onRescan,
                    modifier = Modifier.testTag("short_rescan_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortFeedFilter.entries.forEach { filter ->
                val selected = activeFilter == filter
                val count = when (filter) {
                    ShortFeedFilter.ALL -> totalCount
                    ShortFeedFilter.SAVED -> savedCount
                    ShortFeedFilter.LIKED -> likedCount
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) Color.White else Color.Black.copy(alpha = 0.42f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.24f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectFilter(filter) }
                        .testTag("short_filter_${filter.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = if (isArabic) filter.titleAr else filter.titleEn,
                            color = if (selected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (count > 0) {
                            Text(
                                text = "$count",
                                color = if (selected) Color.Black.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.72f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopGlassCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.44f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
        modifier = modifier.size(38.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ShortReelPage(
    video: LocalDeviceVideo,
    thumbBitmap: ImageBitmap?,
    exoPlayer: ExoPlayer,
    isSettledActivePage: Boolean,
    hasRenderedFirstFrame: Boolean,
    isPlaying: Boolean,
    isUserPaused: Boolean,
    isLongPressPreview: Boolean,
    showPlayPauseIndicator: Boolean,
    playPauseIsPaused: Boolean,
    isLiked: Boolean,
    isSaved: Boolean,
    isArabic: Boolean,
    currentPositionMs: Long,
    currentDurationMs: Long,
    bottomBarPadding: Dp,
    onSingleTap: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onScrubStart: () -> Unit,
    onScrubProgress: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit
) {
    val heartBursts = remember { mutableStateListOf<FloatingHeartBurst>() }
    val latestOnLongPressEnd by rememberUpdatedState(onLongPressEnd)
    var isHoldingLongPress by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Instant Poster Thumbnail Layer (prevents black flicker while swiping)
        if (thumbBitmap != null) {
            Image(
                bitmap = thumbBitmap,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0E1118),
                                Color(0xFF06070B),
                                Color(0xFF120B18)
                            )
                        )
                    )
            )
        }

        // 2. Active ExoPlayer TextureView Layer (only bound to the currently settled page)
        if (isSettledActivePage) {
            val videoAlpha by animateFloatAsState(
                targetValue = if (hasRenderedFirstFrame) 1f else 0f,
                animationSpec = tween(durationMillis = 180),
                label = "reel_video_alpha"
            )
            key(video.uriString ?: video.id) {
                AndroidView(
                    factory = { ctx ->
                        (LayoutInflater.from(ctx).inflate(
                            R.layout.exo_texture_player_view,
                            null,
                            false
                        ) as PlayerView).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            player = exoPlayer
                        }
                    },
                    update = { playerView ->
                        if (playerView.player !== exoPlayer) {
                            playerView.player = exoPlayer
                        }
                    },
                    onRelease = { playerView ->
                        playerView.player = null
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(videoAlpha)
                )
            }
        }

        // 3. Instagram Reels Gesture Overlay (Single Tap = Play/Pause, Double Tap = Like, Long Press = Clean Preview)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(video.id, isSettledActivePage) {
                    detectTapGestures(
                        onTap = { onSingleTap() },
                        onDoubleTap = { tapOffset ->
                            heartBursts.add(
                                FloatingHeartBurst(
                                    id = System.nanoTime(),
                                    offset = tapOffset
                                )
                            )
                            onDoubleTapLike()
                        },
                        onLongPress = {
                            isHoldingLongPress = true
                            onLongPressStart()
                        },
                        onPress = {
                            tryAwaitRelease()
                            if (isHoldingLongPress) {
                                isHoldingLongPress = false
                                latestOnLongPressEnd()
                            }
                        }
                    )
                }
        )

        // 4. Double-Tap Floating Heart Bursts
        heartBursts.forEach { burst ->
            key(burst.id) {
                DoubleTapHeartAnimation(
                    offset = burst.offset,
                    onFinished = {
                        heartBursts.removeAll { it.id == burst.id }
                    }
                )
            }
        }

        // 5. Center Play / Pause Feedback Badge
        AnimatedVisibility(
            visible = showPlayPauseIndicator && !isLongPressPreview,
            enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.72f, animationSpec = spring(dampingRatio = 0.65f)),
            exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.86f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.56f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (playPauseIsPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (playPauseIsPaused) "Play" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // 6. Bottom Reels Metadata, Scrubber, and Right-Side Action Bar (fades out on clean long-press)
        AnimatedVisibility(
            visible = !isLongPressPreview,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val (effW, effH) = video.effectiveDisplayDimensions()
            val cleanTitle = remember(video.title) { formatCleanTitle(video.title, hideExt = true) }
            val durationLabel = remember(currentDurationMs, video.durationText) {
                if (currentDurationMs > 0L) LocalMediaScanner.formatDuration(currentDurationMs) else video.durationText
            }
            val dimsLabel = remember(effW, effH) {
                if (effW > 0 && effH > 0) "${effW}×${effH}" else "9:16"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.35f to Color.Black.copy(alpha = 0.42f),
                                0.75f to Color.Black.copy(alpha = 0.78f),
                                1.0f to Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .padding(bottom = bottomBarPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Use consistent Reels layout: Info on Start, Action Stack on End (or Right side)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 12.dp, top = 40.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Left/Bottom Metadata Column (Instagram Reels Style)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 14.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Creator / Folder Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(0xFFF58529),
                                                        Color(0xFFDD2A7B),
                                                        Color(0xFF8134AF)
                                                    )
                                                )
                                            )
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF141824)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Folder,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = "@${video.folderName.replace(" ", "_").lowercase()}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                shadow = Shadow(
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    offset = Offset(0f, 1f),
                                                    blurRadius = 4f
                                                )
                                            )
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ReelTagPill(text = "9:16")
                                            ReelTagPill(text = "$durationLabel • $dimsLabel")
                                        }
                                    }
                                }

                                // Expandable Reel Caption / Title
                                var expandedTitle by remember(video.id) { mutableStateOf(false) }
                                Text(
                                    text = cleanTitle,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 19.sp,
                                    maxLines = if (expandedTitle) 5 else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.7f),
                                            offset = Offset(0f, 1f),
                                            blurRadius = 5f
                                        )
                                    ),
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { expandedTitle = !expandedTitle }
                                )

                                // Audio / Sound Ticker Pill
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.Black.copy(alpha = 0.40f),
                                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.16f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.GraphicEq,
                                            contentDescription = null,
                                            tint = if (isPlaying) Color(0xFF00F2FE) else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isArabic) {
                                                "الصوت الأصلي • ${video.folderName}"
                                            } else {
                                                "Original Audio • ${video.folderName}"
                                            },
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Right-Side Instagram Reels Action Bar (Like, Save, Share + Rotating Vinyl)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                // 1. Like Button (زر الإعجاب)
                                ReelsActionButton(
                                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    label = if (isArabic) {
                                        if (isLiked) "أعجبني" else "إعجاب"
                                    } else {
                                        if (isLiked) "Liked" else "Like"
                                    },
                                    tint = if (isLiked) Color(0xFFFF2D55) else Color.White,
                                    isActive = isLiked,
                                    onClick = onToggleLike,
                                    testTag = "short_like_button"
                                )

                                // 2. Save Button (زر الحفظ)
                                ReelsActionButton(
                                    icon = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    label = if (isArabic) {
                                        if (isSaved) "محفوظ" else "حفظ"
                                    } else {
                                        if (isSaved) "Saved" else "Save"
                                    },
                                    tint = if (isSaved) Color(0xFFFFD60A) else Color.White,
                                    isActive = isSaved,
                                    onClick = onToggleSave,
                                    testTag = "short_save_button"
                                )

                                // 3. Share Button (زر المشاركة)
                                ReelsActionButton(
                                    icon = Icons.Filled.Share,
                                    label = if (isArabic) "مشاركة" else "Share",
                                    tint = Color.White,
                                    isActive = false,
                                    onClick = onShare,
                                    testTag = "short_share_button"
                                )

                                // 4. Rotating Reels Audio Disc
                                ReelsRotatingAudioDisc(
                                    thumbBitmap = thumbBitmap,
                                    isSpinning = isPlaying && !isUserPaused
                                )
                            }
                        }
                    }

                    // Slim Instagram Reels Interactive Scrubber above the floating bottom bar
                    ReelsProgressScrubber(
                        positionMs = currentPositionMs,
                        durationMs = currentDurationMs,
                        onScrubStart = onScrubStart,
                        onScrubProgress = onScrubProgress,
                        onScrubEnd = onScrubEnd
                    )
                }
            }
        }
    }
}

@Composable
private fun ReelTagPill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ReelsActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    var pulseTrigger by remember { mutableIntStateOf(0) }
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(pulseTrigger, isActive) {
        if (pulseTrigger > 0) {
            scaleAnim.snapTo(0.76f)
            scaleAnim.animateTo(
                targetValue = 1.24f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
            )
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pulseTrigger++
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(scaleAnim.value)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.34f))
                .border(
                    width = 0.8.dp,
                    color = if (isActive) tint.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.18f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) tint else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.85f),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
private fun ReelsRotatingAudioDisc(
    thumbBitmap: ImageBitmap?,
    isSpinning: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reels_disc_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )
    val effectiveAngle = if (isSpinning) angle else 0f

    Box(
        modifier = Modifier
            .size(36.dp)
            .rotate(effectiveAngle)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A1D28))
            .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (thumbBitmap != null) {
            Image(
                bitmap = thumbBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DoubleTapHeartAnimation(
    offset: Offset,
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(1f) }
    val floatY = remember { Animatable(0f) }
    val density = LocalDensity.current
    val heartSizePx = with(density) { 96.dp.toPx() }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(120)
        )
        delay(180L)
        launch {
            floatY.animateTo(-120f, animationSpec = tween(360, easing = FastOutSlowInEasing))
        }
        alpha.animateTo(0f, animationSpec = tween(360))
        onFinished()
    }

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = Color(0xFFFF2D55),
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (offset.x - heartSizePx / 2f).roundToInt(),
                    y = (offset.y - heartSizePx / 2f + floatY.value).roundToInt()
                )
            }
            .size(96.dp)
            .scale(scale.value)
            .alpha(alpha.value)
    )
}

@Composable
private fun ReelsProgressScrubber(
    positionMs: Long,
    durationMs: Long,
    onScrubStart: () -> Unit,
    onScrubProgress: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var barWidthPx by remember { mutableFloatStateOf(1f) }

    val progress = if (isDragging) {
        dragFraction
    } else if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 2.8.dp,
        animationSpec = tween(150),
        label = "reels_scrubber_height"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isDragging && durationMs > 0L) {
            val previewMs = (durationMs * dragFraction).toLong()
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${LocalMediaScanner.formatDuration(previewMs)} / ${LocalMediaScanner.formatDuration(durationMs)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .onSizeChanged { size ->
                        if (size.width > 0) barWidthPx = size.width.toFloat()
                    }
                    .pointerInput(durationMs) {
                        detectHorizontalDragGestures(
                            onDragStart = { startOffset ->
                                isDragging = true
                                dragFraction = (startOffset.x / barWidthPx).coerceIn(0f, 1f)
                                onScrubStart()
                                onScrubProgress(dragFraction)
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                dragFraction = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                                onScrubProgress(dragFraction)
                            },
                            onDragEnd = {
                                isDragging = false
                                onScrubEnd(dragFraction)
                            },
                            onDragCancel = {
                                isDragging = false
                                onScrubEnd(dragFraction)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight)
                ) {
                    val radius = CornerRadius(size.height / 2f, size.height / 2f)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.24f),
                        cornerRadius = radius
                    )
                    val activeW = (size.width * progress).coerceIn(0f, size.width)
                    if (activeW > 0f) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.92f),
                                    Color(0xFFFF2D55)
                                )
                            ),
                            size = Size(activeW, size.height),
                            cornerRadius = radius
                        )
                    }
                    if (isDragging) {
                        drawCircle(
                            color = Color.White,
                            radius = size.height * 1.15f,
                            center = Offset(activeW, size.height / 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortPermissionEmptyState(
    isArabic: Boolean,
    bottomBarPadding: Dp,
    onGrantPermission: () -> Unit,
    onPickVideos: () -> Unit
) {
    val aura = MaterialTheme.aura
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(bottom = bottomBarPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFF2D55).copy(alpha = 0.16f),
            border = BorderStroke(1.dp, Color(0xFFFF2D55).copy(alpha = 0.45f)),
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isArabic) "مشغل Short الطولي" else "Short Vertical Player",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isArabic) {
                "اسمح بالوصول إلى فيديوهات جهازك لتشغيل الفيديوهات المصورة طولياً والتي لا تتعدى مدتها 30 دقيقة بأسلوب الريلز."
            } else {
                "Grant video library access to play your vertical videos (30 minutes or shorter) in an Instagram Reels-style feed."
            },
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGrantPermission,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF2D55),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("short_grant_permission_button")
        ) {
            Text(
                text = if (isArabic) "السماح بالوصول للفيديوهات" else "Allow Video Access",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onPickVideos,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, aura.borderSubtle),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 13.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("short_pick_from_gallery_button")
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoLibrary,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isArabic) "اختيار فيديو طولي من المعرض" else "Pick Vertical Video",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ShortNoVideosEmptyState(
    isArabic: Boolean,
    activeFilter: ShortFeedFilter,
    bottomBarPadding: Dp,
    onSwitchToAll: () -> Unit,
    onRescan: () -> Unit,
    onPickVideos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(bottom = bottomBarPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (activeFilter) {
                        ShortFeedFilter.SAVED -> Icons.Filled.BookmarkBorder
                        ShortFeedFilter.LIKED -> Icons.Filled.FavoriteBorder
                        ShortFeedFilter.ALL -> Icons.Filled.Timer
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = when (activeFilter) {
                ShortFeedFilter.SAVED -> if (isArabic) "لا توجد فيديوهات محفوظة بعد" else "No Saved Shorts Yet"
                ShortFeedFilter.LIKED -> if (isArabic) "لا توجد فيديوهات معجب بها بعد" else "No Liked Shorts Yet"
                ShortFeedFilter.ALL -> if (isArabic) "لا توجد فيديوهات طولية مطابقة" else "No Eligible Vertical Videos"
            },
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when (activeFilter) {
                ShortFeedFilter.SAVED -> if (isArabic) {
                    "اضغط على زر الحفظ في أي فيديو طولي لإضافته هنا."
                } else {
                    "Tap the Save button on any Short to bookmark it here."
                }
                ShortFeedFilter.LIKED -> if (isArabic) {
                    "اضغط مرتين أو استخدم زر الإعجاب لإضافة الفيديوهات هنا."
                } else {
                    "Double-tap or press Like on any Short to collect it here."
                }
                ShortFeedFilter.ALL -> if (isArabic) {
                    "هذا المشغل يعرض فقط الفيديوهات المصورة طولياً والتي لا يتعدى وقتها 30 دقيقة."
                } else {
                    "This player strictly shows vertically filmed videos that are 30 minutes or shorter."
                }
            },
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        if (activeFilter != ShortFeedFilter.ALL) {
            Button(
                onClick = onSwitchToAll,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 13.dp)
            ) {
                Text(
                    text = if (isArabic) "عرض كل الفيديوهات الطولية" else "Show All Vertical Videos",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPickVideos,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF2D55),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "اختيار فيديو" else "Pick Video",
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onRescan,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "إعادة الفحص" else "Rescan",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
