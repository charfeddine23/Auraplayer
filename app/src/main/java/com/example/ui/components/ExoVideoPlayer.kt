package com.example.ui.components

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.LayoutInflater
import android.view.TextureView
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.example.ui.theme.stableStatusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.model.MediaItem
import com.example.data.model.PlayerSeekbarStyle
import com.example.data.model.VaultProgressionRegistry
import com.example.data.util.LocalMediaScanner
import com.example.data.util.SecurityUtils
import com.example.ui.theme.Charcoal950
import com.example.ui.viewmodel.PlayerAmbientViewModel
import com.example.ui.viewmodel.StreakViewModel
import com.example.ui.viewmodel.WatchXpNotice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

// Design Tokens for Liquid Glass Dark Mode
val NeonCyan = Color(0xFF00FFCC)
val NeonBlue = Color(0xFF0088FF)
val GlassContainerBg = Color(0x99101622)
val GlassBorderStroke = Color(0x3300FFCC)

@OptIn(UnstableApi::class)
enum class VideoResizeMode(val title: String, val exoMode: Int) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    CROP("Crop", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

data class VideoPlayerUiState(
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val resizeMode: VideoResizeMode = VideoResizeMode.FIT,
    val volume: Float = 0.75f,
    val brightness: Float = 0.70f,
    val areControlsVisible: Boolean = true,
    val isAntiCheatLocked: Boolean = false,
    val isAmbientGlowEnabled: Boolean = true,
    val hideFileExtensions: Boolean = true,
    val videoRotationDegrees: Int = 0,
    val isLandscapeOrientation: Boolean = false,
    val selectedAudioTrack: String = "Stereo 2.0 (AAC)",
    val selectedSubtitle: String = "Off",
    val isScreenLocked: Boolean = false
)

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExoVideoPlayer(
    mediaItem: MediaItem,
    streakViewModel: StreakViewModel?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val prefsRepo = remember { com.example.data.preferences.StreakPreferencesRepository.getInstance(context) }
    val playerAmbientViewModel: PlayerAmbientViewModel = viewModel()
    val persistentConfig by playerAmbientViewModel.config.collectAsStateWithLifecycle()
    val isRtlLayout = LocalLayoutDirection.current == LayoutDirection.Rtl

    val streakState = streakViewModel?.uiState?.collectAsState()?.value
    val isPremiumActivated = streakViewModel?.isPremiumActivated?.collectAsState()?.value ?: prefsRepo.isPremiumActivated()
    val isWaveformSeekbarEnabled = streakViewModel?.isWaveformSeekbarEnabled?.collectAsState()?.value ?: prefsRepo.isWaveformSeekbarEnabled()
    val activeSeekbarStyle = streakViewModel?.activeSeekbarStyle?.collectAsState()?.value ?: prefsRepo.getActiveSeekbarStyle()
    val effectiveSeekbarStyle = when {
        activeSeekbarStyle != PlayerSeekbarStyle.CLASSIC -> activeSeekbarStyle
        isWaveformSeekbarEnabled -> PlayerSeekbarStyle.WAVEFORM
        else -> PlayerSeekbarStyle.CLASSIC
    }
    val purchasedAmbientIds by (streakViewModel?.purchasedAmbientModeIds?.collectAsState()
        ?: remember { mutableStateOf(prefsRepo.getPurchasedAmbientModeIds()) })
    val isAntiCheatSeekingLocked = !isPremiumActivated && streakState != null && !streakState.isGoalCompletedToday
    val unlockedAmbientModes = remember(isPremiumActivated, persistentConfig.blackBlurMode, purchasedAmbientIds) {
        com.example.data.local.AmbientBlackBlurMode.entries
            .filter { prefsRepo.isAmbientModeUnlocked(it) || purchasedAmbientIds.contains(it.name) || isPremiumActivated }
            .toSet()
    }
    val effectiveAmbientMode = if (persistentConfig.blackBlurMode in unlockedAmbientModes) {
        persistentConfig.blackBlurMode
    } else {
        com.example.data.local.AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR
    }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxStreamVolume = remember(audioManager) {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
    }

    // Core Player State
    var uiState by remember {
        val currentSysVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxStreamVolume / 2)
        val initialRatio = (currentSysVol.toFloat() / maxStreamVolume.toFloat()).coerceIn(0f, 1f)
        mutableStateOf(
            VideoPlayerUiState(
                isAntiCheatLocked = isAntiCheatSeekingLocked,
                isAmbientGlowEnabled = persistentConfig.isAmbientGlowEnabled,
                hideFileExtensions = prefsRepo.getHideFileExtensions(),
                isLandscapeOrientation = isLandscape,
                volume = initialRatio
            )
        )
    }

    LaunchedEffect(isAntiCheatSeekingLocked) {
        uiState = uiState.copy(isAntiCheatLocked = isAntiCheatSeekingLocked)
    }

    // Dynamic Video Dimensions & Aspect Ratio
    var videoWidth by remember { mutableStateOf(1920) }
    var videoHeight by remember { mutableStateOf(1080) }

    // Cinema Desaturated Edge Sample (strictly for BlackBlurAmbientBackdrop — never alters seekbar colors)
    var cinemaEdgeSample by remember { mutableStateOf(CinemaEdgeSample()) }

    val fallbackCinemaEdges = remember {
        listOf(
            CinemaEdgeSample(
                topEdge = Color(0xFF1D222B),
                bottomEdge = Color(0xFF161920),
                leftEdge = Color(0xFF1A1E26),
                rightEdge = Color(0xFF191D24),
                warmMidnightEdge = Color(0xFF211C1A),
                averageLuminance = 0.28f
            ),
            CinemaEdgeSample(
                topEdge = Color(0xFF221F26),
                bottomEdge = Color(0xFF18161C),
                leftEdge = Color(0xFF1E1C22),
                rightEdge = Color(0xFF1B1920),
                warmMidnightEdge = Color(0xFF241D1B),
                averageLuminance = 0.30f
            ),
            CinemaEdgeSample(
                topEdge = Color(0xFF1A2326),
                bottomEdge = Color(0xFF151C1E),
                leftEdge = Color(0xFF182022),
                rightEdge = Color(0xFF171E20),
                warmMidnightEdge = Color(0xFF1F1D1B),
                averageLuminance = 0.26f
            )
        )
    }

    // Modals & Floating Dialogs
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAudioSubtitleSheet by remember { mutableStateOf(false) }
    var showAmbientModePicker by remember { mutableStateOf(false) }
    var showStrikeLockPopup by remember { mutableStateOf(false) }
    var strikeLockPopupMessage by remember { mutableStateOf("Locked until daily goal is reached.") }
    var showPremiumUnlockDialog by remember { mutableStateOf(false) }
    var playerPremiumCodeInput by remember { mutableStateOf("") }
    var playerPremiumCodeError by remember { mutableStateOf<String?>(null) }
    var lockedPremiumFeatureName by remember { mutableStateOf("") }
    var isBackgroundPlayActive by remember { mutableStateOf(prefsRepo.isBackgroundPlayEnabled()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var gestureOverlayType by remember { mutableStateOf<String?>(null) }
    var seekPreviewText by remember { mutableStateOf("") }
    var activeWatchXpNotice by remember { mutableStateOf<WatchXpNotice?>(null) }
    val xpNoticeAlpha = remember { Animatable(0f) }
    val xpNoticeOffsetY = remember { Animatable(10f) }
    val xpNoticeScale = remember { Animatable(0.92f) }
    var isDraggingSeekbar by remember { mutableStateOf(false) }
    var isDraggingGesture by remember { mutableStateOf(false) }
    var dragGestureMode by remember { mutableStateOf<String?>(null) }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    var scrubbedPositionMs by remember { mutableLongStateOf(0L) }
    var smoothPositionMs by remember { mutableLongStateOf(0L) }
    var controlInteractionNonce by remember { mutableLongStateOf(0L) }
    var isSeekingForward by remember { mutableStateOf(true) }
    var showUnlockButton by remember { mutableStateOf(false) }
    var isLeftToolbarCollapsed by remember { mutableStateOf(persistentConfig.isToolbarCollapsed) }
    // Interactive 1:1 finger tracking progress (0f = fully expanded, 1f = fully collapsed)
    val toolbarCollapseProgress = remember { Animatable(if (persistentConfig.isToolbarCollapsed) 1f else 0f) }
    var showGoalCelebrationBanner by remember { mutableStateOf(false) }

    // Press-and-Hold 2x Speed + Drag-Right Speed Selector State
    var isHoldSpeedActive by remember { mutableStateOf(false) }
    var holdSpeedValue by remember { mutableFloatStateOf(HOLD_SPEED_TIERS[0]) }
    var holdSpeedDragProgress by remember { mutableFloatStateOf(0f) }

    // Directional Edge Glow State (-1 = Rewind/Left, +1 = Forward/Right)
    var edgeGlowDirection by remember { mutableStateOf(0) }
    var edgeGlowTriggerNonce by remember { mutableLongStateOf(0L) }
    val edgeGlowIntensity = remember { Animatable(0f) }

    // Double-Tap Play / Pause Center Flash State
    var doubleTapPlayPauseNonce by remember { mutableLongStateOf(0L) }
    var doubleTapPlayPauseFlashIsPlaying by remember { mutableStateOf(true) }
    val playPauseFlashAlpha = remember { Animatable(0f) }
    val playPauseFlashScale = remember { Animatable(0.82f) }

    LaunchedEffect(edgeGlowTriggerNonce) {
        if (edgeGlowTriggerNonce == 0L) return@LaunchedEffect
        edgeGlowIntensity.snapTo(edgeGlowIntensity.value.coerceAtLeast(0.25f))
        edgeGlowIntensity.animateTo(
            1f,
            tween(90, easing = FastOutSlowInEasing)
        )
        delay(110L)
        edgeGlowIntensity.animateTo(
            0f,
            tween(440, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
        )
        edgeGlowDirection = 0
    }

    LaunchedEffect(doubleTapPlayPauseNonce) {
        if (doubleTapPlayPauseNonce == 0L) return@LaunchedEffect
        playPauseFlashAlpha.snapTo(0f)
        playPauseFlashScale.snapTo(0.78f)
        coroutineScope {
            launch {
                playPauseFlashAlpha.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
                delay(240L)
                playPauseFlashAlpha.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
            }
            launch {
                playPauseFlashScale.animateTo(
                    1.02f,
                    spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)
                )
            }
        }
    }

    fun triggerDirectionalEdgeGlow(direction: Int) {
        edgeGlowDirection = direction
        edgeGlowTriggerNonce++
    }

    LaunchedEffect(streakViewModel) {
        streakViewModel?.watchXpEarnedEvents?.collect { notice ->
            activeWatchXpNotice = notice
        }
    }

    LaunchedEffect(activeWatchXpNotice?.id) {
        val current = activeWatchXpNotice ?: return@LaunchedEffect
        xpNoticeAlpha.snapTo(0f)
        xpNoticeOffsetY.snapTo(10f)
        xpNoticeScale.snapTo(0.92f)
        coroutineScope {
            launch {
                xpNoticeAlpha.animateTo(
                    0.82f,
                    tween(220, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                )
            }
            launch {
                xpNoticeOffsetY.animateTo(
                    0f,
                    tween(240, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                )
            }
            launch {
                xpNoticeScale.animateTo(
                    1f,
                    tween(240, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                )
            }
        }
        delay(520L)
        coroutineScope {
            launch {
                xpNoticeAlpha.animateTo(
                    0f,
                    tween(260, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f))
                )
            }
            launch {
                xpNoticeOffsetY.animateTo(
                    -8f,
                    tween(260, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f))
                )
            }
            launch {
                xpNoticeScale.animateTo(
                    0.96f,
                    tween(260, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f))
                )
            }
        }
        if (activeWatchXpNotice?.id == current.id) {
            activeWatchXpNotice = null
        }
    }

    // Offline Features: Snapshot, Room Bookmarks, Auto-Crop, Boost & Subtitle Sync
    var snapshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showSnapshotToast by remember { mutableStateOf(false) }
    val videoBookmarksFlow = remember(mediaItem.id, mediaItem.uriString) {
        playerAmbientViewModel.observeBookmarksForVideo(mediaItem.id, mediaItem.uriString)
    }
    val videoBookmarks by videoBookmarksFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val bookmarks = remember(videoBookmarks) { videoBookmarks.map { it.timestampMs } }
    var bookmarkToastMessage by remember { mutableStateOf<String?>(null) }
    var isAutoCropBlackBars by remember { mutableStateOf(persistentConfig.isAutoCropBlackBars) }
    var audioBoostMultiplier by remember { mutableFloatStateOf(persistentConfig.audioBoostMultiplier) }
    var subtitleDelayOffsetMs by remember { mutableLongStateOf(persistentConfig.subtitleDelayOffsetMs) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

    LaunchedEffect(showSnapshotToast) {
        if (showSnapshotToast) {
            delay(3200)
            showSnapshotToast = false
        }
    }

    LaunchedEffect(bookmarkToastMessage) {
        if (bookmarkToastMessage != null) {
            delay(2800)
            bookmarkToastMessage = null
        }
    }

    fun takeFrameSnapshot() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        try {
            val surface = playerViewRef?.videoSurfaceView
            if (surface is TextureView) {
                val bmp = surface.bitmap
                if (bmp != null) {
                    snapshotBitmap = bmp
                    showSnapshotToast = true
                    return
                }
            }
        } catch (_: Throwable) {}

        try {
            val pv = playerViewRef
            if (pv != null) {
                val v = pv as android.view.View
                val w = v.width
                val h = v.height
                if (w > 0 && h > 0) {
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val c = android.graphics.Canvas(bmp)
                    v.draw(c)
                    snapshotBitmap = bmp
                    showSnapshotToast = true
                }
            }
        } catch (_: Throwable) {
            showSnapshotToast = true
        }
    }

    // When the 5-minute goal is reached today, trigger smooth celebration and hide anti-cheat banner
    LaunchedEffect(streakState?.isGoalCompletedToday) {
        if (streakState?.isGoalCompletedToday == true && uiState.isAntiCheatLocked) {
            showGoalCelebrationBanner = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(3500)
            showGoalCelebrationBanner = false
        }
    }

    // Helper to format title with or without file extension
    fun formatDisplayTitle(rawTitle: String, hideExt: Boolean): String {
        return if (hideExt) {
            rawTitle.replace(Regex("\\.(mp4|mkv|avi|mov|flv|webm|ts|m4v|3gp|wmv)$", RegexOption.IGNORE_CASE), "")
        } else {
            rawTitle
        }
    }

    // Trigger Anti-Cheat Alert Popup or Aura Pulse Premium Unlock Dialog
    fun triggerStrikeLockAlert(featureName: String, requiredDay: Int = 0) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (requiredDay > 0) {
            lockedPremiumFeatureName = featureName
            playerPremiumCodeInput = ""
            playerPremiumCodeError = null
            showPremiumUnlockDialog = true
        } else {
            strikeLockPopupMessage =
                "التمرير مقفل مؤقتاً لحين إكمال هدف المشاهدة اليومي (5 دقائق).\nSeek locked until 5-min daily watch goal is completed."
            showStrikeLockPopup = true
        }
    }

    fun triggerPremiumFeatureLock(featureName: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        lockedPremiumFeatureName = featureName
        playerPremiumCodeInput = ""
        playerPremiumCodeError = null
        showPremiumUnlockDialog = true
    }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        val videoAudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        ExoPlayer.Builder(context)
            .setAudioAttributes(videoAudioAttributes, /* handleAudioFocus = */ true)
            .build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ALL
            }
    }

    fun togglePlayPauseWithFeedback(fromDoubleTap: Boolean = false) {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            smoothPositionMs = exoPlayer.currentPosition
            uiState = uiState.copy(isPlaying = false, currentPositionMs = smoothPositionMs)
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration.coerceAtLeast(0L)
            if (pos > 500L && dur > 0L) {
                prefsRepo.saveVideoProgress(
                    id = mediaItem.id,
                    uri = mediaItem.uriString,
                    title = mediaItem.title,
                    durationMs = dur,
                    positionMs = pos,
                    thumbnailRes = mediaItem.posterRes
                )
            }
            if (fromDoubleTap) {
                doubleTapPlayPauseFlashIsPlaying = false
                doubleTapPlayPauseNonce++
            }
        } else {
            exoPlayer.play()
            uiState = uiState.copy(isPlaying = true)
            if (fromDoubleTap) {
                doubleTapPlayPauseFlashIsPlaying = true
                doubleTapPlayPauseNonce++
            }
        }
    }

    var isClosing by remember(mediaItem.id) { mutableStateOf(false) }

    val handleClose: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            try {
                exoPlayer.pause()
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration.coerceAtLeast(0L)
                if (pos > 500L && dur > 0L) {
                    prefsRepo.saveVideoProgress(
                        id = mediaItem.id,
                        uri = mediaItem.uriString,
                        title = mediaItem.title,
                        durationMs = dur,
                        positionMs = pos,
                        thumbnailRes = mediaItem.posterRes
                    )
                }
            } catch (_: Exception) {}
            onClose()
        }
    }

    BackHandler(enabled = !isClosing) {
        when {
            showStrikeLockPopup -> showStrikeLockPopup = false
            showPremiumUnlockDialog -> showPremiumUnlockDialog = false
            showAmbientModePicker -> showAmbientModePicker = false
            showSettingsSheet -> showSettingsSheet = false
            showAudioSubtitleSheet -> showAudioSubtitleSheet = false
            else -> handleClose()
        }
    }

    // Automatically restore all Room-persisted video player preferences whenever config emits
    LaunchedEffect(persistentConfig) {
        val restoredResizeMode = VideoResizeMode.entries.firstOrNull {
            it.name == persistentConfig.resizeModeName
        } ?: VideoResizeMode.FIT
        val restoredSpeed = persistentConfig.playbackSpeed.coerceIn(0.5f, 2.0f)
        uiState = uiState.copy(
            isAmbientGlowEnabled = persistentConfig.isAmbientGlowEnabled,
            playbackSpeed = restoredSpeed,
            resizeMode = restoredResizeMode,
            selectedAudioTrack = persistentConfig.selectedAudioTrack,
            selectedSubtitle = persistentConfig.selectedSubtitle
        )
        isAutoCropBlackBars = persistentConfig.isAutoCropBlackBars
        audioBoostMultiplier = persistentConfig.audioBoostMultiplier.coerceIn(1.0f, 2.0f)
        subtitleDelayOffsetMs = persistentConfig.subtitleDelayOffsetMs
        isLeftToolbarCollapsed = persistentConfig.isToolbarCollapsed
        if (!toolbarCollapseProgress.isRunning) {
            toolbarCollapseProgress.snapTo(if (persistentConfig.isToolbarCollapsed) 1f else 0f)
        }
        if (!isHoldSpeedActive) {
            exoPlayer.playbackParameters = PlaybackParameters(restoredSpeed)
        }
        exoPlayer.volume = (uiState.volume * audioBoostMultiplier).coerceIn(0f, 2.0f)
        playerViewRef?.resizeMode = if (isAutoCropBlackBars) {
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            restoredResizeMode.exoMode
        }
    }

    // Local bundled sample video URI (100% offline, guaranteed no HTTP 403)
    val defaultLocalSampleUri = remember(context) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.sample_video}")
    }

    var hasAppliedInitialExplicitSeek by remember(mediaItem) { mutableStateOf(false) }

    // Media item attachment & Automatic Resume or Exact Bookmark Timestamp Seek
    LaunchedEffect(mediaItem) {
        val uri = when {
            !mediaItem.uriString.isNullOrBlank() -> Uri.parse(mediaItem.uriString)
            else -> defaultLocalSampleUri
        }

        try {
            val exoItem = ExoMediaItem.fromUri(uri)
            exoPlayer.setMediaItem(exoItem)
            exoPlayer.prepare()

            val explicitSeekMs = mediaItem.initialSeekPositionMs
            if (explicitSeekMs != null) {
                val targetMs = explicitSeekMs.coerceAtLeast(0L)
                exoPlayer.seekTo(targetMs)
                smoothPositionMs = targetMs
                uiState = uiState.copy(currentPositionMs = targetMs)
            } else if (prefsRepo.getAutoResumePlayback()) {
                // Check if there is saved playback progress for this specific video
                val savedProgress = prefsRepo.getVideoProgress(mediaItem.id, mediaItem.uriString)
                val savedPos = savedProgress?.positionMs ?: 0L
                val savedDur = savedProgress?.durationMs ?: 0L

                // Resume playback if video was in progress (more than 1.5s and not within 3s of the end)
                if (savedPos > 1500L && (savedDur <= 0L || savedPos < savedDur - 3000L)) {
                    exoPlayer.seekTo(savedPos)
                    smoothPositionMs = savedPos
                    uiState = uiState.copy(currentPositionMs = savedPos)
                }
            }

            exoPlayer.play()
        } catch (_: Exception) {}
    }

    // Player Event Listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val dur = exoPlayer.duration.coerceAtLeast(0L)
                uiState = uiState.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    durationMs = dur
                )

                // Secondary check for bookmark jump or auto-resume when player reaches STATE_READY
                if (playbackState == Player.STATE_READY) {
                    val explicitSeekMs = mediaItem.initialSeekPositionMs
                    if (explicitSeekMs != null) {
                        if (!hasAppliedInitialExplicitSeek) {
                            hasAppliedInitialExplicitSeek = true
                            val clamped = if (dur > 0L) explicitSeekMs.coerceIn(0L, dur) else explicitSeekMs.coerceAtLeast(0L)
                            if (abs(exoPlayer.currentPosition - clamped) > 1000L) {
                                exoPlayer.seekTo(clamped)
                            }
                            smoothPositionMs = clamped
                            uiState = uiState.copy(currentPositionMs = clamped)
                        }
                    } else {
                        val savedProgress = prefsRepo.getVideoProgress(mediaItem.id, mediaItem.uriString)
                        val savedPos = savedProgress?.positionMs ?: 0L
                        if (savedPos > 1500L && exoPlayer.currentPosition < 1000L && (dur <= 0L || savedPos < dur - 3000L)) {
                            exoPlayer.seekTo(savedPos)
                            smoothPositionMs = savedPos
                            uiState = uiState.copy(currentPositionMs = savedPos)
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // If remote source or expired URL throws an error (e.g. 403 Forbidden),
                // automatically fallback to the local bundled high-definition offline video
                try {
                    val fallbackItem = ExoMediaItem.fromUri(defaultLocalSampleUri)
                    exoPlayer.setMediaItem(fallbackItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                } catch (_: Throwable) {}
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                uiState = uiState.copy(isPlaying = isPlaying)
                com.example.data.local.MediaPlaybackActivityBus.setVideoPlaying(isPlaying)
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoWidth = videoSize.width
                    videoHeight = videoSize.height
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            com.example.data.local.MediaPlaybackActivityBus.setVideoPlaying(false)
            try {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration.coerceAtLeast(0L)
                if (pos > 500L && dur > 0L) {
                    prefsRepo.saveVideoProgress(
                        id = mediaItem.id,
                        uri = mediaItem.uriString,
                        title = mediaItem.title,
                        durationMs = dur,
                        positionMs = pos,
                        thumbnailRes = mediaItem.posterRes
                    )
                }
            } catch (_: Exception) {}
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Pause playback on backgrounding unless Background Play is active with Premium (or in PiP)
    DisposableEffect(lifecycleOwner, exoPlayer, isBackgroundPlayActive, isPremiumActivated) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                val inPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    (context as? Activity)?.isInPictureInPictureMode == true
                } else {
                    false
                }
                val allowBackground = isPremiumActivated && isBackgroundPlayActive
                if (!allowBackground && !inPip && exoPlayer.isPlaying) {
                    exoPlayer.pause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // VSync-locked progress tracker for silky-smooth 60-120 FPS Canvas seekbar motion
    LaunchedEffect(uiState.isPlaying, isDraggingSeekbar) {
        while (uiState.isPlaying && !isDraggingSeekbar) {
            withFrameNanos {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration.coerceAtLeast(0L)
                smoothPositionMs = pos
                if (dur > 0L && dur != uiState.durationMs) {
                    uiState = uiState.copy(durationMs = dur)
                }
            }
        }
        if (!isDraggingSeekbar) {
            smoothPositionMs = exoPlayer.currentPosition
        }
    }

    // Real-time Progress, 5-Minute Daily Goal Ticker & Ambient Color Loop
    LaunchedEffect(uiState.isPlaying, isDraggingSeekbar) {
        var secondAccumulatorMs = 0L
        while (uiState.isPlaying && !isDraggingSeekbar) {
            delay(250)
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration.coerceAtLeast(0L)
            uiState = uiState.copy(
                currentPositionMs = pos,
                durationMs = dur
            )

            // 1. Core Fix: Tick genuine watch time towards the 5-minute daily watch goal!
            // Strictly counts real playback (not seeking, not paused, not buffering)
            if (!uiState.isBuffering && exoPlayer.isPlaying) {
                secondAccumulatorMs += 250L
                if (secondAccumulatorMs >= 1000L) {
                    if (streakViewModel != null) {
                        streakViewModel.tickGenuineWatchTime(seconds = 1, isSeeking = false)
                    } else {
                        prefsRepo.tickDailyWatchTime(seconds = 1)
                    }
                    secondAccumulatorMs -= 1000L
                }
            }

            // 2. Periodically persist playback progress for this video and Continue Watching
            if (pos > 500L && dur > 0L && (pos % 2000L < 300L)) {
                prefsRepo.saveVideoProgress(
                    id = mediaItem.id,
                    uri = mediaItem.uriString,
                    title = mediaItem.title,
                    durationMs = dur,
                    positionMs = pos,
                    thumbnailRes = mediaItem.posterRes
                )
            }

            // Extract desaturated cinema edge colors & luminance smoothly from TextureView frame (heavily damped + flash clamped)
            if (uiState.isAmbientGlowEnabled && (pos % 600L < 220L)) {
                playerViewRef?.videoSurfaceView?.let { surfaceView ->
                    if (surfaceView is TextureView && surfaceView.isAvailable) {
                        val bitmap = try {
                            surfaceView.getBitmap(16, 16)
                        } catch (_: Throwable) {
                            null
                        }
                        if (bitmap != null) {
                            try {
                                val rawSample = extractCinemaEdgeSample(bitmap)
                                cinemaEdgeSample = dampCinemaEdgeSample(cinemaEdgeSample, rawSample)
                            } catch (_: Throwable) {
                            } finally {
                                if (!bitmap.isRecycled) {
                                    bitmap.recycle()
                                }
                            }
                        }
                    } else {
                        val paletteIdx = ((pos / 4500) % fallbackCinemaEdges.size).toInt()
                        cinemaEdgeSample = dampCinemaEdgeSample(cinemaEdgeSample, fallbackCinemaEdges[paletteIdx])
                    }
                }
            }
        }
    }

    // Auto-hide controls after configured seconds of inactivity
    LaunchedEffect(
        uiState.areControlsVisible,
        uiState.isPlaying,
        isDraggingSeekbar,
        showSettingsSheet,
        showAudioSubtitleSheet,
        showAmbientModePicker,
        controlInteractionNonce,
        persistentConfig.controlsAutoHideSeconds
    ) {
        if (uiState.areControlsVisible && uiState.isPlaying && !isDraggingSeekbar && !showSettingsSheet && !showAudioSubtitleSheet && !showAmbientModePicker) {
            delay(persistentConfig.controlsAutoHideSeconds.coerceIn(3, 15) * 1000L)
            uiState = uiState.copy(areControlsVisible = false)
        }
    }

    // Dismiss HUD overlays
    LaunchedEffect(gestureOverlayType) {
        if (gestureOverlayType != null) {
            delay(1400)
            gestureOverlayType = null
        }
    }

    // Dismiss Strike Lock Popup automatically after 3.2 seconds
    LaunchedEffect(showStrikeLockPopup) {
        if (showStrikeLockPopup) {
            delay(3200)
            showStrikeLockPopup = false
        }
    }

    // Auto-dismiss floating unlock button after 3.5 seconds
    LaunchedEffect(showUnlockButton) {
        if (showUnlockButton) {
            delay(3500)
            showUnlockButton = false
        }
    }

    // Glowing Neon Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "player_neon_pulse")
    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_alpha"
    )

    val ambientBreathScale by infiniteTransition.animateFloat(
        initialValue = 1.02f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_breath_scale"
    )

    val density = LocalDensity.current
    val latestScreenLocked by rememberUpdatedState(uiState.isScreenLocked)
    val latestEnableSwipeGestures by rememberUpdatedState(persistentConfig.enableSwipeGestures)
    val latestDoubleTapSeconds by rememberUpdatedState(persistentConfig.doubleTapSeekSeconds)

    DisposableEffect(exoPlayer) {
        onDispose {
            try {
                if (isHoldSpeedActive) {
                    exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
                }
            } catch (_: Throwable) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Charcoal950)
            .testTag("high_end_exo_video_player")
            // Unified gesture recognizer:
            // - Single Tap: toggle controls
            // - Double Tap: skip +/- doubleTapSeekSeconds
            // - Immediate Swipe (<280ms): Left Brightness / Right Volume / Horizontal Seek
            // - Press & Hold (280ms+): Instant 2.0x speed, Drag Right to step 2.0x -> 2.5x -> 3.0x -> 3.5x -> 4.0x
            .pointerInput(Unit) {
                val baseStepPx = with(density) { 44.dp.toPx() }
                val minStepPx = with(density) { 22.dp.toPx() }
                val edgeMarginPx = with(density) { 20.dp.toPx() }
                val holdTimeoutMs = 280L

                val handleDoubleTapAt: (Offset) -> Unit = { offset ->
                    if (latestScreenLocked) {
                        showUnlockButton = true
                    } else {
                        val widthF = size.width.toFloat().coerceAtLeast(1f)
                        val relX = offset.x / widthF
                        // Center 52% zone toggles Play/Pause; outer 24% left/right edges skip +/- 10s with Directional Edge Glow
                        if (relX in 0.24f..0.76f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            togglePlayPauseWithFeedback(fromDoubleTap = true)
                        } else {
                            val skipSeconds = latestDoubleTapSeconds.coerceIn(5, 30)
                            val skipMs = skipSeconds * 1000L
                            val isRightEdge = relX > 0.76f
                            if (uiState.isAntiCheatLocked) {
                                triggerStrikeLockAlert("Seeking Anti-Cheat")
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (isRightEdge) {
                                    val target = (exoPlayer.currentPosition + skipMs).coerceAtMost(uiState.durationMs)
                                    exoPlayer.seekTo(target)
                                    smoothPositionMs = target
                                    uiState = uiState.copy(currentPositionMs = target)
                                    triggerDirectionalEdgeGlow(direction = 1)
                                } else {
                                    val target = (exoPlayer.currentPosition - skipMs).coerceAtLeast(0L)
                                    exoPlayer.seekTo(target)
                                    smoothPositionMs = target
                                    uiState = uiState.copy(currentPositionMs = target)
                                    triggerDirectionalEdgeGlow(direction = -1)
                                }
                            }
                        }
                    }
                }

                val runHoldSpeedLoop: suspend androidx.compose.ui.input.pointer.AwaitPointerEventScope.(
                    pointerId: androidx.compose.ui.input.pointer.PointerId,
                    anchorX: Float
                ) -> Unit = { pointerId, anchorX ->
                    if (latestScreenLocked) {
                        showUnlockButton = true
                        while (true) {
                            val ev = awaitPointerEvent()
                            val ch = ev.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!ch.pressed) {
                                ch.consume()
                                break
                            }
                        }
                    } else {
                        val wasPlaying = exoPlayer.isPlaying
                        var tempUnpaused = false
                        var currentTierIndex = 0
                        try {
                            if (!wasPlaying) {
                                exoPlayer.play()
                                tempUnpaused = true
                            }
                            isHoldSpeedActive = true
                            holdSpeedValue = HOLD_SPEED_TIERS[0]
                            holdSpeedDragProgress = 0f
                            exoPlayer.playbackParameters = PlaybackParameters(HOLD_SPEED_TIERS[0])
                            if (uiState.areControlsVisible) {
                                uiState = uiState.copy(areControlsVisible = false)
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            while (true) {
                                val ev = awaitPointerEvent()
                                val ch = ev.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!ch.pressed) {
                                    ch.consume()
                                    break
                                }
                                ch.consume()
                                val deltaRightPx = ch.position.x - anchorX
                                holdSpeedDragProgress = computeHoldDragProgress(
                                    deltaRightPx = deltaRightPx,
                                    anchorX = anchorX,
                                    viewportWidthPx = size.width.toFloat(),
                                    baseStepPx = baseStepPx,
                                    minStepPx = minStepPx,
                                    edgeMarginPx = edgeMarginPx
                                )
                                val nextIndex = computeHoldSpeedTierIndexWithHysteresis(
                                    deltaRightPx = deltaRightPx,
                                    currentIndex = currentTierIndex,
                                    anchorX = anchorX,
                                    viewportWidthPx = size.width.toFloat(),
                                    baseStepPx = baseStepPx,
                                    minStepPx = minStepPx,
                                    edgeMarginPx = edgeMarginPx
                                )
                                if (nextIndex != currentTierIndex) {
                                    currentTierIndex = nextIndex
                                    val newSpeed = HOLD_SPEED_TIERS[nextIndex]
                                    holdSpeedValue = newSpeed
                                    exoPlayer.playbackParameters = PlaybackParameters(newSpeed)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        } finally {
                            isHoldSpeedActive = false
                            holdSpeedValue = HOLD_SPEED_TIERS[0]
                            holdSpeedDragProgress = 0f
                            exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
                            if (tempUnpaused) {
                                exoPlayer.pause()
                            }
                        }
                    }
                }

                val runSwipeDragLoop: suspend androidx.compose.ui.input.pointer.AwaitPointerEventScope.(
                    pointerId: androidx.compose.ui.input.pointer.PointerId,
                    initialDownPos: Offset,
                    firstMovedPos: Offset
                ) -> Unit = { pointerId, initialDownPos, firstMovedPos ->
                    if (latestScreenLocked) {
                        showUnlockButton = true
                    } else if (latestEnableSwipeGestures) {
                        var activeDragMode: String? = null
                        var accX = firstMovedPos.x - initialDownPos.x
                        var accY = firstMovedPos.y - initialDownPos.y
                        var prevPos = firstMovedPos

                        fun applySwipeStep(pos: Offset, deltaX: Float, deltaY: Float) {
                            accX += deltaX
                            accY += deltaY
                            if (activeDragMode == null) {
                                val absX = abs(accX)
                                val absY = abs(accY)
                                val threshold = 16f
                                if (absX > threshold && absX > absY * 1.75f) {
                                    activeDragMode = "SEEK"
                                    dragGestureMode = "SEEK"
                                } else if (absY > threshold && absY > absX * 1.4f) {
                                    val isRightSide = pos.x > (size.width / 2f)
                                    activeDragMode = if (isRightSide) "VOL" else "BRIGHT"
                                    dragGestureMode = activeDragMode
                                }
                            }
                            when (activeDragMode) {
                                "VOL" -> {
                                    val delta = -deltaY / 450f
                                    val newVol = (uiState.volume + delta).coerceIn(0f, 1f)
                                    uiState = uiState.copy(volume = newVol)
                                    exoPlayer.volume = newVol
                                    audioManager?.let { am ->
                                        val targetIndex = (newVol * maxStreamVolume).toInt().coerceIn(0, maxStreamVolume)
                                        try {
                                            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
                                        } catch (_: Exception) {}
                                    }
                                    gestureOverlayType = "VOLUME"
                                }
                                "BRIGHT" -> {
                                    val delta = -deltaY / 450f
                                    val newBrightness = (uiState.brightness + delta).coerceIn(0.05f, 1f)
                                    uiState = uiState.copy(brightness = newBrightness)
                                    (context as? Activity)?.let { act ->
                                        val lp = act.window.attributes
                                        lp.screenBrightness = newBrightness
                                        act.window.attributes = lp
                                    }
                                    gestureOverlayType = "BRIGHTNESS"
                                }
                                "SEEK" -> {
                                    if (uiState.isAntiCheatLocked) {
                                        triggerStrikeLockAlert("Seeking Anti-Cheat")
                                    } else {
                                        isSeekingForward = deltaX >= 0f
                                        val deltaMs = (deltaX * 120).toLong()
                                        val targetPos = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, uiState.durationMs)
                                        exoPlayer.seekTo(targetPos)
                                        smoothPositionMs = targetPos
                                        uiState = uiState.copy(currentPositionMs = targetPos)
                                        gestureOverlayType = "SEEK"
                                        seekPreviewText = "${if (deltaMs >= 0) "+" else ""}${deltaMs / 1000}s (${formatDuration(targetPos)})"
                                    }
                                }
                            }
                        }

                        try {
                            isDraggingGesture = true
                            dragGestureMode = null
                            dragAccumulatedX = accX
                            dragAccumulatedY = accY
                            uiState = uiState.copy(areControlsVisible = false)
                            applySwipeStep(firstMovedPos, 0f, 0f)

                            while (true) {
                                val ev = awaitPointerEvent()
                                val ch = ev.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!ch.pressed) {
                                    ch.consume()
                                    break
                                }
                                val stepDelta = ch.position - prevPos
                                prevPos = ch.position
                                ch.consume()
                                applySwipeStep(ch.position, stepDelta.x, stepDelta.y)
                            }
                        } finally {
                            isDraggingGesture = false
                            dragGestureMode = null
                            gestureOverlayType = null
                        }
                    }
                }

                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = true)
                    var lastPos = firstDown.position
                    var movedPos: Offset? = null
                    var liftedEarly = false

                    withTimeoutOrNull(holdTimeoutMs) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == firstDown.id } ?: break
                            if (!change.pressed) {
                                liftedEarly = true
                                change.consume()
                                break
                            }
                            lastPos = change.position
                            val dist = hypot(
                                change.position.x - firstDown.position.x,
                                change.position.y - firstDown.position.y
                            )
                            if (dist > viewConfiguration.touchSlop) {
                                movedPos = change.position
                                change.consume()
                                break
                            }
                        }
                    }

                    val initialMove = movedPos
                    when {
                        initialMove != null -> {
                            runSwipeDragLoop(firstDown.id, firstDown.position, initialMove)
                        }
                        !liftedEarly -> {
                            runHoldSpeedLoop(firstDown.id, lastPos.x)
                        }
                        else -> {
                            val doubleTapSlopPx = viewConfiguration.touchSlop * 6f
                            val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                                awaitFirstDown(requireUnconsumed = true)
                            }
                            if (secondDown == null ||
                                hypot(
                                    secondDown.position.x - firstDown.position.x,
                                    secondDown.position.y - firstDown.position.y
                                ) > doubleTapSlopPx
                            ) {
                                if (latestScreenLocked) {
                                    showUnlockButton = true
                                } else {
                                    uiState = uiState.copy(areControlsVisible = !uiState.areControlsVisible)
                                }
                            } else {
                                var secondMovedPos: Offset? = null
                                var secondLifted = false
                                var secondLastPos = secondDown.position
                                withTimeoutOrNull(holdTimeoutMs) {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val ch = event.changes.firstOrNull { it.id == secondDown.id } ?: break
                                        if (!ch.pressed) {
                                            secondLifted = true
                                            ch.consume()
                                            break
                                        }
                                        secondLastPos = ch.position
                                        if (hypot(
                                                ch.position.x - secondDown.position.x,
                                                ch.position.y - secondDown.position.y
                                            ) > viewConfiguration.touchSlop
                                        ) {
                                            secondMovedPos = ch.position
                                            ch.consume()
                                            break
                                        }
                                    }
                                }
                                val secondMove = secondMovedPos
                                when {
                                    secondMove != null -> runSwipeDragLoop(
                                        secondDown.id,
                                        secondDown.position,
                                        secondMove
                                    )
                                    !secondLifted -> runHoldSpeedLoop(secondDown.id, secondLastPos.x)
                                    else -> handleDoubleTapAt(secondDown.position)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // =========================================================================
        // 5-MODE PURE CINEMA "BLACK BLUR" AMBIENT VIDEO GLOW FILL SYSTEM
        // =========================================================================

        // LAYER 1 (Background): Hardware-accelerated Black Blur RenderEffect + Canvas
        BlackBlurAmbientBackdrop(
            isEnabled = persistentConfig.isAmbientGlowEnabled && (effectiveAmbientMode in unlockedAmbientModes),
            mode = effectiveAmbientMode,
            edgeSample = cinemaEdgeSample,
            isCroppedFullScreen = uiState.resizeMode == VideoResizeMode.CROP || isAutoCropBlackBars
        )

        // LAYER 2 (Foreground): Main video surface centered with soft cinema framing
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.resizeMode != VideoResizeMode.CROP) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF040406).copy(alpha = 0.55f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color(0xFF040406).copy(alpha = 0.58f)
                                    )
                                )
                            )
                        }
                )
            }

            AndroidView(
                factory = { ctx ->
                    val inflated = LayoutInflater.from(ctx).inflate(
                        R.layout.exo_texture_player_view,
                        null,
                        false
                    ) as PlayerView
                    inflated.apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = uiState.resizeMode.exoMode
                        keepScreenOn = true
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        playerViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        val xpNotice = activeWatchXpNotice
        if (xpNotice != null) {
            val xpTopOffset by animateDpAsState(
                targetValue = if (uiState.areControlsVisible && !uiState.isScreenLocked) 62.dp else 14.dp,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "xp_whisper_top_offset"
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .stableStatusBarsPadding()
                        .padding(start = 18.dp, top = xpTopOffset),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = "+${VaultProgressionRegistry.formatXpNumber(xpNotice.xpAmount)} XP",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.55f),
                                offset = Offset(0f, 1.5f),
                                blurRadius = 6f
                            )
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = xpNoticeAlpha.value
                                translationY = xpNoticeOffsetY.value.dp.toPx()
                                scaleX = xpNoticeScale.value
                                scaleY = xpNoticeScale.value
                            }
                            .testTag("video_player_xp_gain_whisper")
                    )
                }
            }
        }

        // -------------------------------------------------------------------------
        // GESTURE OVERLAYS: Sleek Vertical Bars on Sides for Volume & Brightness
        // No central box for volume/brightness; only a minimal capsule for seek.
        // -------------------------------------------------------------------------
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            // Brightness Vertical Bar (Left edge)
            AnimatedVisibility(
                visible = gestureOverlayType == "BRIGHTNESS",
                enter = fadeIn(tween(160)) + slideInHorizontally { -it },
                exit = fadeOut(tween(220)) + slideOutHorizontally { -it },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xD90E1422),
                    border = BorderStroke(1.2.dp, Color(0x66FFD700)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .width(42.dp)
                        .height(200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessHigh,
                            contentDescription = "Brightness",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )

                        // Vertical level indicator track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .width(10.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(uiState.brightness.coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFFFF9100))
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "${(uiState.brightness * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Volume Vertical Bar (Right edge)
            AnimatedVisibility(
                visible = gestureOverlayType == "VOLUME",
                enter = fadeIn(tween(160)) + slideInHorizontally { it },
                exit = fadeOut(tween(220)) + slideOutHorizontally { it },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xD90E1422),
                    border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.7f)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .width(42.dp)
                        .height(200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )

                        // Vertical level indicator track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .width(10.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(uiState.volume.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(NeonCyan, Color(0xFF0088FF))
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "${(uiState.volume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Seek Capsule (Center of screen only for horizontal seek)
            AnimatedVisibility(
                visible = gestureOverlayType == "SEEK",
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.9f),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xE60E1422),
                    border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.7f)),
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSeekingForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = if (isSeekingForward) "Forward" else "Rewind",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = seekPreviewText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------------------
        // PRESS-AND-HOLD 2X SPEED EDGE AURA & INTERACTIVE SPEED TIER CAPSULE HUD
        // -------------------------------------------------------------------------
        HoldSpeedEdgeAura(
            visible = isHoldSpeedActive,
            speed = holdSpeedValue,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = isHoldSpeedActive,
            enter = fadeIn(tween(150)) + scaleIn(
                initialScale = 0.88f,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium)
            ),
            exit = fadeOut(tween(170)) + scaleOut(targetScale = 0.90f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .stableStatusBarsPadding()
                .padding(top = 14.dp, start = 16.dp, end = 16.dp)
        ) {
            HoldSpeedHudOverlay(
                speed = holdSpeedValue,
                dragProgress = holdSpeedDragProgress,
                isArabic = isRtlLayout
            )
        }

        // -------------------------------------------------------------------------
        // DIRECTIONAL WHITE EDGE GLOW (10s Rewind Left & Forward Right)
        // -------------------------------------------------------------------------
        DirectionalSeekEdgeGlowOverlay(
            direction = edgeGlowDirection,
            intensity = edgeGlowIntensity.value,
            modifier = Modifier.fillMaxSize()
        )

        // -------------------------------------------------------------------------
        // DOUBLE-TAP PLAY / PAUSE CENTER FLASH BADGE
        // -------------------------------------------------------------------------
        if (playPauseFlashAlpha.value > 0.01f) {
            DoubleTapPlayPauseOverlay(
                isPlaying = doubleTapPlayPauseFlashIsPlaying,
                alpha = playPauseFlashAlpha.value,
                scale = playPauseFlashScale.value,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // =========================================================================
        // CLEAN NON-OVERLAPPING ARCHITECTURE (Single Column with Arrangement.SpaceBetween)
        // =========================================================================
        AnimatedVisibility(
            visible = uiState.areControlsVisible && !uiState.isScreenLocked,
            enter = fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xD907090F),
                                Color(0x3307090F),
                                Color(0x2207090F),
                                Color(0xF007090F)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .stableStatusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // -------------------------------------------------------------
                    // TOP BAR: Clean Liquid Glass Container
                    // Contains ONLY: Back Arrow & Video Title on the Right, and Quality Badge on the Left
                    // -------------------------------------------------------------
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = GlassContainerBg,
                        border = BorderStroke(1.dp, GlassBorderStroke)
                    ) {
                        val displayTitle = formatDisplayTitle(
                            mediaItem.titleArabic ?: mediaItem.title,
                            uiState.hideFileExtensions
                        )

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Side: Back Button & Video Title (العنوان في الجهة اليسرى)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            handleClose()
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("video_player_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = displayTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1,
                                            modifier = Modifier.basicMarquee()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Right Side: Quality Badge (دقة الفيديو في الجهة اليمنى)
                                QualityBadgeChip(
                                    qualityText = mediaItem.qualityTag.ifBlank { "4K HDR" }
                                )
                            }
                        }
                    }

                    // Center controls removed from vertical flow — anchored directly at exact Box(Alignment.Center) below!

                    // -------------------------------------------------------------
                    // BOTTOM BAR: Compact Unified Frosted Surface with Configurable Seekbar Position
                    // Progress Slider starts from Left and ends at Right
                    // -------------------------------------------------------------
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = GlassContainerBg,
                        border = BorderStroke(1.dp, GlassBorderStroke)
                    ) {
                        // Standard LTR Layout Direction: progress bar starts from Left and ends at Right
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                val displayMs = if (isDraggingSeekbar) scrubbedPositionMs else uiState.currentPositionMs
                                val progressRatioProvider: () -> Float = {
                                    val liveMs = if (isDraggingSeekbar) scrubbedPositionMs else smoothPositionMs
                                    if (uiState.durationMs > 0L) {
                                        (liveMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
                                    } else 0f
                                }

                                val displayedBookmarks = if (persistentConfig.showBookmarkMarkersOnSeekbar) bookmarks else emptyList()
                                val thicknessScale = persistentConfig.seekbarThickness.scaleFactor
                                val glowScale = persistentConfig.seekbarGlowIntensity.alphaMultiplier
                                val seekbarBoxHeight = if (effectiveSeekbarStyle != PlayerSeekbarStyle.CLASSIC) {
                                    (persistentConfig.seekbarThickness.containerHeightDp - 6).coerceAtLeast(26).dp
                                } else {
                                    (20f * thicknessScale).dp.coerceAtLeast(18.dp)
                                }

                                @Composable
                                fun SeekbarTrackView() {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(seekbarBoxHeight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                    when (effectiveSeekbarStyle) {
                                        PlayerSeekbarStyle.WAVEFORM -> {
                                            WaveformAudioSeekbar(
                                                mediaId = mediaItem.id,
                                                progressRatioProvider = progressRatioProvider,
                                                durationMs = uiState.durationMs,
                                                isPlaying = uiState.isPlaying && !uiState.isBuffering,
                                                isDragging = isDraggingSeekbar,
                                                isAntiCheatLocked = uiState.isAntiCheatLocked,
                                                activePrimaryColor = NeonCyan,
                                                activeSecondaryColor = NeonBlue,
                                                bookmarks = displayedBookmarks,
                                                thicknessScale = thicknessScale,
                                                glowScale = glowScale,
                                                onLockedAttempt = {
                                                    triggerStrikeLockAlert("Seeking Anti-Cheat")
                                                },
                                                onSeekStart = { ratio ->
                                                    controlInteractionNonce++
                                                    isDraggingSeekbar = true
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekUpdate = { ratio ->
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekFinish = {
                                                    controlInteractionNonce++
                                                    exoPlayer.seekTo(scrubbedPositionMs)
                                                    smoothPositionMs = scrubbedPositionMs
                                                    uiState = uiState.copy(currentPositionMs = scrubbedPositionMs)
                                                    isDraggingSeekbar = false
                                                    prefsRepo.saveVideoProgress(
                                                        id = mediaItem.id,
                                                        uri = mediaItem.uriString,
                                                        title = mediaItem.title,
                                                        durationMs = uiState.durationMs,
                                                        positionMs = scrubbedPositionMs,
                                                        thumbnailRes = mediaItem.posterRes
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .testTag("video_player_progress_slider")
                                            )
                                        }
                                        PlayerSeekbarStyle.SHOOTING_STAR -> {
                                            ShootingStarSeekbar(
                                                progressRatioProvider = progressRatioProvider,
                                                durationMs = uiState.durationMs,
                                                isPlaying = uiState.isPlaying && !uiState.isBuffering,
                                                isDragging = isDraggingSeekbar,
                                                isAntiCheatLocked = uiState.isAntiCheatLocked,
                                                activePrimaryColor = NeonCyan,
                                                activeSecondaryColor = NeonBlue,
                                                bookmarks = displayedBookmarks,
                                                thicknessScale = thicknessScale,
                                                glowScale = glowScale,
                                                onLockedAttempt = {
                                                    triggerStrikeLockAlert("Seeking Anti-Cheat")
                                                },
                                                onSeekStart = { ratio ->
                                                    controlInteractionNonce++
                                                    isDraggingSeekbar = true
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekUpdate = { ratio ->
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekFinish = {
                                                    controlInteractionNonce++
                                                    exoPlayer.seekTo(scrubbedPositionMs)
                                                    smoothPositionMs = scrubbedPositionMs
                                                    uiState = uiState.copy(currentPositionMs = scrubbedPositionMs)
                                                    isDraggingSeekbar = false
                                                    prefsRepo.saveVideoProgress(
                                                        id = mediaItem.id,
                                                        uri = mediaItem.uriString,
                                                        title = mediaItem.title,
                                                        durationMs = uiState.durationMs,
                                                        positionMs = scrubbedPositionMs,
                                                        thumbnailRes = mediaItem.posterRes
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .testTag("video_player_progress_slider")
                                            )
                                        }
                                        PlayerSeekbarStyle.ANGRY_SNAKE -> {
                                            AngrySnakeSeekbar(
                                                progressRatioProvider = progressRatioProvider,
                                                durationMs = uiState.durationMs,
                                                isPlaying = uiState.isPlaying && !uiState.isBuffering,
                                                isDragging = isDraggingSeekbar,
                                                isAntiCheatLocked = uiState.isAntiCheatLocked,
                                                bookmarks = displayedBookmarks,
                                                thicknessScale = thicknessScale,
                                                glowScale = glowScale,
                                                onLockedAttempt = {
                                                    triggerStrikeLockAlert("Seeking Anti-Cheat")
                                                },
                                                onSeekStart = { ratio ->
                                                    controlInteractionNonce++
                                                    isDraggingSeekbar = true
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekUpdate = { ratio ->
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekFinish = {
                                                    controlInteractionNonce++
                                                    exoPlayer.seekTo(scrubbedPositionMs)
                                                    smoothPositionMs = scrubbedPositionMs
                                                    uiState = uiState.copy(currentPositionMs = scrubbedPositionMs)
                                                    isDraggingSeekbar = false
                                                    prefsRepo.saveVideoProgress(
                                                        id = mediaItem.id,
                                                        uri = mediaItem.uriString,
                                                        title = mediaItem.title,
                                                        durationMs = uiState.durationMs,
                                                        positionMs = scrubbedPositionMs,
                                                        thumbnailRes = mediaItem.posterRes
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .testTag("video_player_progress_slider")
                                            )
                                        }
                                        PlayerSeekbarStyle.PLASMA_KATANA,
                                        PlayerSeekbarStyle.QUANTUM_COLLIDER,
                                        PlayerSeekbarStyle.DOUBLE_HELIX_GENE,
                                        PlayerSeekbarStyle.EVENT_HORIZON,
                                        PlayerSeekbarStyle.CYBER_MATRIX_CODE -> {
                                            LiquidGlassSeekbarDispatcher(
                                                style = effectiveSeekbarStyle,
                                                progressRatioProvider = progressRatioProvider,
                                                durationMs = uiState.durationMs,
                                                isPlaying = uiState.isPlaying && !uiState.isBuffering,
                                                isDragging = isDraggingSeekbar,
                                                isAntiCheatLocked = uiState.isAntiCheatLocked,
                                                bookmarks = displayedBookmarks,
                                                thicknessScale = thicknessScale,
                                                glowScale = glowScale,
                                                onLockedAttempt = {
                                                    triggerStrikeLockAlert("Seeking Anti-Cheat")
                                                },
                                                onSeekStart = { ratio ->
                                                    controlInteractionNonce++
                                                    isDraggingSeekbar = true
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekUpdate = { ratio ->
                                                    val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                    scrubbedPositionMs = target
                                                    smoothPositionMs = target
                                                    if (persistentConfig.liveSeekPreview) {
                                                        exoPlayer.seekTo(target)
                                                    }
                                                    uiState = uiState.copy(currentPositionMs = target)
                                                },
                                                onSeekFinish = {
                                                    controlInteractionNonce++
                                                    exoPlayer.seekTo(scrubbedPositionMs)
                                                    smoothPositionMs = scrubbedPositionMs
                                                    uiState = uiState.copy(currentPositionMs = scrubbedPositionMs)
                                                    isDraggingSeekbar = false
                                                    prefsRepo.saveVideoProgress(
                                                        id = mediaItem.id,
                                                        uri = mediaItem.uriString,
                                                        title = mediaItem.title,
                                                        durationMs = uiState.durationMs,
                                                        positionMs = scrubbedPositionMs,
                                                        thumbnailRes = mediaItem.posterRes
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .testTag("video_player_progress_slider")
                                            )
                                        }
                                        PlayerSeekbarStyle.CLASSIC -> {
                                            val sliderProgress = progressRatioProvider()
                                            Slider(
                                                value = sliderProgress,
                                                enabled = !uiState.isAntiCheatLocked,
                                                onValueChange = { ratio ->
                                                    if (!uiState.isAntiCheatLocked) {
                                                        controlInteractionNonce++
                                                        isDraggingSeekbar = true
                                                        val target = (ratio * uiState.durationMs).toLong().coerceIn(0L, uiState.durationMs)
                                                        scrubbedPositionMs = target
                                                        smoothPositionMs = target
                                                        if (persistentConfig.liveSeekPreview) {
                                                            exoPlayer.seekTo(target)
                                                        }
                                                    }
                                                },
                                                onValueChangeFinished = {
                                                    if (!uiState.isAntiCheatLocked) {
                                                        controlInteractionNonce++
                                                        exoPlayer.seekTo(scrubbedPositionMs)
                                                        smoothPositionMs = scrubbedPositionMs
                                                        uiState = uiState.copy(currentPositionMs = scrubbedPositionMs)
                                                        isDraggingSeekbar = false
                                                        prefsRepo.saveVideoProgress(
                                                            id = mediaItem.id,
                                                            uri = mediaItem.uriString,
                                                            title = mediaItem.title,
                                                            durationMs = uiState.durationMs,
                                                            positionMs = scrubbedPositionMs,
                                                            thumbnailRes = mediaItem.posterRes
                                                        )
                                                    }
                                                },
                                                colors = SliderDefaults.colors(
                                                    thumbColor = if (uiState.isAntiCheatLocked) Color(0xFF64748B) else NeonCyan,
                                                    activeTrackColor = if (uiState.isAntiCheatLocked) Color(0xFF64748B) else NeonCyan,
                                                    inactiveTrackColor = Color(0x33FFFFFF),
                                                    disabledThumbColor = Color(0xFF64748B),
                                                    disabledActiveTrackColor = Color(0xFF475569)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(seekbarBoxHeight)
                                                    .testTag("video_player_progress_slider")
                                            )

                                            // Interactive Neon Dots on seekbar track
                                            if (uiState.durationMs > 0L && displayedBookmarks.isNotEmpty()) {
                                                Canvas(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp)
                                                        .height(seekbarBoxHeight)
                                                ) {
                                                    val trackWidth = size.width
                                                    val centerY = size.height / 2f
                                                    displayedBookmarks.forEach { bmMs ->
                                                        val bmRatio = (bmMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
                                                        val x = bmRatio * trackWidth
                                                        // Neon outer glow
                                                        drawCircle(
                                                            color = Color(0x9900FFCC).copy(alpha = (0.6f * glowScale).coerceIn(0.2f, 1f)),
                                                            radius = 5.5.dp.toPx() * thicknessScale,
                                                            center = Offset(x, centerY)
                                                        )
                                                        // Inner diamond/dot
                                                        drawCircle(
                                                            color = Color(0xFFFFD700),
                                                            radius = 3.dp.toPx() * thicknessScale,
                                                            center = Offset(x, centerY)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Reversed Timestamps Row (RTL Mirrored)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // In RTL: This appears on the Right
                                    Text(
                                        text = formatDuration(displayMs),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    )

                                    // Dynamic Animated Anti-Cheat Status & Goal Celebration
                                    AnimatedVisibility(
                                        visible = uiState.isAntiCheatLocked || showGoalCelebrationBanner,
                                        enter = fadeIn(tween(300)) + scaleIn(),
                                        exit = fadeOut(tween(600)) + scaleOut(targetScale = 0.7f)
                                    ) {
                                        if (showGoalCelebrationBanner) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Goal Achieved",
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "تم تحقيق الهدف اليومي! فك قفل التمرير",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = NeonCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        } else if (uiState.isAntiCheatLocked) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    triggerStrikeLockAlert("Seeking Anti-Cheat")
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFB74D),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Anti-Cheat Active (Seek Locked)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFFFFB74D),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    if (!uiState.isAntiCheatLocked && !showGoalCelebrationBanner) {
                                        Text(
                                            text = if (isRtlLayout) "اضغط مطولاً: 2x • سحب يميناً للسرعة" else "Hold: 2x • Drag right for speed",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .padding(horizontal = 8.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }

                                    // In RTL: This appears on the Left
                                    Text(
                                        text = formatDuration(uiState.durationMs),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                }

                                @Composable
                                fun BottomActionsRowView() {
                                    // Bottom Action Row (Speed options + Rotate + Audio/Subtitles + PiP)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Speed Selector Badges
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val speedOptions = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
                                            speedOptions.forEach { speed ->
                                                val isSelected = uiState.playbackSpeed == speed
                                                Surface(
                                                    shape = RoundedCornerShape(7.dp),
                                                    color = if (isSelected) NeonCyan else Color(0x22FFFFFF),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) NeonCyan else Color(0x22FFFFFF)
                                                    ),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            uiState = uiState.copy(playbackSpeed = speed)
                                                            exoPlayer.playbackParameters = PlaybackParameters(speed)
                                                            playerAmbientViewModel.setPlaybackSpeed(speed)
                                                        }
                                                        .testTag("speed_option_${speed}x")
                                                ) {
                                                    Text(
                                                        text = "${speed}x",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (isSelected) Charcoal950 else Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.5.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Rotate + Audio/Subtitle + PiP actions
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(7.dp),
                                                color = Color(0x2600FFCC),
                                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        controlInteractionNonce++
                                                        val act = context as? Activity
                                                        val targetOrientation = if (isLandscape) {
                                                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                                        } else {
                                                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                                        }
                                                        act?.requestedOrientation = targetOrientation
                                                    }
                                                    .testTag("video_player_rotate_frame_button")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ScreenRotation,
                                                        contentDescription = if (isLandscape) "Switch to Portrait" else "Switch to Landscape",
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isRtlLayout) "تدوير" else "Rotate",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = NeonCyan,
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    showAudioSubtitleSheet = true
                                                },
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .testTag("audio_subtitles_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Subtitles,
                                                    contentDescription = "Audio and Subtitles",
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }

                                            val isPipLocked = !isPremiumActivated
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clickable {
                                                        if (isPipLocked) {
                                                            triggerPremiumFeatureLock("وضع صورة داخل صورة (PiP)")
                                                        } else if (!prefsRepo.isPipModeEnabled()) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            bookmarkToastMessage = if (isRtlLayout) "وضع PiP متوقف من إعدادات النظام" else "PiP mode is turned off in System Settings"
                                                        } else {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            triggerPictureInPicture(context)
                                                        }
                                                    }
                                                    .testTag("pip_trigger_button"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PictureInPicture,
                                                    contentDescription = "Picture in Picture",
                                                    tint = if (isPipLocked) Color(0xFF64748B) else Color.White,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                                if (isPipLocked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Locked",
                                                        tint = Color(0xFFFFD700),
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .align(Alignment.TopEnd)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (persistentConfig.seekbarBelowControls) {
                                    BottomActionsRowView()
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SeekbarTrackView()
                                } else {
                                    SeekbarTrackView()
                                    Spacer(modifier = Modifier.height(4.dp))
                                    BottomActionsRowView()
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // EXACT GEOMETRIC CENTER CONTROLS:
                // Rewind 10s on LEFT, Play/Pause in CENTER, Forward 10s on RIGHT
                // Anchored to Alignment.Center of the full-screen Box in both Portrait & Landscape
                // -------------------------------------------------------------
                val isAnyDragActive = isDraggingGesture || isDraggingSeekbar || gestureOverlayType != null || isHoldSpeedActive
                if (!isAnyDragActive) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (uiState.isAntiCheatLocked) {
                                        triggerStrikeLockAlert("Seeking Anti-Cheat")
                                    } else {
                                        controlInteractionNonce++
                                        val skipSeconds = persistentConfig.doubleTapSeekSeconds.coerceIn(5, 30)
                                        val skipMs = skipSeconds * 1000L
                                        val target = (exoPlayer.currentPosition - skipMs).coerceAtLeast(0L)
                                        exoPlayer.seekTo(target)
                                        smoothPositionMs = target
                                        uiState = uiState.copy(currentPositionMs = target)
                                        triggerDirectionalEdgeGlow(direction = -1)
                                    }
                                },
                                modifier = Modifier
                                    .size(54.dp)
                                    .testTag("video_player_rewind_10s_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Rewind 10s (Left)",
                                    tint = if (uiState.isAntiCheatLocked) Color(0xFF64748B) else Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(36.dp))

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    controlInteractionNonce++
                                    togglePlayPauseWithFeedback(fromDoubleTap = false)
                                },
                                modifier = Modifier
                                    .size(76.dp)
                                    .testTag("video_player_center_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(36.dp))

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (uiState.isAntiCheatLocked) {
                                        triggerStrikeLockAlert("Seeking Anti-Cheat")
                                    } else {
                                        controlInteractionNonce++
                                        val skipSeconds = persistentConfig.doubleTapSeekSeconds.coerceIn(5, 30)
                                        val skipMs = skipSeconds * 1000L
                                        val target = (exoPlayer.currentPosition + skipMs).coerceAtMost(uiState.durationMs)
                                        exoPlayer.seekTo(target)
                                        smoothPositionMs = target
                                        uiState = uiState.copy(currentPositionMs = target)
                                        triggerDirectionalEdgeGlow(direction = 1)
                                    }
                                },
                                modifier = Modifier
                                    .size(54.dp)
                                    .testTag("video_player_forward_10s_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Forward 10s (Right)",
                                    tint = if (uiState.isAntiCheatLocked) Color(0xFF64748B) else Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // SETTINGS & TOOLS CAPSULE:
                // Portrait -> vertical bar expanding top-to-bottom on physical LEFT
                // Landscape -> horizontal bar expanding LEFT-TO-RIGHT (الى اليمين)
                // -------------------------------------------------------------
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val currentProgress = toolbarCollapseProgress.value
                        val infiniteTransition = rememberInfiniteTransition(label = "indicator")
                        val bounceOffset by infiniteTransition.animateFloat(
                            initialValue = -5f,
                            targetValue = 5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bounce"
                        )
                        val glowAnim by infiniteTransition.animateFloat(
                            initialValue = 0.45f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "glow"
                        )

                        @Composable
                        fun ToolbarActionButtons() {
                            // 1. Settings (الاعدادات)
                            SettingsGearButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    controlInteractionNonce++
                                    showSettingsSheet = true
                                }
                            )

                            // 2. Smart Bookmark Quick Add (Moved into Rotate button's slot)
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    controlInteractionNonce++
                                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                                    val dur = exoPlayer.duration.coerceAtLeast(uiState.durationMs).coerceAtLeast(0L)
                                    playerAmbientViewModel.addBookmark(
                                        videoId = mediaItem.id,
                                        uriString = mediaItem.uriString,
                                        videoTitle = formatDisplayTitle(mediaItem.title, uiState.hideFileExtensions),
                                        timestampMs = pos,
                                        durationMs = dur,
                                        thumbnailRes = mediaItem.posterRes
                                    ) { inserted ->
                                        bookmarkToastMessage = if (inserted) {
                                            "تم حفظ علامة مرجعية ⭐ (${formatDuration(pos)})"
                                        } else {
                                            "العلامة محفوظة مسبقاً (${formatDuration(pos)})"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("quick_add_bookmark_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(25.dp)
                                )
                            }

                            // 3. Aspect Ratio / Format (زر تنسيق الفيديو)
                            AspectRatioButton(
                                resizeMode = uiState.resizeMode,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    controlInteractionNonce++
                                    val nextMode = when (uiState.resizeMode) {
                                        VideoResizeMode.FIT -> VideoResizeMode.CROP
                                        VideoResizeMode.CROP -> VideoResizeMode.STRETCH
                                        VideoResizeMode.STRETCH -> VideoResizeMode.FIT
                                    }
                                    uiState = uiState.copy(resizeMode = nextMode)
                                    playerViewRef?.resizeMode = if (isAutoCropBlackBars) {
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    } else {
                                        nextMode.exoMode
                                    }
                                    playerAmbientViewModel.setResizeModeName(nextMode.name)
                                }
                            )

                            // 4. Ambient Video Glow Fill (زر أوضاع التمويه الأسود 5 Black Blur Modes)
                            AmbientGlowToggleChip(
                                isEnabled = persistentConfig.isAmbientGlowEnabled,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    controlInteractionNonce++
                                    showAmbientModePicker = !showAmbientModePicker
                                }
                            )

                            // 5. Screen Lock (زر قفل شاشة العرض)
                            ScreenLockButton(
                                isLocked = uiState.isScreenLocked,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val newLock = !uiState.isScreenLocked
                                    uiState = uiState.copy(
                                        isScreenLocked = newLock,
                                        areControlsVisible = false
                                    )
                                    if (newLock) {
                                        showUnlockButton = true
                                    }
                                }
                            )

                            // 6. Cinematic Frame Snapshot (زر التقاط لقطة سينمائية بدون إطار)
                            IconButton(
                                onClick = {
                                    controlInteractionNonce++
                                    takeFrameSnapshot()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("video_player_camera_snapshot_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Snapshot",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }

                        if (isLandscape) {
                            // Landscape: Frameless toolbar expanding LEFT -> RIGHT
                            val containerWidth = (312.dp - (260.dp * currentProgress))
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .stableStatusBarsPadding()
                                    .padding(start = 16.dp, top = 74.dp)
                                    .width(containerWidth)
                                    .height(52.dp)
                                    .clipToBounds()
                                    .pointerInput(isLeftToolbarCollapsed) {
                                        var dragDeltaX = 0f
                                        detectHorizontalDragGestures(
                                            onDragStart = {
                                                dragDeltaX = 0f
                                                controlInteractionNonce++
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            onDragEnd = {
                                                scope.launch {
                                                    val collapsedTarget: Boolean
                                                    if (dragDeltaX > 18f) {
                                                        toolbarCollapseProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                                        collapsedTarget = false
                                                    } else if (dragDeltaX < -18f) {
                                                        toolbarCollapseProgress.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                                                        collapsedTarget = true
                                                    } else {
                                                        val target = if (toolbarCollapseProgress.value > 0.45f) 1f else 0f
                                                        toolbarCollapseProgress.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
                                                        collapsedTarget = (target == 1f)
                                                    }
                                                    isLeftToolbarCollapsed = collapsedTarget
                                                    playerAmbientViewModel.setToolbarCollapsed(collapsedTarget)
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch {
                                                    val target = if (isLeftToolbarCollapsed) 1f else 0f
                                                    toolbarCollapseProgress.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
                                                }
                                            },
                                            onHorizontalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragDeltaX += dragAmount
                                                scope.launch {
                                                    val delta = -dragAmount / 260f
                                                    val newProgress = (toolbarCollapseProgress.value + delta).coerceIn(0f, 1f)
                                                    toolbarCollapseProgress.snapTo(newProgress)
                                                    isLeftToolbarCollapsed = newProgress > 0.5f
                                                }
                                            }
                                        )
                                    },
                                color = Color.Transparent,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(52.dp)
                                        .padding(horizontal = 7.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ToolbarActionButtons()
                                }
                            }

                            // Horizontal Expand/Collapse Arrow Indicator (Frameless)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .stableStatusBarsPadding()
                                    .padding(start = 16.dp + containerWidth + 4.dp, top = 82.dp)
                                    .graphicsLayer {
                                        translationX = bounceOffset
                                    }
                                    .clickable {
                                        scope.launch {
                                            controlInteractionNonce++
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val nextCollapsed = !isLeftToolbarCollapsed
                                            toolbarCollapseProgress.animateTo(
                                                if (nextCollapsed) 1f else 0f,
                                                spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                            isLeftToolbarCollapsed = nextCollapsed
                                            playerAmbientViewModel.setToolbarCollapsed(nextCollapsed)
                                        }
                                    }
                                    .testTag("left_toolbar_finger_indicator"),
                                shape = CircleShape,
                                color = Color.Transparent,
                                shadowElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLeftToolbarCollapsed) {
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight
                                        } else {
                                            Icons.AutoMirrored.Filled.KeyboardArrowLeft
                                        },
                                        contentDescription = if (isLeftToolbarCollapsed) "Expand settings to right" else "Collapse settings to left",
                                        tint = NeonCyan.copy(alpha = glowAnim),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            // Portrait: Frameless vertical bar expanding TOP -> BOTTOM on physical LEFT
                            val containerHeight = (312.dp - (260.dp * currentProgress))
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .stableStatusBarsPadding()
                                    .padding(start = 16.dp, top = 76.dp)
                                    .width(52.dp)
                                    .height(containerHeight)
                                    .clipToBounds()
                                    .pointerInput(isLeftToolbarCollapsed) {
                                        var dragDeltaY = 0f
                                        detectDragGestures(
                                            onDragStart = {
                                                dragDeltaY = 0f
                                                controlInteractionNonce++
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            onDragEnd = {
                                                scope.launch {
                                                    val collapsedTarget: Boolean
                                                    if (dragDeltaY < -18f) {
                                                        toolbarCollapseProgress.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                                                        collapsedTarget = true
                                                    } else if (dragDeltaY > 18f) {
                                                        toolbarCollapseProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                                        collapsedTarget = false
                                                    } else {
                                                        val target = if (toolbarCollapseProgress.value > 0.45f) 1f else 0f
                                                        toolbarCollapseProgress.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
                                                        collapsedTarget = (target == 1f)
                                                    }
                                                    isLeftToolbarCollapsed = collapsedTarget
                                                    playerAmbientViewModel.setToolbarCollapsed(collapsedTarget)
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch {
                                                    val target = if (isLeftToolbarCollapsed) 1f else 0f
                                                    toolbarCollapseProgress.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragDeltaY += dragAmount.y
                                                scope.launch {
                                                    val delta = -dragAmount.y / 260f
                                                    val newProgress = (toolbarCollapseProgress.value + delta).coerceIn(0f, 1f)
                                                    toolbarCollapseProgress.snapTo(newProgress)
                                                    isLeftToolbarCollapsed = newProgress > 0.5f
                                                }
                                            }
                                        )
                                    },
                                color = Color.Transparent,
                                shadowElevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(52.dp)
                                        .padding(horizontal = 6.dp, vertical = 7.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ToolbarActionButtons()
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .stableStatusBarsPadding()
                                    .padding(start = 24.dp, top = 76.dp + containerHeight + 4.dp)
                                    .graphicsLayer {
                                        translationY = bounceOffset
                                    }
                                    .clickable {
                                        scope.launch {
                                            controlInteractionNonce++
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val nextCollapsed = !isLeftToolbarCollapsed
                                            toolbarCollapseProgress.animateTo(
                                                if (nextCollapsed) 1f else 0f,
                                                spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                            isLeftToolbarCollapsed = nextCollapsed
                                            playerAmbientViewModel.setToolbarCollapsed(nextCollapsed)
                                        }
                                    }
                                    .testTag("left_toolbar_finger_indicator"),
                                shape = CircleShape,
                                color = Color.Transparent,
                                shadowElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLeftToolbarCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = if (isLeftToolbarCollapsed) "Swipe down to expand" else "Swipe up to collapse",
                                        tint = NeonCyan.copy(alpha = glowAnim),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // FLOATING SCREEN UNLOCK BUTTON (Elevated higher to completely avoid bottom seekbar/scrubber)
        // =========================================================================
        AnimatedVisibility(
            visible = uiState.isScreenLocked && showUnlockButton,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(260)) + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 145.dp) // Elevated safely away from gesture scrubbing zone
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xF20E1424),
                border = BorderStroke(
                    1.6.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFB74D), NeonCyan, Color(0xFFFFB74D))
                    )
                ),
                shadowElevation = 22.dp,
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        uiState = uiState.copy(isScreenLocked = false, areControlsVisible = true)
                        showUnlockButton = false
                    }
                    .testTag("video_player_floating_unlock_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "انقر لفك قفل الشاشة",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }

        // =========================================================================
        // DYNAMIC STRIKE LOCK POPUP BANNER (Liquid Glass Backdrop & Glowing Border)
        // =========================================================================
        AnimatedVisibility(
            visible = showStrikeLockPopup,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .stableStatusBarsPadding()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xEE121826),
                border = BorderStroke(
                    1.4.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFB74D), NeonCyan, Color(0xFFFF5252))
                    )
                ),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStrikeLockPopup = false }
                    .testTag("strike_lock_alert_popup")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFB74D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نظام حماية السلسلة (مؤقت)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strikeLockPopupMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SETTINGS BOTTOM SHEET (Hide Extensions Toggle, Ambient Glow, Strike Features)
        // =========================================================================
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = Color(0xF20B0F19),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Player Settings & Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Hide File Extensions Toggle (CRITICAL REQUIREMENT)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (uiState.hideFileExtensions) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hide File Extensions (إخفاء الامتداد)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (uiState.hideFileExtensions) "Titles display clean without .mp4, .mkv" else "Show full filenames with extensions",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = uiState.hideFileExtensions,
                            onCheckedChange = { isChecked ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                uiState = uiState.copy(hideFileExtensions = isChecked)
                                prefsRepo.saveHideFileExtensions(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = Color(0xFF64748B),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("hide_file_extensions_switch")
                        )
                    }

                    // 2. Ambient Video Glow Fill (10 Pure-Cinema & Fade Modes synced with Store)
                    AmbientBlackBlurSettingsSection(
                        isEnabled = persistentConfig.isAmbientGlowEnabled,
                        selectedMode = effectiveAmbientMode,
                        isArabic = isRtlLayout,
                        unlockedModes = unlockedAmbientModes,
                        onToggleEnabled = { isChecked ->
                            uiState = uiState.copy(isAmbientGlowEnabled = isChecked)
                            playerAmbientViewModel.setAmbientGlowEnabled(isChecked)
                        },
                        onSelectMode = { mode ->
                            uiState = uiState.copy(isAmbientGlowEnabled = true)
                            playerAmbientViewModel.selectBlackBlurMode(mode)
                        },
                        onLockedModeAttempt = { mode ->
                            val title = if (isRtlLayout) mode.titleAr else mode.titleEn
                            bookmarkToastMessage = if (isRtlLayout) {
                                "🔒 افتح «$title» من المتجر ← Ambient Video Fill"
                            } else {
                                "🔒 Unlock \"$title\" in Shop → Ambient Video Fill"
                            }
                        }
                    )

                    // 2b. Auto-Crop Black Bars (Shader / Aspect ratio border crop)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Crop Black Bars (قص الحواف السوداء)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (isAutoCropBlackBars) "Enabled: Ultra-wide zoom crops borders" else "Disabled: Native aspect ratio",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = isAutoCropBlackBars,
                            onCheckedChange = { isChecked ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAutoCropBlackBars = isChecked
                                playerViewRef?.resizeMode = if (isChecked) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else uiState.resizeMode.exoMode
                                playerAmbientViewModel.setAutoCropBlackBars(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("auto_crop_black_bars_switch")
                        )
                    }

                    // 2c. Seekbar Below Bottom Control Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isRtlLayout) "شريط التقدم أسفل الأزرار" else "Seekbar Below Controls",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (isRtlLayout) "نقل شريط التمرير أسفل أزرار التحكم السفلية" else "Display timeline seekbar below bottom control buttons",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = persistentConfig.seekbarBelowControls,
                            onCheckedChange = { isChecked ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                playerAmbientViewModel.setSeekbarBelowControls(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = Color(0xFF64748B),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("seekbar_below_controls_switch")
                        )
                    }

                    // 3. Background Play (Unlocks via Aura Pulse Premium)
                    val isBackgroundPlayLocked = !isPremiumActivated
                    PlayerSettingItem(
                        icon = Icons.Default.GraphicEq,
                        title = "Background Audio Playback (التشغيل في الخلفية)",
                        subtitle = when {
                            isBackgroundPlayLocked -> "مغلق: يفتح عبر Aura Pulse Premium"
                            isBackgroundPlayActive -> "مفعل بـ Aura Pulse Premium (يعمل مع إغلاق الشاشة)"
                            else -> "مفتوح بـ Aura Pulse Premium • اضغط للتفعيل"
                        },
                        badge = when {
                            isBackgroundPlayLocked -> "PREMIUM"
                            isBackgroundPlayActive -> "ACTIVE ✓"
                            else -> "UNLOCKED"
                        },
                        isLocked = isBackgroundPlayLocked,
                        onLockedClick = { triggerPremiumFeatureLock("التشغيل في الخلفية (Background Play)") },
                        onUnlockedClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val next = !isBackgroundPlayActive
                            isBackgroundPlayActive = next
                            streakViewModel?.setBackgroundPlayEnabled(next) ?: prefsRepo.setBackgroundPlayEnabled(next)
                        }
                    )

                    // 4. Offline Sharing (Unlocks via Aura Pulse Premium)
                    val isSharingLocked = !isPremiumActivated
                    PlayerSettingItem(
                        icon = Icons.Default.Share,
                        title = "High-Speed Offline Sharing (مشاركة الفيديو)",
                        subtitle = if (isSharingLocked) "مغلق: يفتح عبر Aura Pulse Premium" else "مفتوح بـ Aura Pulse Premium • اضغط لمشاركة الفيديو",
                        badge = if (isSharingLocked) "PREMIUM" else "SHARE",
                        isLocked = isSharingLocked,
                        onLockedClick = { triggerPremiumFeatureLock("مشاركة الفيديو السريعة (Offline Sharing)") },
                        onUnlockedClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            runCatching {
                                val shareUri = LocalMediaScanner.resolveShareableUri(context, mediaItem.uriString)
                                if (shareUri != null) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/*"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        putExtra(Intent.EXTRA_SUBJECT, mediaItem.title)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, mediaItem.title))
                                } else {
                                    bookmarkToastMessage = if (isRtlLayout) "تعذر مشاركة هذا المصدر محلياً" else "This media source cannot be shared"
                                }
                            }
                        }
                    )

                    // 5. Hardware Acceleration
                    PlayerSettingItem(
                        icon = Icons.Default.GraphicEq,
                        title = "Hardware Acceleration",
                        subtitle = "GPU HW+ Decoder Active (120 FPS)",
                        badge = "ENABLED"
                    )

                    // 6. Display Mode
                    PlayerSettingItem(
                        icon = Icons.Default.AspectRatio,
                        title = "Display Mode",
                        subtitle = "Current: ${uiState.resizeMode.title}",
                        badge = uiState.resizeMode.name
                    )

                    // 7. Playback Speed
                    PlayerSettingItem(
                        icon = Icons.Default.Speed,
                        title = "Playback Speed",
                        subtitle = "${uiState.playbackSpeed}x Normal",
                        badge = "${uiState.playbackSpeed}x"
                    )

                    // 8. Video Frame Rotation
                    PlayerSettingItem(
                        icon = Icons.Default.Rotate90DegreesCw,
                        title = "Video Frame Rotation",
                        subtitle = "Angle: ${uiState.videoRotationDegrees}° (Tap to rotate +90°)",
                        badge = "${uiState.videoRotationDegrees}°"
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // =========================================================================
        // AUDIO & SUBTITLES BOTTOM SHEET
        // =========================================================================
        if (showAudioSubtitleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAudioSubtitleSheet = false },
                containerColor = Color(0xF20B0F19),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Audio & Subtitles",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "AUDIO TRACKS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("Stereo 2.0 (AAC)", "Surround 5.1 (Dolby Digital)", "Original Studio Audio").forEach { track ->
                        val isSelected = uiState.selectedAudioTrack == track
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    uiState = uiState.copy(selectedAudioTrack = track)
                                    playerAmbientViewModel.setSelectedAudioTrack(track)
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = track,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Volume Boost Slider
                    Text(
                        text = "AUDIO BOOST (${(audioBoostMultiplier * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = audioBoostMultiplier,
                        valueRange = 1.0f..2.0f,
                        steps = 9,
                        onValueChange = { boost ->
                            audioBoostMultiplier = boost
                            exoPlayer.volume = (uiState.volume * boost).coerceIn(0f, 2.0f)
                            playerAmbientViewModel.setAudioBoostMultiplier(boost)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("audio_boost_slider")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SUBTITLES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("Off", "English [CC]", "Arabic (العربية)", "French (Français)").forEach { sub ->
                        val isSelected = uiState.selectedSubtitle == sub
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    uiState = uiState.copy(selectedSubtitle = sub)
                                    playerAmbientViewModel.setSelectedSubtitle(sub)
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle Sync & Delay Fine-tuning Controls
                    Text(
                        text = "SUBTITLE SYNC DELAY (${if (subtitleDelayOffsetMs >= 0) "+" else ""}${subtitleDelayOffsetMs} ms)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-500L, -100L, 0L, 100L, 500L).forEach { delta ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (delta == 0L) Color(0x3300FFCC) else Color(0x22FFFFFF),
                                border = BorderStroke(1.dp, if (delta == 0L) NeonCyan else Color(0x44FFFFFF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val nextOffset = if (delta == 0L) 0L else subtitleDelayOffsetMs + delta
                                        subtitleDelayOffsetMs = nextOffset
                                        playerAmbientViewModel.setSubtitleDelayOffsetMs(nextOffset)
                                    }
                            ) {
                                Text(
                                    text = if (delta == 0L) "Reset" else "${if (delta > 0) "+" else ""}${delta}ms",
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // =========================================================================
        // CINEMATIC FRAME SNAPSHOT NOTIFICATION PILL
        // =========================================================================
        AnimatedVisibility(
            visible = showSnapshotToast,
            enter = fadeIn(tween(200)) + slideInVertically { -it },
            exit = fadeOut(tween(300)) + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .stableStatusBarsPadding()
                .padding(top = 18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xF20E1524),
                border = BorderStroke(1.4.dp, NeonCyan),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    snapshotBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Snapshot Preview",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تم التقاط الإطار السينمائي بنجاح 📷",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // =========================================================================
        // SMART BOOKMARK TOAST PILL
        // =========================================================================
        AnimatedVisibility(
            visible = bookmarkToastMessage != null,
            enter = fadeIn(tween(200)) + scaleIn(),
            exit = fadeOut(tween(300)) + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 120.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF20E1524),
                border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bookmarkToastMessage ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // =========================================================================
        // AURA PULSE PREMIUM UNLOCK DIALOG FOR LOCKED VIDEO PLAYER FEATURES
        // =========================================================================
        if (showPremiumUnlockDialog) {
            AlertDialog(
                onDismissRequest = { showPremiumUnlockDialog = false },
                containerColor = Color(0xF5121826),
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(34.dp)
                    )
                },
                title = {
                    Text(
                        text = "Aura Pulse Premium",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (lockedPremiumFeatureName.isNotBlank()) {
                                "ميزة \"$lockedPremiumFeatureName\" تتطلب Aura Pulse Premium.\n\nأدخل رمز التفعيل (Redeem Code) لفتح وضع PiP والتشغيل في الخلفية والمشاركة."
                            } else {
                                "أدخل رمز التفعيل الخاص بـ Aura Pulse Premium لفتح المزايا المتقدمة في مشغل الفيديو."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = playerPremiumCodeInput,
                            onValueChange = {
                                playerPremiumCodeInput = it
                                playerPremiumCodeError = null
                            },
                            singleLine = true,
                            placeholder = { Text("أدخل كود التفعيل...", color = Color(0xFF94A3B8)) },
                            isError = playerPremiumCodeError != null,
                            supportingText = {
                                playerPremiumCodeError?.let { err ->
                                    Text(err, color = Color(0xFFFF5252))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0x44FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_premium_redeem_input")
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (SecurityUtils.verifyPremiumRedeemCode(playerPremiumCodeInput)) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                streakViewModel?.setPremiumActivated(true) ?: prefsRepo.setPremiumActivated(true)
                                playerPremiumCodeInput = ""
                                playerPremiumCodeError = null
                                showPremiumUnlockDialog = false
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                playerPremiumCodeError = "كود التفعيل غير صحيح"
                            }
                        },
                        modifier = Modifier.testTag("player_activate_premium_button")
                    ) {
                        Text(
                            text = "تفعيل الكود 👑",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPremiumUnlockDialog = false }) {
                        Text(
                            text = "إغلاق",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
        // =========================================================================
        // 5-MODE BLACK BLUR AMBIENT VIDEO GLOW FILL OVERLAY PICKER
        // =========================================================================
        BlackBlurAmbientPickerDialog(
            visible = showAmbientModePicker,
            isEnabled = persistentConfig.isAmbientGlowEnabled,
            selectedMode = effectiveAmbientMode,
            isArabic = isRtlLayout,
            unlockedModes = unlockedAmbientModes,
            onToggleEnabled = { enabled ->
                uiState = uiState.copy(isAmbientGlowEnabled = enabled)
                playerAmbientViewModel.setAmbientGlowEnabled(enabled)
            },
            onSelectMode = { mode ->
                uiState = uiState.copy(isAmbientGlowEnabled = true)
                playerAmbientViewModel.selectBlackBlurMode(mode)
            },
            onLockedModeAttempt = { mode ->
                val title = if (isRtlLayout) mode.titleAr else mode.titleEn
                bookmarkToastMessage = if (isRtlLayout) {
                    "🔒 افتح «$title» من المتجر ← Ambient Video Fill"
                } else {
                    "🔒 Unlock \"$title\" in Shop → Ambient Video Fill"
                }
            },
            onDismiss = { showAmbientModePicker = false }
        )
    }
}

// -----------------------------------------------------------------------------
// REUSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun QualityBadgeChip(qualityText: String = "4K HDR") {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0x2200FFCC),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.8f))
    ) {
        Text(
            text = qualityText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = NeonCyan,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
private fun AmbientGlowToggleChip(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(38.dp)
            .testTag("video_player_ambient_glow_toggle")
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = "Ambient Video Glow Fill Modes",
            tint = if (isEnabled) Color(0xFFE2E8F0) else Color(0xFF64748B),
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun AspectRatioButton(
    resizeMode: VideoResizeMode,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(38.dp)
            .testTag("video_player_aspect_ratio_button")
    ) {
        Icon(
            imageVector = when (resizeMode) {
                VideoResizeMode.FIT -> Icons.Default.FitScreen
                VideoResizeMode.CROP -> Icons.Default.Crop
                VideoResizeMode.STRETCH -> Icons.Default.AspectRatio
            },
            contentDescription = "Aspect Ratio: ${resizeMode.title}",
            tint = NeonCyan,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun ScreenLockButton(
    isLocked: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(38.dp)
            .testTag("video_player_screen_lock_button")
    ) {
        Icon(
            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
            contentDescription = if (isLocked) "Unlock Screen" else "Lock Screen",
            tint = if (isLocked) Color(0xFFFFB74D) else Color.White,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun SettingsGearButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .testTag("video_player_settings_button")
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun PlayerSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    isLocked: Boolean = false,
    onLockedClick: (() -> Unit)? = null,
    onUnlockedClick: (() -> Unit)? = null
) {
    val clickAction = if (isLocked) onLockedClick else onUnlockedClick
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = clickAction != null) {
                clickAction?.invoke()
            }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isLocked) Color(0xFF64748B) else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isLocked) Color(0xFF94A3B8) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isLocked) Color(0xFFFFB74D) else Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isLocked) Color(0x33FFB74D) else Color(0x3300FFCC),
            border = BorderStroke(1.dp, if (isLocked) Color(0xFFFFB74D) else NeonCyan.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isLocked) Color(0xFFFFB74D) else NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

private fun triggerPictureInPicture(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        (context as? Activity)?.let { activity ->
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                activity.enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }
}

private fun formatDuration(millis: Long): String = LocalMediaScanner.formatDuration(millis)

/**
 * Store-unlocked Waveform Audio Seekbar:
 * - Renders 60 animated vertical bars via Compose Canvas & rememberInfiniteTransition
 * - Zero phase-seam harmonic multipliers (integer 1x/2x) + smooth play/pause transition
 * - Reads 10ms progressRatioProvider inside DrawScope for butter-smooth 60-120 FPS glide
 */
@Composable
private fun WaveformAudioSeekbar(
    mediaId: String,
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    activePrimaryColor: Color,
    activeSecondaryColor: Color,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barCount = 60
    val baseProfile = remember(mediaId) {
        val seed = (mediaId.hashCode() and 0xFFFF) / 65535f
        FloatArray(barCount) { index ->
            val norm = index / (barCount - 1f)
            val envelope = 0.34f + 0.66f * sin(norm * PI).toFloat()
            val harmonic = 0.52f + 0.48f * abs(sin(index * 0.43f + seed * 6.28f))
            (0.24f + 0.76f * envelope * harmonic).coerceIn(0.22f, 1.0f)
        }
    }

    var wavePhase by remember { mutableFloatStateOf(0f) }
    var glowClock by remember { mutableFloatStateOf(0f) }
    var motionWeight by remember { mutableFloatStateOf(if ((isPlaying || isDragging) && !isAntiCheatLocked) 1f else 0f) }
    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }

    val latestIsPlaying by androidx.compose.runtime.rememberUpdatedState(isPlaying)
    val latestIsDragging by androidx.compose.runtime.rememberUpdatedState(isDragging)
    val latestLocked by androidx.compose.runtime.rememberUpdatedState(isAntiCheatLocked)
    val latestProgressProvider by androidx.compose.runtime.rememberUpdatedState(progressRatioProvider)

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        val twoPi = (2.0 * PI).toFloat()
        smoothedRatio = latestProgressProvider().coerceIn(0f, 1f)
        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) {
                    lastNanos = now
                    return@withFrameNanos
                }
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now

                val active = (latestIsPlaying || latestIsDragging) && !latestLocked
                val targetMotion = if (active) 1f else 0f
                motionWeight += (targetMotion - motionWeight) * (1f - exp(-dt * (if (active) 7.5f else 4.5f)))

                val targetRatio = latestProgressProvider().coerceIn(0f, 1f)
                if (latestIsDragging) {
                    smoothedRatio = targetRatio
                } else {
                    val diff = targetRatio - smoothedRatio
                    smoothedRatio = if (abs(diff) > 0.08f) {
                        targetRatio
                    } else {
                        smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }
                }

                val speedFactor = if (latestIsDragging) 1.45f else 1f
                if (motionWeight > 0.002f) {
                    wavePhase = (wavePhase + dt * (twoPi / 1.6f) * speedFactor * motionWeight) % twoPi
                    glowClock = (glowClock + dt * (twoPi / 2.2f) * motionWeight) % twoPi
                }
            }
        }
    }

    val lockedColor = Color(0xFF64748B)
    val activeBarBrush = remember(activePrimaryColor, activeSecondaryColor, isAntiCheatLocked) {
        if (isAntiCheatLocked) {
            Brush.horizontalGradient(listOf(lockedColor, lockedColor))
        } else {
            Brush.horizontalGradient(
                listOf(
                    activePrimaryColor,
                    activeSecondaryColor.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.92f)
                )
            )
        }
    }
    val inactiveBarColor = Color(0x3DFFFFFF)
    val inactiveTrackColor = Color(0x2EFFFFFF)
    val thumbRingColor = if (isAntiCheatLocked) lockedColor else activePrimaryColor

    Canvas(
        modifier = modifier
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
                    val padPx = 9.dp.toPx()
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
    ) {
        val progressRatio = smoothedRatio.coerceIn(0f, 1f)
        val glowPulse = 0.89f + 0.11f * sin(glowClock)
        val thumbHaloAlpha = if (isAntiCheatLocked) 0.3f else ((0.14f + 0.26f * glowPulse * motionWeight) * glowScale).coerceIn(0.05f, 0.75f)
        val thumbHaloColor = if (isAntiCheatLocked) lockedColor.copy(alpha = 0.3f) else activePrimaryColor.copy(alpha = thumbHaloAlpha)
        val padPx = 9.dp.toPx()
        val trackWidth = (size.width - padPx * 2f).coerceAtLeast(1f)
        val centerY = size.height / 2f
        val thumbX = padPx + trackWidth * progressRatio
        val slotWidth = trackWidth / barCount
        val barWidth = (slotWidth * 0.56f * thicknessScale).coerceIn(1.4.dp.toPx(), 4.8.dp.toPx())
        val barCorner = barWidth / 2f
        val maxBarHeight = size.height * 0.80f
        val minBarHeight = 3.5.dp.toPx() * thicknessScale
        val swellZonePx = 24.dp.toPx()

        // Smooth baseline track beneath the pulsing bars
        drawLine(
            color = inactiveTrackColor,
            start = Offset(padPx, centerY),
            end = Offset(padPx + trackWidth, centerY),
            strokeWidth = 2.5.dp.toPx() * thicknessScale,
            cap = StrokeCap.Round
        )
        if (thumbX > padPx) {
            drawLine(
                brush = activeBarBrush,
                start = Offset(padPx, centerY),
                end = Offset(thumbX, centerY),
                strokeWidth = 3.dp.toPx() * thicknessScale,
                cap = StrokeCap.Round
            )
        }

        // 60 animated vertical sine-wave bars (settle to calm baseline when paused, resume full wave on play)
        for (i in 0 until barCount) {
            val barCenterX = padPx + (i + 0.5f) * slotWidth
            val distToThumb = abs(barCenterX - thumbX)
            val crestBoost = if (distToThumb < swellZonePx) {
                val proximity = 1f - (distToThumb / swellZonePx)
                1f + (if (isDragging) 0.44f else 0.24f) * proximity * motionWeight
            } else {
                1f
            }

            val calmBaseline = 0.30f
            val dynamicWave = if (motionWeight > 0.005f) {
                val primarySine = sin(wavePhase + i * 0.34f)
                val secondarySine = sin(wavePhase * 2f - i * 0.22f)
                0.56f + 0.44f * ((primarySine * 0.72f + secondarySine * 0.28f + 1f) * 0.5f)
            } else {
                calmBaseline
            }
            val waveNorm = calmBaseline + (dynamicWave - calmBaseline) * motionWeight

            val barHeight = (maxBarHeight * baseProfile[i] * waveNorm * crestBoost)
                .coerceIn(minBarHeight, size.height * 0.94f)
            val topY = centerY - barHeight / 2f
            val leftX = barCenterX - barWidth / 2f

            if (barCenterX <= thumbX) {
                drawRoundRect(
                    brush = activeBarBrush,
                    topLeft = Offset(leftX, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barCorner, barCorner)
                )
            } else {
                drawRoundRect(
                    color = inactiveBarColor,
                    topLeft = Offset(leftX, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barCorner, barCorner)
                )
            }
        }

        // Preserve Interactive Neon Bookmark Dots along seekbar
        if (durationMs > 0L && bookmarks.isNotEmpty()) {
            bookmarks.forEach { bmMs ->
                val bmRatio = (bmMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val bx = padPx + bmRatio * trackWidth
                drawCircle(
                    color = Color(0x9900FFCC),
                    radius = 5.dp.toPx(),
                    center = Offset(bx, centerY)
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 2.6.dp.toPx(),
                    center = Offset(bx, centerY)
                )
            }
        }

        // Luminous circular scrubber thumb (ring + bright core + ambient halo)
        val haloRadius = if (isDragging) 14.dp.toPx() else 11.dp.toPx()
        val ringRadius = if (isDragging) 8.5.dp.toPx() else 6.8.dp.toPx()
        val coreRadius = if (isDragging) 4.6.dp.toPx() else 3.6.dp.toPx()

        drawCircle(
            color = thumbHaloColor,
            radius = haloRadius,
            center = Offset(thumbX, centerY)
        )
        drawCircle(
            color = thumbRingColor,
            radius = ringRadius,
            center = Offset(thumbX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = coreRadius,
            center = Offset(thumbX, centerY)
        )
    }
}

/**
 * Store-unlocked Shooting Star Seekbar:
 * - Laser bolts & diagonal cosmic shooting stars continuously launch and collide with the current video position
 * - Triggers expanding plasma shockwaves, 8 radial impact sparks, and a 4-point star flare at thumbX
 * - Reads 10ms progressRatioProvider inside DrawScope for butter-smooth 60-120 FPS tracking
 */
@Composable
private fun ShootingStarSeekbar(
    progressRatioProvider: () -> Float,
    durationMs: Long,
    isPlaying: Boolean,
    isDragging: Boolean,
    isAntiCheatLocked: Boolean,
    activePrimaryColor: Color,
    activeSecondaryColor: Color,
    bookmarks: List<Long>,
    thicknessScale: Float = 1.0f,
    glowScale: Float = 1.0f,
    onLockedAttempt: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekUpdate: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var boltPhase by remember { mutableFloatStateOf(0f) }
    var diagPhase by remember { mutableFloatStateOf(0f) }
    var twinkleClock by remember { mutableFloatStateOf(0f) }
    var sparkRotation by remember { mutableFloatStateOf(0f) }
    var motionWeight by remember { mutableFloatStateOf(if ((isPlaying || isDragging) && !isAntiCheatLocked) 1f else 0f) }
    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }

    val latestIsPlaying by androidx.compose.runtime.rememberUpdatedState(isPlaying)
    val latestIsDragging by androidx.compose.runtime.rememberUpdatedState(isDragging)
    val latestLocked by androidx.compose.runtime.rememberUpdatedState(isAntiCheatLocked)
    val latestProgressProvider by androidx.compose.runtime.rememberUpdatedState(progressRatioProvider)

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        val twoPi = (2.0 * PI).toFloat()
        smoothedRatio = latestProgressProvider().coerceIn(0f, 1f)
        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) {
                    lastNanos = now
                    return@withFrameNanos
                }
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now

                val active = (latestIsPlaying || latestIsDragging) && !latestLocked
                val targetMotion = if (active) 1f else 0f
                motionWeight += (targetMotion - motionWeight) * (1f - exp(-dt * (if (active) 7.5f else 4.5f)))

                val targetRatio = latestProgressProvider().coerceIn(0f, 1f)
                if (latestIsDragging) {
                    smoothedRatio = targetRatio
                } else {
                    val diff = targetRatio - smoothedRatio
                    smoothedRatio = if (abs(diff) > 0.08f) {
                        targetRatio
                    } else {
                        smoothedRatio + diff * (1f - exp(-dt * 28f))
                    }
                }

                val dragBoost = if (latestIsDragging) 1.55f else 1f
                if (motionWeight > 0.002f) {
                    boltPhase = (boltPhase + dt * 0.67f * dragBoost * motionWeight) % 1f
                    diagPhase = (diagPhase + dt * 1.0f * dragBoost * motionWeight) % 1f
                    twinkleClock = (twinkleClock + dt * (twoPi / 1.5f) * motionWeight) % twoPi
                    sparkRotation = (sparkRotation + dt * 0.72f * dragBoost * motionWeight) % twoPi
                }
            }
        }
    }

    val lockedColor = Color(0xFF64748B)
    val laserCyan = if (isAntiCheatLocked) lockedColor else Color(0xFF00F0FF)
    val laserMagenta = if (isAntiCheatLocked) lockedColor else Color(0xFFFF4FD8)
    val starGold = if (isAntiCheatLocked) lockedColor else Color(0xFFFFD700)

    val beamBrush = remember(activePrimaryColor, activeSecondaryColor, isAntiCheatLocked) {
        if (isAntiCheatLocked) {
            Brush.horizontalGradient(listOf(lockedColor, lockedColor))
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF00F0FF),
                    activePrimaryColor,
                    Color(0xFFFF4FD8)
                )
            )
        }
    }

    Canvas(
        modifier = modifier
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
                    val padPx = 10.dp.toPx()
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
    ) {
        val progressRatio = smoothedRatio.coerceIn(0f, 1f)
        val twinkleCycle = 0.5f + 0.5f * sin(twinkleClock)
        val padPx = 10.dp.toPx()
        val trackWidth = (size.width - padPx * 2f).coerceAtLeast(1f)
        val centerY = size.height / 2f
        val thumbX = padPx + trackWidth * progressRatio

        // 1. Cosmic stars along the unplayed rail (twinkle during playback, calm static starlight when paused)
        for (s in 0 until 12) {
            val sx = padPx + ((s * 0.193f) % 1f) * trackWidth
            if (sx > thumbX + 6.dp.toPx()) {
                val sy = centerY + (if (s % 2 == 0) -1f else 1f) * (5.dp.toPx() + (s % 3) * 3.dp.toPx())
                val twinkle = if (motionWeight > 0.01f) {
                    0.50f * abs(sin(twinkleClock + s * 0.9f)) * motionWeight
                } else {
                    0f
                }
                val alpha = 0.14f + twinkle
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = if (s % 3 == 0) 1.5.dp.toPx() else 1.dp.toPx(),
                    center = Offset(sx, sy)
                )
            }
        }

        // 2. Unplayed dark cosmic rail
        drawLine(
            color = Color(0x2BFFFFFF),
            start = Offset(padPx, centerY),
            end = Offset(padPx + trackWidth, centerY),
            strokeWidth = 2.8.dp.toPx() * thicknessScale,
            cap = StrokeCap.Round
        )

        // 3. Charged laser progress beam (outer plasma bloom calms down on pause)
        if (thumbX > padPx) {
            drawLine(
                color = laserCyan.copy(alpha = ((0.10f + 0.16f * motionWeight) * glowScale).coerceIn(0.04f, 0.55f)),
                start = Offset(padPx, centerY),
                end = Offset(thumbX, centerY),
                strokeWidth = (6.dp.toPx() + 3.5.dp.toPx() * motionWeight) * thicknessScale,
                cap = StrokeCap.Round
            )
            drawLine(
                brush = beamBrush,
                start = Offset(padPx, centerY),
                end = Offset(thumbX, centerY),
                strokeWidth = (3.6.dp.toPx() + 0.4.dp.toPx() * motionWeight) * thicknessScale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.65f + 0.23f * motionWeight),
                start = Offset(padPx, centerY),
                end = Offset(thumbX, centerY),
                strokeWidth = 1.4.dp.toPx() * thicknessScale,
                cap = StrokeCap.Round
            )
        }

        // 4. Laser Emitter Nozzle on the left origin (dims & stops pulsing when paused)
        if (!isAntiCheatLocked) {
            drawCircle(
                color = laserCyan.copy(alpha = 0.14f + (0.21f + 0.25f * twinkleCycle) * motionWeight),
                radius = 4.dp.toPx() + 1.5.dp.toPx() * motionWeight,
                center = Offset(padPx, centerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.55f + 0.45f * motionWeight),
                radius = 2.2.dp.toPx(),
                center = Offset(padPx, centerY)
            )
        }

        // 5. Launching Laser Projectiles + Converging Cosmic Lasers colliding at thumbX (active only when playing/dragging)
        var peakImpact = if (isDragging) 0.55f * motionWeight else 0f
        if (!isAntiCheatLocked && motionWeight > 0.01f) {
            val span = (thumbX - padPx).coerceAtLeast(0f)
            if (span > 10.dp.toPx()) {
                val boltCount = 4
                for (b in 0 until boltCount) {
                    val t = (boltPhase + b * (1f / boltCount)) % 1f
                    val eased = t * (0.35f + 0.65f * t)
                    val headX = padPx + span * eased
                    val tailLen = (span * 0.26f).coerceIn(12.dp.toPx(), 42.dp.toPx())
                    val tailX = (headX - tailLen).coerceAtLeast(padPx)
                    val boltColor = if (b % 2 == 0) laserCyan else laserMagenta

                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, boltColor.copy(alpha = 0.9f * motionWeight), Color.White),
                            startX = tailX,
                            endX = headX
                        ),
                        start = Offset(tailX, centerY),
                        end = Offset(headX, centerY),
                        strokeWidth = 5.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = motionWeight),
                        radius = 2.5.dp.toPx(),
                        center = Offset(headX, centerY)
                    )

                    if (t > 0.84f) {
                        val imp = ((t - 0.84f) / 0.16f).coerceIn(0f, 1f) * motionWeight
                        if (imp > peakImpact) peakImpact = imp
                    }
                }
            }

            // Diagonal converging cosmic lasers slamming into (thumbX, centerY)
            for (d in 0 until 2) {
                val dt = (diagPhase + d * 0.5f) % 1f
                val signY = if (d == 0) -1f else 1f
                val originX = (thumbX - 46.dp.toPx()).coerceAtLeast(padPx)
                val originY = centerY + signY * (size.height * 0.42f)
                val curX = originX + (thumbX - originX) * dt
                val curY = originY + (centerY - originY) * dt
                val tailT = (dt - 0.30f).coerceAtLeast(0f)
                val tailX = originX + (thumbX - originX) * tailT
                val tailY = originY + (centerY - originY) * tailT
                val streakColor = if (d == 0) starGold else laserMagenta

                drawLine(
                    color = streakColor.copy(alpha = 0.85f * motionWeight),
                    start = Offset(tailX, tailY),
                    end = Offset(curX, curY),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = motionWeight),
                    radius = 2.dp.toPx(),
                    center = Offset(curX, curY)
                )
                if (dt > 0.85f) {
                    val imp = ((dt - 0.85f) / 0.15f).coerceIn(0f, 1f) * motionWeight
                    if (imp > peakImpact) peakImpact = imp
                }
            }
        }

        // 6. Bookmark Gold Markers
        if (durationMs > 0L && bookmarks.isNotEmpty()) {
            bookmarks.forEach { bmMs ->
                val bmRatio = (bmMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val bx = padPx + bmRatio * trackWidth
                drawCircle(
                    color = Color(0x9900FFCC),
                    radius = 5.dp.toPx(),
                    center = Offset(bx, centerY)
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 2.6.dp.toPx(),
                    center = Offset(bx, centerY)
                )
            }
        }

        // 7. Collision Shockwave + Radial Sparks + Star Burst at Current Video Position (stops completely on pause)
        if (!isAntiCheatLocked && motionWeight > 0.01f && peakImpact > 0.02f) {
            val shockRadius = (6.dp.toPx() + 15.dp.toPx() * peakImpact) * (if (isDragging) 1.25f else 1f)
            val shockAlpha = ((1f - peakImpact * 0.85f).coerceIn(0f, 1f)) * motionWeight
            drawCircle(
                color = laserCyan.copy(alpha = shockAlpha * 0.85f),
                radius = shockRadius,
                center = Offset(thumbX, centerY),
                style = Stroke(width = 1.9.dp.toPx())
            )
            for (sp in 0 until 8) {
                val angle = (sp * (PI / 4.0) + sparkRotation).toFloat()
                val innerR = 4.dp.toPx() + 5.dp.toPx() * peakImpact
                val outerR = innerR + (8.dp.toPx() + (if (isDragging) 4.dp.toPx() else 0f)) * (1f - peakImpact * 0.3f)
                drawLine(
                    color = (if (sp % 2 == 0) starGold else laserMagenta).copy(alpha = shockAlpha),
                    start = Offset(thumbX + cos(angle) * innerR, centerY + sin(angle) * innerR),
                    end = Offset(thumbX + cos(angle) * outerR, centerY + sin(angle) * outerR),
                    strokeWidth = 1.7.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // 8. Luminous Shooting-Star Collision Core Thumb (calms into clean cursor when paused)
        val haloRadius = (if (isDragging) 14.dp.toPx() else 9.5.dp.toPx() + 2.5.dp.toPx() * motionWeight) + 3.dp.toPx() * peakImpact
        val flareLen = ((if (isDragging) 12.dp.toPx() else 9.dp.toPx()) + 4.dp.toPx() * peakImpact) * motionWeight
        drawCircle(
            color = laserMagenta.copy(
                alpha = if (isAntiCheatLocked) 0.25f else (0.14f + (0.20f + 0.30f * peakImpact) * motionWeight)
            ),
            radius = haloRadius,
            center = Offset(thumbX, centerY)
        )
        if (!isAntiCheatLocked && motionWeight > 0.02f) {
            drawLine(
                color = Color.White.copy(alpha = 0.88f * motionWeight),
                start = Offset(thumbX - flareLen, centerY),
                end = Offset(thumbX + flareLen, centerY),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.88f * motionWeight),
                start = Offset(thumbX, centerY - flareLen),
                end = Offset(thumbX, centerY + flareLen),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawCircle(
            color = laserCyan,
            radius = if (isDragging) 8.2.dp.toPx() else 6.8.dp.toPx(),
            center = Offset(thumbX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = if (isDragging) 4.5.dp.toPx() else 3.6.dp.toPx(),
            center = Offset(thumbX, centerY)
        )
    }
}

/**
 * Store-unlocked Angry Snake Seekbar (Ultra-Smooth Edition):
 * - VSync-locked `withFrameNanos` velocity integration: zero phase snaps on pause, resume, or drag
 * - Zero-allocation continuous Quadratic Bezier spine (`Path.quadraticTo`) for liquid serpentine motion at 60-120 FPS
 * - Unified single-pass `awaitEachGesture` touch & horizontal drag seeking with instant drag-velocity rage response
 * - Smoothly calms down into a resting serpent when playback is paused
 */
@Composable
private fun AngrySnakeSeekbar(
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
    // Continuous VSync frame state (never wraps discontinuously!)
    var slitherAngle by remember { mutableFloatStateOf(0f) }
    var tongueAngle by remember { mutableFloatStateOf(0f) }
    var strikeClock by remember { mutableFloatStateOf(0f) }
    var smoothedMotion by remember { mutableFloatStateOf(if (isPlaying || isDragging) 1f else 0f) }
    var smoothedRage by remember { mutableFloatStateOf(0f) }
    var dragVelocityBoost by remember { mutableFloatStateOf(0f) }
    var smoothedRatio by remember { mutableFloatStateOf(progressRatioProvider().coerceIn(0f, 1f)) }

    val latestIsPlaying by androidx.compose.runtime.rememberUpdatedState(isPlaying)
    val latestIsDragging by androidx.compose.runtime.rememberUpdatedState(isDragging)
    val latestLocked by androidx.compose.runtime.rememberUpdatedState(isAntiCheatLocked)
    val latestProgressProvider by androidx.compose.runtime.rememberUpdatedState(progressRatioProvider)

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var lastRatio = latestProgressProvider().coerceIn(0f, 1f)
        smoothedRatio = lastRatio

        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) {
                    lastNanos = now
                    return@withFrameNanos
                }
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now

                val active = (latestIsPlaying || latestIsDragging) && !latestLocked
                val targetMotion = if (active) 1f else 0f
                val motionAlpha = 1f - exp(-dt * (if (active) 8.5f else 5.0f))
                smoothedMotion += (targetMotion - smoothedMotion) * motionAlpha

                // Smoothly track playback ratio at 60-120Hz (zero-latency when dragging)
                val targetRatio = latestProgressProvider().coerceIn(0f, 1f)
                if (latestIsDragging) {
                    val deltaRatio = abs(targetRatio - lastRatio) / dt
                    val impulse = (deltaRatio * 0.75f).coerceIn(0f, 1f)
                    dragVelocityBoost = max(dragVelocityBoost * exp(-dt * 6.5f), impulse)
                    smoothedRatio = targetRatio
                } else {
                    dragVelocityBoost *= exp(-dt * 5.5f)
                    val ratioDiff = targetRatio - smoothedRatio
                    smoothedRatio = if (abs(ratioDiff) > 0.08f) {
                        targetRatio
                    } else {
                        smoothedRatio + ratioDiff * (1f - exp(-dt * 28f))
                    }
                }
                lastRatio = targetRatio

                val targetRage = if (latestLocked) {
                    0f
                } else if (latestIsDragging) {
                    (0.55f + 0.45f * dragVelocityBoost).coerceIn(0f, 1f)
                } else {
                    0f
                }
                smoothedRage += (targetRage - smoothedRage) * (1f - exp(-dt * 7.5f))

                // Continuous velocity integration: zero phase jumps when toggling drag/play/pause!
                val slitherSpeed = (4.6f + 6.8f * smoothedRage) * smoothedMotion
                slitherAngle += dt * slitherSpeed
                tongueAngle += dt * (9.5f + 5.0f * smoothedRage) * smoothedMotion
                strikeClock += dt * (2.2f + 1.8f * smoothedRage) * smoothedMotion
            }
        }
    }

    val lockedColor = Color(0xFF64748B)
    val venomGreen = if (isAntiCheatLocked) lockedColor else Color(0xFF22C55E)
    val acidLime = if (isAntiCheatLocked) lockedColor else Color(0xFFA3E635)
    val rageCrimson = if (isAntiCheatLocked) lockedColor else Color(0xFFFF2E4C)
    val rageEmber = if (isAntiCheatLocked) lockedColor else Color(0xFFFF8A00)
    val darkScale = if (isAntiCheatLocked) Color(0xFF334155) else Color(0xFF0A2113)

    val preyPositions = remember { floatArrayOf(0.14f, 0.27f, 0.40f, 0.53f, 0.66f, 0.79f, 0.91f) }
    val segCount = 32
    val spineX = remember { FloatArray(segCount + 1) }
    val spineY = remember { FloatArray(segCount + 1) }
    val bodyPath = remember { Path() }
    val viperHeadPath = remember { Path() }
    val tonguePath = remember { Path() }

    Canvas(
        modifier = modifier
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
                    val padPx = 14.dp.toPx()
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
    ) {
        val motion = smoothedMotion.coerceIn(0f, 1f)
        val progressRatio = smoothedRatio.coerceIn(0f, 1f)
        val padPx = 14.dp.toPx()
        val trackWidth = (size.width - padPx * 2f).coerceAtLeast(1f)
        val centerY = size.height / 2f
        val thumbX = padPx + trackWidth * progressRatio

        // Compute organic rage intensity from drag, prey proximity, bite flare & periodic hiss
        var nearPreyBoost = 0f
        var recentBiteFlare = 0f
        for (i in preyPositions.indices) {
            val px = padPx + trackWidth * preyPositions[i]
            val dist = px - thumbX
            if (dist > 0f && dist < 26.dp.toPx()) {
                nearPreyBoost = max(nearPreyBoost, 1f - dist / 26.dp.toPx())
            } else if (dist <= 0f && dist > -18.dp.toPx()) {
                recentBiteFlare = max(recentBiteFlare, 1f - abs(dist) / 18.dp.toPx())
            }
        }
        val periodicHiss = if (motion > 0.01f) {
            max(0f, sin(strikeClock)).let { it * it } * 0.36f * motion
        } else 0f
        val rageLevel = (0.14f + smoothedRage * 0.86f + nearPreyBoost * 0.55f + recentBiteFlare * 0.7f + periodicHiss)
            .coerceIn(0f, 1f) * (if (isAntiCheatLocked) 0f else (0.12f + 0.88f * motion))

        // 1. Unplayed dark venom rail
        drawLine(
            color = if (isAntiCheatLocked) Color(0x2864748B) else Color(0x3022C55E),
            start = Offset(padPx, centerY),
            end = Offset(padPx + trackWidth, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. Prey energy orbs along unplayed track (smoothly tremble as snake nears)
        if (!isAntiCheatLocked) {
            for (i in preyPositions.indices) {
                val orbX = padPx + trackWidth * preyPositions[i]
                if (orbX > thumbX + 3.dp.toPx()) {
                    val dist = orbX - thumbX
                    val danger = if (dist < 28.dp.toPx()) (1f - dist / 28.dp.toPx()) * motion else 0f
                    val jitterX = if (danger > 0.04f) sin(slitherAngle * 2.6f + i) * 1.2.dp.toPx() * danger else 0f
                    val jitterY = if (danger > 0.04f) cos(slitherAngle * 3.1f + i) * 1.2.dp.toPx() * danger else 0f
                    val pulse = 0.85f + 0.18f * sin(strikeClock * 2.1f + i) * motion
                    val orbCenter = Offset(orbX + jitterX, centerY + jitterY)

                    drawCircle(
                        color = rageCrimson.copy(alpha = 0.20f + 0.30f * danger),
                        radius = (5.2.dp.toPx() + 2.dp.toPx() * danger) * pulse,
                        center = orbCenter
                    )
                    drawCircle(
                        color = lerp(rageEmber, rageCrimson, danger),
                        radius = 2.6.dp.toPx() * pulse,
                        center = orbCenter
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 1.0.dp.toPx(),
                        center = orbCenter
                    )
                }
            }
        }

        // 3. Spatial traveling serpentine spine (anchored to world coordinates near head for genuine gliding motion)
        val span = (thumbX - padPx).coerceAtLeast(0f)
        val wavelengthPx = 42.dp.toPx()
        val maxAmp = (4.4.dp.toPx() + 3.2.dp.toPx() * rageLevel) * motion

        for (idx in 0..segCount) {
            val t = idx.toFloat() / segCount.toFloat()
            val x = padPx + span * t
            val env = sin(t * PI.toFloat()).let { it * 0.72f + 0.28f * t } * (0.30f + 0.70f * t)
            // World-anchored spatial wave so waves glide seamlessly backward as the snake moves forward
            val spatialPhase = ((thumbX - x) / wavelengthPx) * (2f * PI.toFloat())
            val y = centerY + sin(slitherAngle - spatialPhase) * maxAmp * env
            spineX[idx] = x
            spineY[idx] = y
        }

        val headPos = Offset(spineX[segCount], spineY[segCount])
        val neckPos = Offset(spineX[segCount - 2], spineY[segCount - 2])
        val headAngleRad = if (span > 6.dp.toPx()) {
            atan2((headPos.y - neckPos.y).toDouble(), (headPos.x - neckPos.x).toDouble()).toFloat()
        } else 0f
        val headAngleDeg = (headAngleRad * 180f / PI.toFloat()).coerceIn(-30f, 30f)

        // Rattlesnake tail tip vibration when angry
        if (span > 12.dp.toPx() && !isAntiCheatLocked && motion > 0.05f && rageLevel > 0.25f) {
            val rattleVib = sin(slitherAngle * 5.2f) * 1.8.dp.toPx() * rageLevel
            for (r in 0 until 3) {
                val rx = (padPx - r * 2.1.dp.toPx()).coerceAtLeast(3.dp.toPx())
                val ry = spineY[0] + rattleVib * (if (r % 2 == 0) 1f else -1f)
                drawCircle(
                    color = lerp(Color(0xFFD97706), rageEmber, rageLevel).copy(alpha = 0.85f * motion),
                    radius = (2.0f - r * 0.35f).dp.toPx(),
                    center = Offset(rx, ry)
                )
            }
        }

        // Build smooth quadratic Bezier Path along spine points (zero polygonal corners)
        if (span > 2.dp.toPx()) {
            bodyPath.reset()
            bodyPath.moveTo(spineX[0], spineY[0])
            for (i in 1 until segCount) {
                val midX = (spineX[i] + spineX[i + 1]) * 0.5f
                val midY = (spineY[i] + spineY[i + 1]) * 0.5f
                bodyPath.quadraticTo(spineX[i], spineY[i], midX, midY)
            }
            bodyPath.lineTo(headPos.x, headPos.y)

            val tailColor = lerp(venomGreen, rageEmber, rageLevel * 0.45f)
            val midColor = lerp(venomGreen, rageCrimson, rageLevel * 0.72f)
            val neckColor = lerp(acidLime, rageCrimson, (rageLevel * 1.05f).coerceIn(0f, 1f))
            val bodyBrush = Brush.horizontalGradient(
                colors = listOf(tailColor, midColor, neckColor),
                startX = padPx,
                endX = max(padPx + 4.dp.toPx(), headPos.x)
            )
            val bodyWidth = (if (isDragging) 6.2.dp.toPx() else 5.4.dp.toPx()) * thicknessScale

            // Outer venom / rage glow
            if (!isAntiCheatLocked) {
                drawPath(
                    path = bodyPath,
                    color = neckColor.copy(alpha = ((0.14f + 0.24f * rageLevel) * glowScale).coerceIn(0.05f, 0.72f)),
                    style = Stroke(width = bodyWidth + 6.dp.toPx() * thicknessScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            // Dark armored scale outline
            drawPath(
                path = bodyPath,
                color = darkScale,
                style = Stroke(width = bodyWidth + 1.8.dp.toPx() * thicknessScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Main silky gradient body
            drawPath(
                path = bodyPath,
                brush = bodyBrush,
                style = Stroke(width = bodyWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Smooth dorsal viper markings along the Bezier spine
            val scaleColor = lerp(acidLime, rageEmber, rageLevel).copy(alpha = 0.78f)
            for (i in 3 until segCount step 2) {
                val t = i.toFloat() / segCount.toFloat()
                drawCircle(
                    color = scaleColor,
                    radius = (1.1.dp.toPx() + 0.75.dp.toPx() * sin(t * PI.toFloat())),
                    center = Offset(spineX[i], spineY[i])
                )
            }
        }

        // 4. Bookmark Markers
        if (durationMs > 0L && bookmarks.isNotEmpty()) {
            bookmarks.forEach { bmMs ->
                val bmRatio = (bmMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val bx = padPx + bmRatio * trackWidth
                drawCircle(
                    color = Color(0x99FF8A00),
                    radius = 4.6.dp.toPx(),
                    center = Offset(bx, centerY)
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 2.4.dp.toPx(),
                    center = Offset(bx, centerY)
                )
            }
        }

        // 5. Bite Shockwave + Venom Droplet Sparks when raging or biting a prey orb
        val burstIntensity = max(recentBiteFlare, if (isDragging) 0.72f else 0f) * motion
        if (!isAntiCheatLocked && burstIntensity > 0.08f) {
            drawCircle(
                color = rageCrimson.copy(alpha = 0.42f * burstIntensity),
                radius = 10.dp.toPx() + 9.dp.toPx() * burstIntensity,
                center = headPos,
                style = Stroke(width = 1.8.dp.toPx())
            )
            for (v in 0 until 6) {
                val angle = (v * (PI / 3.0) + slitherAngle * 0.55).toFloat()
                val progressFrac = ((strikeClock * 0.65f + v * 0.17f) % 1f)
                val dist = 9.dp.toPx() + 10.dp.toPx() * progressFrac * burstIntensity
                val vx = headPos.x + cos(angle) * dist
                val vy = headPos.y + sin(angle) * dist
                drawCircle(
                    color = (if (v % 2 == 0) acidLime else rageCrimson).copy(alpha = (1f - progressFrac * 0.5f) * 0.85f * burstIntensity),
                    radius = 1.8.dp.toPx(),
                    center = Offset(vx, vy)
                )
            }
        }

        // 6. Expressive Angry Viper Head at headPos
        val headScale = if (isDragging) 1.18f else (1f + 0.08f * nearPreyBoost)
        rotate(degrees = headAngleDeg, pivot = headPos) {
            val hLen = 10.5.dp.toPx() * headScale
            val hBack = 7.5.dp.toPx() * headScale
            val hWidth = 6.8.dp.toPx() * headScale

            if (!isAntiCheatLocked) {
                drawCircle(
                    color = lerp(venomGreen, rageCrimson, rageLevel).copy(alpha = 0.20f + 0.35f * rageLevel),
                    radius = 12.dp.toPx() * headScale,
                    center = headPos
                )
            }

            // Flickering forked crimson tongue (continuous smooth sine extension)
            if (!isAntiCheatLocked && motion > 0.05f) {
                val flick = 0.5f + 0.5f * sin(tongueAngle)
                val tongueExtend = (4.dp.toPx() + 6.5.dp.toPx() * flick + 3.dp.toPx() * rageLevel) * motion
                val startX = headPos.x + hLen * 0.78f
                val forkStart = startX + tongueExtend * 0.65f
                val tipX = startX + tongueExtend
                val forkSpread = 2.6.dp.toPx() * motion
                tonguePath.reset()
                tonguePath.moveTo(startX, headPos.y)
                tonguePath.lineTo(forkStart, headPos.y)
                tonguePath.moveTo(forkStart, headPos.y)
                tonguePath.lineTo(tipX, headPos.y - forkSpread)
                tonguePath.moveTo(forkStart, headPos.y)
                tonguePath.lineTo(tipX, headPos.y + forkSpread)
                drawPath(
                    path = tonguePath,
                    color = rageCrimson.copy(alpha = 0.95f * motion),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Triangular Viper Head Skull Path
            viperHeadPath.reset()
            viperHeadPath.moveTo(headPos.x + hLen, headPos.y)
            viperHeadPath.lineTo(headPos.x + hLen * 0.15f, headPos.y - hWidth * 0.88f)
            viperHeadPath.lineTo(headPos.x - hBack, headPos.y - hWidth)
            viperHeadPath.lineTo(headPos.x - hBack * 0.55f, headPos.y)
            viperHeadPath.lineTo(headPos.x - hBack, headPos.y + hWidth)
            viperHeadPath.lineTo(headPos.x + hLen * 0.15f, headPos.y + hWidth * 0.88f)
            viperHeadPath.close()

            val headFill = lerp(venomGreen, rageCrimson, (rageLevel * 0.78f).coerceIn(0f, 1f))
            drawPath(
                path = viperHeadPath,
                color = headFill
            )
            drawPath(
                path = viperHeadPath,
                color = darkScale,
                style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round)
            )

            // Venom Fangs
            if (!isAntiCheatLocked && (rageLevel > 0.35f || nearPreyBoost > 0.25f || isDragging)) {
                val fangLen = 2.5.dp.toPx() * headScale
                drawLine(
                    color = Color.White,
                    start = Offset(headPos.x + hLen * 0.65f, headPos.y - 2.3.dp.toPx()),
                    end = Offset(headPos.x + hLen * 0.92f + fangLen * 0.35f, headPos.y - 1.2.dp.toPx()),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(headPos.x + hLen * 0.65f, headPos.y + 2.3.dp.toPx()),
                    end = Offset(headPos.x + hLen * 0.92f + fangLen * 0.35f, headPos.y + 1.2.dp.toPx()),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Glowing Fierce Eyes + Angled Angry Brows
            val eyeX = headPos.x + hLen * 0.08f
            val eyeOffsetY = hWidth * 0.44f
            val eyeRadius = 1.9.dp.toPx() * headScale
            val eyeColor = if (isAntiCheatLocked) Color(0xFF94A3B8) else lerp(Color(0xFFFACC15), Color(0xFFFF1744), rageLevel)

            for (side in listOf(-1f, 1f)) {
                val ey = headPos.y + side * eyeOffsetY
                drawCircle(
                    color = eyeColor,
                    radius = eyeRadius,
                    center = Offset(eyeX, ey)
                )
                drawLine(
                    color = Color.Black,
                    start = Offset(eyeX - eyeRadius * 0.45f, ey),
                    end = Offset(eyeX + eyeRadius * 0.45f, ey),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = darkScale,
                    start = Offset(eyeX - 2.4.dp.toPx(), ey + side * 2.2.dp.toPx()),
                    end = Offset(eyeX + 2.2.dp.toPx(), ey + side * 0.6.dp.toPx()),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

internal val HOLD_SPEED_TIERS: List<Float> = listOf(2.0f, 2.5f, 3.0f, 3.5f, 4.0f)

internal fun computeHoldSpeedStepPx(
    anchorX: Float,
    viewportWidthPx: Float,
    baseStepPx: Float,
    minStepPx: Float,
    edgeMarginPx: Float
): Float {
    val tierCount = HOLD_SPEED_TIERS.size
    if (tierCount <= 1) return baseStepPx
    val availableRightPx = (viewportWidthPx - anchorX - edgeMarginPx)
        .coerceAtLeast(minStepPx * (tierCount - 1))
    return minOf(baseStepPx, availableRightPx / (tierCount - 0.65f))
        .coerceAtLeast(minStepPx)
}

internal fun computeHoldDragProgress(
    deltaRightPx: Float,
    anchorX: Float,
    viewportWidthPx: Float,
    baseStepPx: Float,
    minStepPx: Float,
    edgeMarginPx: Float
): Float {
    val maxIndex = (HOLD_SPEED_TIERS.size - 1).coerceAtLeast(1)
    val stepPx = computeHoldSpeedStepPx(anchorX, viewportWidthPx, baseStepPx, minStepPx, edgeMarginPx)
    return (deltaRightPx / (stepPx * maxIndex)).coerceIn(0f, 1f)
}

internal fun computeHoldSpeedTierIndexWithHysteresis(
    deltaRightPx: Float,
    currentIndex: Int,
    anchorX: Float,
    viewportWidthPx: Float,
    baseStepPx: Float,
    minStepPx: Float,
    edgeMarginPx: Float
): Int {
    val maxIndex = HOLD_SPEED_TIERS.lastIndex
    if (maxIndex <= 0 || deltaRightPx <= 0f) return 0
    val stepPx = computeHoldSpeedStepPx(anchorX, viewportWidthPx, baseStepPx, minStepPx, edgeMarginPx)
    val clampedCurrent = currentIndex.coerceIn(0, maxIndex)
    val posInSteps = deltaRightPx / stepPx
    var next = clampedCurrent
    while (next < maxIndex && posInSteps >= (next + 1).toFloat()) {
        next++
    }
    while (next > 0 && posInSteps < (next - 0.16f)) {
        next--
    }
    return next.coerceIn(0, maxIndex)
}

@Composable
private fun HoldSpeedEdgeAura(
    visible: Boolean,
    speed: Float,
    modifier: Modifier = Modifier
) {
    val intensity by animateFloatAsState(
        targetValue = if (visible) ((speed - 1.5f) / 2.5f).coerceIn(0.35f, 1f) else 0f,
        animationSpec = tween(180),
        label = "hold_speed_edge_intensity"
    )
    if (intensity <= 0.01f) return

    val infiniteTransition = rememberInfiniteTransition(label = "hold_speed_aura_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (900f / (speed / 2f).coerceAtLeast(1f)).toInt().coerceIn(320, 900),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "hold_speed_streak_phase"
    )

    val accent = when {
        speed >= 3.5f -> Color(0xFFFF9100)
        speed >= 3.0f -> Color(0xFFFFD700)
        else -> NeonCyan
    }

    Canvas(modifier = modifier) {
        val edgeWidth = size.width * 0.14f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(accent.copy(alpha = 0.16f * intensity), Color.Transparent),
                startX = 0f,
                endX = edgeWidth
            )
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, accent.copy(alpha = 0.20f * intensity)),
                startX = size.width - edgeWidth,
                endX = size.width
            )
        )
        // Subtle horizontal speed-streaks along outer edges
        val streakCount = 5
        for (i in 0 until streakCount) {
            val phase = (pulse + i * 0.21f) % 1f
            val y = size.height * (0.18f + i * 0.16f)
            val xStart = size.width * (0.86f + phase * 0.12f)
            val streakLen = 28.dp.toPx() * intensity
            drawLine(
                color = accent.copy(alpha = (1f - phase) * 0.45f * intensity),
                start = Offset(xStart, y),
                end = Offset((xStart + streakLen).coerceAtMost(size.width), y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun HoldSpeedHudOverlay(
    speed: Float,
    dragProgress: Float,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val activeAccent = when {
        speed >= 3.5f -> Color(0xFFFF8A00)
        speed >= 3.0f -> Color(0xFFFFD700)
        speed >= 2.5f -> Color(0xFF38BDF8)
        else -> NeonCyan
    }
    val infiniteTransition = rememberInfiniteTransition(label = "hold_speed_chevrons")
    val chevronPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (760f / (speed / 2f).coerceAtLeast(1f)).toInt().coerceIn(280, 760),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "hold_speed_chevron_anim"
    )
    val animatedFill by animateFloatAsState(
        targetValue = dragProgress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium),
        label = "hold_speed_rail_fill"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            modifier = modifier
                .widthIn(min = 240.dp, max = 316.dp)
                .testTag("video_player_hold_speed_hud"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xEB0A101D),
            border = BorderStroke(
                width = 1.1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.72f),
                        activeAccent,
                        Color(0xFF0088FF).copy(alpha = 0.72f)
                    )
                )
            ),
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Header: Animated chevrons + Live Speed + Frameless Speed Boost label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-4).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (c in 0 until 3) {
                            val localAlpha = 0.35f + 0.65f * ((chevronPhase + (2 - c) * 0.33f) % 1f)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = activeAccent.copy(alpha = localAlpha.coerceIn(0.25f, 1f)),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    val formattedSpeed = if (speed % 1.0f == 0f) {
                        "${speed.toInt()}.0x"
                    } else {
                        "${speed}x"
                    }
                    Text(
                        text = formattedSpeed,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 0.4.sp
                        ),
                        modifier = Modifier.testTag("hold_speed_value_text")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isArabic) "تسريع مستمر" else "SPEED BOOST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = activeAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // 5-tier Drag-Right Speed Selector Rail (Left -> Right matches physical drag right)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x660F172A))
                        .border(0.7.dp, Color(0x24FFFFFF), RoundedCornerShape(10.dp))
                        .padding(2.dp)
                ) {
                    // Continuous glowing track behind the discrete tier labels
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        if (animatedFill > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedFill)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                NeonCyan.copy(alpha = 0.22f),
                                                activeAccent.copy(alpha = 0.34f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HOLD_SPEED_TIERS.forEach { tierSpeed ->
                            val isSelected = abs(speed - tierSpeed) < 0.05f
                            val tierLabel = if (tierSpeed % 1.0f == 0f) {
                                "${tierSpeed.toInt()}x"
                            } else {
                                "${tierSpeed}x"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) activeAccent else Color.Transparent
                                    )
                                    .padding(vertical = 3.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tierLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Charcoal950 else Color(0xFFB8C4D9),
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                        fontSize = 10.5.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionalSeekEdgeGlowOverlay(
    direction: Int,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    if (direction == 0 || intensity <= 0.01f) return
    val isForward = direction > 0

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier.testTag(
                if (isForward) "directional_edge_glow_forward"
                else "directional_edge_glow_rewind"
            )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height * 0.5f
                val anchorX = if (isForward) size.width * 1.03f else -size.width * 0.03f
                val bloomRadius = max(size.width * 0.38f, size.height * 0.54f)

                // Soft, luxurious white edge glow radiating inward from the tapped side
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.24f * intensity),
                            Color.White.copy(alpha = 0.11f * intensity),
                            Color.White.copy(alpha = 0.03f * intensity),
                            Color.Transparent
                        ),
                        center = Offset(anchorX, centerY),
                        radius = bloomRadius
                    ),
                    radius = bloomRadius,
                    center = Offset(anchorX, centerY)
                )

                val bandWidth = size.width * 0.18f
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = if (isForward) {
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.05f * intensity),
                                Color.White.copy(alpha = 0.16f * intensity)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.16f * intensity),
                                Color.White.copy(alpha = 0.05f * intensity),
                                Color.Transparent
                            )
                        },
                        startX = if (isForward) size.width - bandWidth else 0f,
                        endX = if (isForward) size.width else bandWidth
                    )
                )

                val rimX = if (isForward) size.width - 1.dp.toPx() else 1.dp.toPx()
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.22f * intensity),
                            Color.White.copy(alpha = 0.42f * intensity),
                            Color.White.copy(alpha = 0.22f * intensity),
                            Color.Transparent
                        )
                    ),
                    start = Offset(rimX, size.height * 0.14f),
                    end = Offset(rimX, size.height * 0.86f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun DoubleTapPlayPauseOverlay(
    isPlaying: Boolean,
    alpha: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(72.dp)
            .graphicsLayer {
                this.alpha = alpha.coerceIn(0f, 1f)
                this.scaleX = scale
                this.scaleY = scale
            }
            .testTag("double_tap_play_pause_badge"),
        shape = CircleShape,
        color = Color(0xDC0B111E),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.85f), NeonBlue.copy(alpha = 0.85f))
            )
        ),
        shadowElevation = 18.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPlaying) "Play" else "Pause",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}



