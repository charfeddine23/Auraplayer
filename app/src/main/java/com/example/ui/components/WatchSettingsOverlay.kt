package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.local.PlayerAmbientConfig
import com.example.data.local.SeekbarGlowIntensity
import com.example.data.local.SeekbarThickness
import com.example.data.local.VideoBookmarkEntity
import com.example.data.local.VideoBookmarkGroup
import com.example.data.local.WatchSettingsConfig
import com.example.data.local.WatchSortBy
import com.example.data.local.WatchSortDirection
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.PlayerSeekbarStyle
import com.example.ui.screens.WatchCategory
import com.example.ui.screens.WatchFileDisplayMode
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.TextDark
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import kotlin.math.PI
import kotlin.math.sin

private enum class WatchSettingsPage {
    MAIN_SETTINGS,
    AMBIENT_GLOW_MODES,
    BOOKMARKED_VIDEOS,
    VIDEO_TIMESTAMPS
}

private enum class WatchSettingsFilterTab(val titleEn: String, val titleAr: String) {
    ALL("All", "الكل"),
    WATCH_LIBRARY("Watch Library", "صفحة Watch"),
    VIDEO_PLAYER("Video Player", "مشغل الفيديو"),
    SEEKBAR("Seekbar", "شريط التمرير")
}

/**
 * Redesigned Cinema-Grade Watch Settings & Bookmarks Studio:
 * - Includes categorized Watch Library preferences, Video Player engine settings, and Seekbar studio controls
 * - Completely refactored visual hierarchy: luminous spotlight hero, squircle icon tiles, hairline-divided bento panels,
 *   segmented pill selectors, Black Blur visual swatches, and miniature Seekbar Canvas previews.
 * - Every option persists automatically without requiring any confirmation action.
 */
@Composable
fun WatchSettingsOverlay(
    visible: Boolean,
    isArabic: Boolean,
    watchSettings: WatchSettingsConfig,
    playerConfig: PlayerAmbientConfig,
    selectedCategory: WatchCategory,
    selectedFileDisplayMode: WatchFileDisplayMode,
    hideFileExtensions: Boolean,
    autoResumePlayback: Boolean,
    backgroundPlayEnabled: Boolean,
    isPremiumActivated: Boolean,
    activeSeekbarStyle: PlayerSeekbarStyle,
    unlockedSeekbarStyles: Set<PlayerSeekbarStyle>,
    unlockedAmbientModes: Set<AmbientBlackBlurMode> = AmbientBlackBlurMode.entries.toSet(),
    bookmarkGroups: List<VideoBookmarkGroup>,
    localVideos: List<LocalDeviceVideo>,
    // Watch Library Callbacks
    onSelectCategory: (WatchCategory) -> Unit,
    onSelectFileDisplayMode: (WatchFileDisplayMode) -> Unit,
    onToggleShowContinueWatching: (Boolean) -> Unit,
    onToggleShowSearchBar: (Boolean) -> Unit,
    onToggleHideFileExtensions: (Boolean) -> Unit,
    onToggleShowNewVideoBadge: (Boolean) -> Unit,
    onSelectNewVideoBadgeDays: (Int) -> Unit,
    onSelectSortBy: (WatchSortBy) -> Unit,
    onSelectSortDirection: (WatchSortDirection) -> Unit,
    onToggleHideShortClips: (Boolean) -> Unit,
    onToggleAutoFetchOnlineFolderCovers: (Boolean) -> Unit = {},
    onClearAndRefreshFolderCovers: () -> Unit = {},
    cachedFolderCoversCount: Int = 0,
    onRescanVideos: () -> Unit,
    onImportPhoneFiles: () -> Unit,
    onOpenGalleryPicker: () -> Unit,
    // Video Player Callbacks
    onToggleAmbientGlow: (Boolean) -> Unit,
    onSelectBlackBlurMode: (AmbientBlackBlurMode) -> Unit,
    onSelectResizeMode: (String) -> Unit,
    onToggleAutoCropBlackBars: (Boolean) -> Unit,
    onSelectPlaybackSpeed: (Float) -> Unit,
    onSelectDoubleTapSeekSeconds: (Int) -> Unit,
    onSelectControlsAutoHideSeconds: (Int) -> Unit,
    onToggleSwipeGestures: (Boolean) -> Unit,
    onSelectAudioBoost: (Float) -> Unit,
    onToggleAutoResumePlayback: (Boolean) -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    // Seekbar Callbacks
    onSelectSeekbarStyle: (PlayerSeekbarStyle) -> Unit,
    onSelectSeekbarThickness: (SeekbarThickness) -> Unit,
    onSelectSeekbarGlowIntensity: (SeekbarGlowIntensity) -> Unit,
    onToggleShowBookmarkMarkersOnSeekbar: (Boolean) -> Unit,
    onToggleLiveSeekPreview: (Boolean) -> Unit,
    onToggleSeekbarBelowControls: (Boolean) -> Unit = {},
    // Bookmarks Callbacks
    onPlayBookmarkAtTimestamp: (VideoBookmarkGroup, Long) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDeleteBookmarksForVideo: (VideoBookmarkGroup) -> Unit,
    onDeleteAllBookmarks: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val dirMultiplier = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    val smoothEnter = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
    val smoothExit = remember { CubicBezierEasing(0.4f, 0f, 1f, 1f) }

    var currentPage by remember { mutableStateOf(WatchSettingsPage.MAIN_SETTINGS) }
    var selectedVideoKey by remember { mutableStateOf<String?>(null) }
    var activeFilterTab by remember { mutableStateOf(WatchSettingsFilterTab.ALL) }

    LaunchedEffect(visible) {
        if (!visible) {
            currentPage = WatchSettingsPage.MAIN_SETTINGS
            selectedVideoKey = null
        }
    }

    val activeVideoGroup = remember(bookmarkGroups, selectedVideoKey) {
        selectedVideoKey?.let { key -> bookmarkGroups.find { it.videoKey == key } }
    }

    LaunchedEffect(currentPage, activeVideoGroup) {
        if (currentPage == WatchSettingsPage.VIDEO_TIMESTAMPS && activeVideoGroup == null) {
            currentPage = WatchSettingsPage.BOOKMARKED_VIDEOS
            selectedVideoKey = null
        }
    }

    val navigateOneStepBack: () -> Unit = {
        when (currentPage) {
            WatchSettingsPage.VIDEO_TIMESTAMPS -> {
                selectedVideoKey = null
                currentPage = WatchSettingsPage.BOOKMARKED_VIDEOS
            }
            WatchSettingsPage.BOOKMARKED_VIDEOS -> {
                currentPage = WatchSettingsPage.MAIN_SETTINGS
            }
            WatchSettingsPage.AMBIENT_GLOW_MODES -> {
                currentPage = WatchSettingsPage.MAIN_SETTINGS
            }
            WatchSettingsPage.MAIN_SETTINGS -> {
                onDismiss()
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(290, easing = smoothEnter)) { it * dirMultiplier },
        exit = slideOutHorizontally(tween(240, easing = smoothExit)) { it * dirMultiplier }
    ) {
        BackHandler(enabled = visible) {
            navigateOneStepBack()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(aura.background)
                .testTag("watch_settings_overlay")
        ) {
            AuraAtmosphericLayer()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .stableStatusBarsPadding()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Sleek Editorial Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navigateOneStepBack()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("watch_settings_back_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(aura.surfaceCard)
                                    .border(1.dp, aura.borderSubtle, RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isArabic) "رجوع" else "Back",
                                    tint = aura.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val unlockedAmbientCount = AmbientBlackBlurMode.entries.count {
                                it in unlockedAmbientModes || it.isUnlockedByDefault || isPremiumActivated
                            }
                            Text(
                                text = when (currentPage) {
                                    WatchSettingsPage.MAIN_SETTINGS -> if (isArabic) "استوديو المشاهدة" else "Watch Studio"
                                    WatchSettingsPage.AMBIENT_GLOW_MODES -> "Ambient Video Glow Fill"
                                    WatchSettingsPage.BOOKMARKED_VIDEOS -> if (isArabic) "مكتبة العلامات" else "Bookmarks Vault"
                                    WatchSettingsPage.VIDEO_TIMESTAMPS -> activeVideoGroup?.videoTitle
                                        ?: if (isArabic) "أوقات العلامات" else "Saved Timestamps"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = aura.textPrimary,
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = when (currentPage) {
                                    WatchSettingsPage.MAIN_SETTINGS -> if (isArabic) {
                                        "المكتبة • المشغل • شريط التمرير"
                                    } else {
                                        "Library • Video Player • Seekbar"
                                    }
                                    WatchSettingsPage.AMBIENT_GLOW_MODES -> if (isArabic) {
                                        "$unlockedAmbientCount / 10 متاح • حفظ تلقائي"
                                    } else {
                                        "$unlockedAmbientCount / 10 unlocked • Auto-saved"
                                    }
                                    WatchSettingsPage.BOOKMARKED_VIDEOS -> if (isArabic) {
                                        "${bookmarkGroups.size} فيديو • ${bookmarkGroups.sumOf { it.bookmarks.size }} لحظة محفوظة"
                                    } else {
                                        "${bookmarkGroups.size} videos • ${bookmarkGroups.sumOf { it.bookmarks.size }} saved moments"
                                    }
                                    WatchSettingsPage.VIDEO_TIMESTAMPS -> {
                                        val count = activeVideoGroup?.bookmarks?.size ?: 0
                                        if (isArabic) "$count علامات • اضغط للانتقال فوراً" else "$count marks • Tap to jump instantly"
                                    }
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    // Contextual Header Action Pill
                    when {
                        currentPage == WatchSettingsPage.MAIN_SETTINGS ||
                            currentPage == WatchSettingsPage.AMBIENT_GLOW_MODES -> {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = aura.accentEmeraldSoft,
                                border = BorderStroke(1.dp, aura.accentEmerald.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(aura.accentEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isArabic) "حفظ تلقائي" else "AUTO-SAVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.accentEmerald,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.5.sp
                                        )
                                    )
                                }
                            }
                        }
                        currentPage == WatchSettingsPage.BOOKMARKED_VIDEOS && bookmarkGroups.isNotEmpty() -> {
                            ClearBookmarksHeaderPill(
                                label = if (isArabic) "مسح الكل" else "Clear All",
                                onClick = onDeleteAllBookmarks,
                                tag = "clear_all_bookmarks_button"
                            )
                        }
                        currentPage == WatchSettingsPage.VIDEO_TIMESTAMPS && activeVideoGroup != null -> {
                            ClearBookmarksHeaderPill(
                                label = if (isArabic) "حذف علامات الفيديو" else "Clear Video",
                                onClick = { onDeleteBookmarksForVideo(activeVideoGroup) },
                                tag = "clear_video_bookmarks_button"
                            )
                        }
                    }
                }

                // Multi-page smooth content transitions
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        if (forward) {
                            ContentTransform(
                                targetContentEnter = slideInHorizontally(tween(280, easing = smoothEnter)) { it * dirMultiplier },
                                initialContentExit = slideOutHorizontally(tween(280, easing = smoothEnter)) { -(it * 0.22f * dirMultiplier).toInt() },
                                targetContentZIndex = 1f,
                                sizeTransform = SizeTransform(clip = true)
                            )
                        } else {
                            ContentTransform(
                                targetContentEnter = slideInHorizontally(tween(250, easing = smoothEnter)) { -(it * 0.22f * dirMultiplier).toInt() },
                                initialContentExit = slideOutHorizontally(tween(240, easing = smoothExit)) { it * dirMultiplier },
                                targetContentZIndex = -1f,
                                sizeTransform = SizeTransform(clip = true)
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds(),
                    label = "watch_settings_page_transition"
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(aura.background)
                    ) {
                        when (page) {
                        WatchSettingsPage.MAIN_SETTINGS -> {
                            WatchSettingsMainContent(
                                isArabic = isArabic,
                                activeFilterTab = activeFilterTab,
                                onSelectFilterTab = { activeFilterTab = it },
                                watchSettings = watchSettings,
                                playerConfig = playerConfig,
                                selectedCategory = selectedCategory,
                                selectedFileDisplayMode = selectedFileDisplayMode,
                                hideFileExtensions = hideFileExtensions,
                                autoResumePlayback = autoResumePlayback,
                                backgroundPlayEnabled = backgroundPlayEnabled,
                                isPremiumActivated = isPremiumActivated,
                                activeSeekbarStyle = activeSeekbarStyle,
                                unlockedSeekbarStyles = unlockedSeekbarStyles,
                                bookmarkGroups = bookmarkGroups,
                                onOpenBookmarks = {
                                    currentPage = WatchSettingsPage.BOOKMARKED_VIDEOS
                                },
                                onOpenAmbientGlowModes = {
                                    currentPage = WatchSettingsPage.AMBIENT_GLOW_MODES
                                },
                                onSelectCategory = onSelectCategory,
                                onSelectFileDisplayMode = onSelectFileDisplayMode,
                                onToggleShowContinueWatching = onToggleShowContinueWatching,
                                onToggleShowSearchBar = onToggleShowSearchBar,
                                onToggleHideFileExtensions = onToggleHideFileExtensions,
                                onToggleShowNewVideoBadge = onToggleShowNewVideoBadge,
                                onSelectNewVideoBadgeDays = onSelectNewVideoBadgeDays,
                                onSelectSortBy = onSelectSortBy,
                                onSelectSortDirection = onSelectSortDirection,
                                onToggleHideShortClips = onToggleHideShortClips,
                                onToggleAutoFetchOnlineFolderCovers = onToggleAutoFetchOnlineFolderCovers,
                                onClearAndRefreshFolderCovers = onClearAndRefreshFolderCovers,
                                cachedFolderCoversCount = cachedFolderCoversCount,
                                onRescanVideos = {
                                    onRescanVideos()
                                    onDismiss()
                                },
                                onImportPhoneFiles = {
                                    onImportPhoneFiles()
                                    onDismiss()
                                },
                                onOpenGalleryPicker = {
                                    onOpenGalleryPicker()
                                    onDismiss()
                                },
                                onSelectResizeMode = onSelectResizeMode,
                                onToggleAutoCropBlackBars = onToggleAutoCropBlackBars,
                                onSelectPlaybackSpeed = onSelectPlaybackSpeed,
                                onSelectDoubleTapSeekSeconds = onSelectDoubleTapSeekSeconds,
                                onSelectControlsAutoHideSeconds = onSelectControlsAutoHideSeconds,
                                onToggleSwipeGestures = onToggleSwipeGestures,
                                onSelectAudioBoost = onSelectAudioBoost,
                                onToggleAutoResumePlayback = onToggleAutoResumePlayback,
                                onToggleBackgroundPlay = onToggleBackgroundPlay,
                                onSelectSeekbarStyle = onSelectSeekbarStyle,
                                onSelectSeekbarThickness = onSelectSeekbarThickness,
                                onSelectSeekbarGlowIntensity = onSelectSeekbarGlowIntensity,
                                onToggleShowBookmarkMarkersOnSeekbar = onToggleShowBookmarkMarkersOnSeekbar,
                                onToggleLiveSeekPreview = onToggleLiveSeekPreview,
                                onToggleSeekbarBelowControls = onToggleSeekbarBelowControls
                            )
                        }

                        WatchSettingsPage.AMBIENT_GLOW_MODES -> {
                            AmbientGlowModesContent(
                                isArabic = isArabic,
                                playerConfig = playerConfig,
                                unlockedModes = unlockedAmbientModes,
                                isPremiumActivated = isPremiumActivated,
                                onToggleAmbientGlow = onToggleAmbientGlow,
                                onSelectBlackBlurMode = onSelectBlackBlurMode
                            )
                        }

                        WatchSettingsPage.BOOKMARKED_VIDEOS -> {
                            BookmarkedVideosListContent(
                                isArabic = isArabic,
                                bookmarkGroups = bookmarkGroups,
                                localVideos = localVideos,
                                onSelectVideo = { group ->
                                    selectedVideoKey = group.videoKey
                                    currentPage = WatchSettingsPage.VIDEO_TIMESTAMPS
                                },
                                onDeleteVideoBookmarks = onDeleteBookmarksForVideo
                            )
                        }

                        WatchSettingsPage.VIDEO_TIMESTAMPS -> {
                            VideoBookmarksTimestampsContent(
                                isArabic = isArabic,
                                group = activeVideoGroup,
                                localVideos = localVideos,
                                onPlayAtTimestamp = { timestampMs ->
                                    activeVideoGroup?.let { group ->
                                        onPlayBookmarkAtTimestamp(group, timestampMs)
                                    }
                                },
                                onDeleteBookmark = onDeleteBookmark
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
private fun ClearBookmarksHeaderPill(
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = aura.accentFlameSoft,
        border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.45f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = aura.accentFlame,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = aura.accentFlame,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun WatchSettingsMainContent(
    isArabic: Boolean,
    activeFilterTab: WatchSettingsFilterTab,
    onSelectFilterTab: (WatchSettingsFilterTab) -> Unit,
    watchSettings: WatchSettingsConfig,
    playerConfig: PlayerAmbientConfig,
    selectedCategory: WatchCategory,
    selectedFileDisplayMode: WatchFileDisplayMode,
    hideFileExtensions: Boolean,
    autoResumePlayback: Boolean,
    backgroundPlayEnabled: Boolean,
    isPremiumActivated: Boolean,
    activeSeekbarStyle: PlayerSeekbarStyle,
    unlockedSeekbarStyles: Set<PlayerSeekbarStyle>,
    bookmarkGroups: List<VideoBookmarkGroup>,
    onOpenBookmarks: () -> Unit,
    onOpenAmbientGlowModes: () -> Unit,
    onSelectCategory: (WatchCategory) -> Unit,
    onSelectFileDisplayMode: (WatchFileDisplayMode) -> Unit,
    onToggleShowContinueWatching: (Boolean) -> Unit,
    onToggleShowSearchBar: (Boolean) -> Unit,
    onToggleHideFileExtensions: (Boolean) -> Unit,
    onToggleShowNewVideoBadge: (Boolean) -> Unit,
    onSelectNewVideoBadgeDays: (Int) -> Unit,
    onSelectSortBy: (WatchSortBy) -> Unit,
    onSelectSortDirection: (WatchSortDirection) -> Unit,
    onToggleHideShortClips: (Boolean) -> Unit,
    onToggleAutoFetchOnlineFolderCovers: (Boolean) -> Unit = {},
    onClearAndRefreshFolderCovers: () -> Unit = {},
    cachedFolderCoversCount: Int = 0,
    onRescanVideos: () -> Unit,
    onImportPhoneFiles: () -> Unit,
    onOpenGalleryPicker: () -> Unit,
    onSelectResizeMode: (String) -> Unit,
    onToggleAutoCropBlackBars: (Boolean) -> Unit,
    onSelectPlaybackSpeed: (Float) -> Unit,
    onSelectDoubleTapSeekSeconds: (Int) -> Unit,
    onSelectControlsAutoHideSeconds: (Int) -> Unit,
    onToggleSwipeGestures: (Boolean) -> Unit,
    onSelectAudioBoost: (Float) -> Unit,
    onToggleAutoResumePlayback: (Boolean) -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    onSelectSeekbarStyle: (PlayerSeekbarStyle) -> Unit,
    onSelectSeekbarThickness: (SeekbarThickness) -> Unit,
    onSelectSeekbarGlowIntensity: (SeekbarGlowIntensity) -> Unit,
    onToggleShowBookmarkMarkersOnSeekbar: (Boolean) -> Unit,
    onToggleLiveSeekPreview: (Boolean) -> Unit,
    onToggleSeekbarBelowControls: (Boolean) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val totalBookmarksCount = remember(bookmarkGroups) { bookmarkGroups.sumOf { it.bookmarks.size } }
    var seekbarLockNotice by remember { mutableStateOf<String?>(null) }
    var isSeekbarSkinExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(seekbarLockNotice) {
        if (seekbarLockNotice != null) {
            kotlinx.coroutines.delay(2600L)
            seekbarLockNotice = null
        }
    }

    val showLibrarySections = activeFilterTab == WatchSettingsFilterTab.ALL ||
        activeFilterTab == WatchSettingsFilterTab.WATCH_LIBRARY
    val showPlayerSection = activeFilterTab == WatchSettingsFilterTab.ALL ||
        activeFilterTab == WatchSettingsFilterTab.VIDEO_PLAYER
    val showSeekbarSection = activeFilterTab == WatchSettingsFilterTab.ALL ||
        activeFilterTab == WatchSettingsFilterTab.SEEKBAR

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Category Quick-Jump Filter Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WatchSettingsFilterTab.entries.forEach { tab ->
                val isSelected = activeFilterTab == tab
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) aura.accentCyan else aura.surfaceCard,
                    animationSpec = tween(200),
                    label = "filter_pill_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (aura.isDark) TextDark else Color.White
                    } else {
                        aura.textSecondary
                    },
                    animationSpec = tween(200),
                    label = "filter_pill_text"
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = bgColor,
                    border = if (isSelected) null else BorderStroke(1.dp, aura.borderSubtle),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectFilterTab(tab)
                        }
                        .testTag("watch_settings_filter_${tab.name}")
                ) {
                    Text(
                        text = if (isArabic) tab.titleAr else tab.titleEn,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // =========================================================================
            // 1. SPOTLIGHT HERO: العلامة (Bookmarks Manager)
            // =========================================================================
            if (activeFilterTab == WatchSettingsFilterTab.ALL || activeFilterTab == WatchSettingsFilterTab.WATCH_LIBRARY) {
                item(key = "hero_bookmarks") {
                    BookmarksSpotlightHeroCard(
                        isArabic = isArabic,
                        videosCount = bookmarkGroups.size,
                        totalMarksCount = totalBookmarksCount,
                        onClick = onOpenBookmarks
                    )
                }
            }

            // =========================================================================
            // 2. WATCH LIBRARY DISPLAY & SORTING
            // =========================================================================
            if (showLibrarySections) {
                item(key = "section_library_display") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionEyebrowHeader(
                            title = if (isArabic) "العرض وتصميم المكتبة" else "LIBRARY DISPLAY & LAYOUT",
                            subtitle = if (isArabic) "تخصيص مظهر صفحة Watch" else "Customize Watch browser presentation",
                            accentColor = aura.accentCyan,
                            icon = Icons.Default.Tune
                        )

                        BentoSettingsPanel {
                            // Classification Segmented Track (Folder vs Files)
                            SegmentedLabelBlock(
                                title = if (isArabic) "تبويب التصنيف الافتراضي" else "Default View Tab",
                                accentColor = aura.accentCyan
                            ) {
                                SegmentedPillTrack(
                                    options = WatchCategory.entries,
                                    selected = selectedCategory,
                                    accentColor = aura.accentCyan,
                                    labelFor = { if (isArabic) it.titleAr else it.titleEn },
                                    onSelect = onSelectCategory
                                )
                            }

                            BentoDivider()

                            // Display Style Segmented Bento (List / Grid / Magazine / Gallery View) for Folders & Files
                            SegmentedLabelBlock(
                                title = if (isArabic) "نمط عرض المجلدات والفيديوهات" else "Folders & Videos Layout",
                                accentColor = aura.accentCyan
                            ) {
                                SegmentedPillTrack(
                                    options = WatchFileDisplayMode.entries,
                                    selected = selectedFileDisplayMode,
                                    accentColor = aura.accentCyan,
                                    labelFor = { if (isArabic) it.titleAr else it.titleEn },
                                    onSelect = onSelectFileDisplayMode
                                )
                            }

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.History,
                                iconTint = aura.accentCyan,
                                title = if (isArabic) "نافذة متابعة المشاهدة" else "Continue Watching Spotlight",
                                subtitle = if (isArabic) "عرض آخر فيديو شاهدته في أعلى صفحة Watch" else "Pin resume card above your library",
                                checked = watchSettings.showContinueWatching,
                                onCheckedChange = onToggleShowContinueWatching
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.Search,
                                iconTint = aura.accentCyan,
                                title = if (isArabic) "إظهار شريط البحث" else "Library Search Bar",
                                subtitle = if (isArabic) "تفعيل البحث السريع عن الفيديوهات" else "Quick title filtering bar in Watch",
                                checked = watchSettings.showSearchBar,
                                onCheckedChange = onToggleShowSearchBar
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.VisibilityOff,
                                iconTint = aura.accentCyan,
                                title = if (isArabic) "إخفاء امتداد الملفات" else "Clean File Titles",
                                subtitle = if (isArabic) "إخفاء صيغ مثل .mp4 و .mkv عن العناوين" else "Strip .mp4, .mkv extensions cleanly",
                                checked = hideFileExtensions,
                                onCheckedChange = onToggleHideFileExtensions
                            )

                            BentoDivider()

                            val activeNewDays = watchSettings.newVideoBadgeDays.coerceIn(1, 365)
                            EditorialSettingToggleRow(
                                icon = Icons.Default.NewReleases,
                                iconTint = aura.accentCyan,
                                title = if (isArabic) "شارة «NEW» للفيديوهات الجديدة" else "\"NEW\" Video Badge",
                                subtitle = if (watchSettings.showNewVideoBadge) {
                                    if (isArabic) {
                                        "تظهر تلقائياً على الفيديوهات المضافة خلال آخر ${formatArabicDaysLabel(activeNewDays)}"
                                    } else {
                                        "Shown on videos added within the last $activeNewDays ${if (activeNewDays == 1) "day" else "days"}"
                                    }
                                } else {
                                    if (isArabic) "مُعطّلة — إخفاء شارة NEW من البطاقات" else "Off — NEW badge hidden on video cards"
                                },
                                checked = watchSettings.showNewVideoBadge,
                                onCheckedChange = onToggleShowNewVideoBadge
                            )

                            AnimatedVisibility(visible = watchSettings.showNewVideoBadge) {
                                Column {
                                    BentoDivider()
                                    val newDaysPresets = remember { listOf(1, 3, 7, 14, 30) }
                                    SegmentedLabelBlock(
                                        title = if (isArabic) "مدة بقاء شارة NEW (الافتراضي 7 أيام)" else "NEW Badge Duration (Default: 7 Days)",
                                        accentColor = aura.accentCyan
                                    ) {
                                        SegmentedPillTrack(
                                            options = newDaysPresets,
                                            selected = activeNewDays,
                                            accentColor = aura.accentCyan,
                                            labelFor = { d -> if (isArabic) "${d} ي" else "${d}d" },
                                            onSelect = onSelectNewVideoBadgeDays
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isArabic) "تخصيص دقيق للأيام" else "Custom Duration",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = aura.textSecondary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.5.sp
                                            )
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = aura.surfaceSubtle,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (activeNewDays > 1) aura.borderSubtle else aura.borderMuted.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable(enabled = activeNewDays > 1) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        onSelectNewVideoBadgeDays((activeNewDays - 1).coerceAtLeast(1))
                                                    }
                                                    .testTag("new_badge_days_decrease")
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(34.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = if (isArabic) "إنقاص يوم" else "Decrease days",
                                                        tint = if (activeNewDays > 1) aura.textPrimary else aura.textTertiary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = aura.accentCyanSoft,
                                                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.45f)),
                                                modifier = Modifier.testTag("new_badge_days_value")
                                            ) {
                                                Text(
                                                    text = if (isArabic) formatArabicDaysLabel(activeNewDays) else "$activeNewDays ${if (activeNewDays == 1) "day" else "days"}",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = aura.accentCyan,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = aura.surfaceSubtle,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (activeNewDays < 365) aura.borderSubtle else aura.borderMuted.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable(enabled = activeNewDays < 365) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        onSelectNewVideoBadgeDays((activeNewDays + 1).coerceAtMost(365))
                                                    }
                                                    .testTag("new_badge_days_increase")
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(34.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = if (isArabic) "زيادة يوم" else "Increase days",
                                                        tint = if (activeNewDays < 365) aura.textPrimary else aura.textTertiary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.AutoAwesome,
                                iconTint = aura.accentCyan,
                                title = if (isArabic) "جلب أغلفة المجلدات تلقائياً (HD)" else "Auto HD Online Folder Covers",
                                subtitle = if (watchSettings.autoFetchOnlineFolderCovers) {
                                    if (isArabic) {
                                        "فلتر دقة صارم (600×800+ / 600×600+) وحفظ محلي للأوفلاين"
                                    } else {
                                        "Strict HD filter (≥600×800 posters / ≥600×600 art) cached locally"
                                    }
                                } else {
                                    if (isArabic) {
                                        "مُعطّل — استخدام صور الفيديو المحلية فقط"
                                    } else {
                                        "Off — local video frames & default folder covers only"
                                    }
                                },
                                checked = watchSettings.autoFetchOnlineFolderCovers,
                                onCheckedChange = onToggleAutoFetchOnlineFolderCovers
                            )

                            AnimatedVisibility(visible = watchSettings.autoFetchOnlineFolderCovers || cachedFolderCoversCount > 0) {
                                Column {
                                    BentoDivider()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onClearAndRefreshFolderCovers()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("refresh_clear_folder_covers_button"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(11.dp))
                                                    .background(aura.accentCyan.copy(alpha = 0.14f))
                                                    .border(0.8.dp, aura.accentCyan.copy(alpha = 0.35f), RoundedCornerShape(11.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = aura.accentCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isArabic) "تحديث وإعادة فحص أغلفة HD" else "Refresh & Clear HD Cover Cache",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = aura.textPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                                Text(
                                                    text = if (isArabic) {
                                                        "$cachedFolderCoversCount غلاف HD محفوظ محلياً للأوفلاين"
                                                    } else {
                                                        "$cachedFolderCoversCount verified HD covers cached for offline use"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = aura.textSecondary,
                                                        fontSize = 11.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = aura.accentCyanSoft,
                                            border = BorderStroke(0.8.dp, aura.accentCyan.copy(alpha = 0.45f))
                                        ) {
                                            Text(
                                                text = if (isArabic) "تحديث" else "SYNC",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = aura.accentCyan,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "section_library_sorting") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionEyebrowHeader(
                            title = if (isArabic) "الترتيب والفلترة" else "SORTING & FILTERING",
                            subtitle = if (isArabic) "تنظيم الفيديوهات حسب رغبتك" else "Control library order & duration filter",
                            accentColor = aura.accentViolet,
                            icon = Icons.Default.Sort
                        )

                        BentoSettingsPanel {
                            SegmentedLabelBlock(
                                title = if (isArabic) "ترتيب الفيديوهات حسب" else "Sort Library By",
                                accentColor = aura.accentViolet
                            ) {
                                SegmentedPillTrack(
                                    options = WatchSortBy.entries,
                                    selected = watchSettings.sortBy,
                                    accentColor = aura.accentViolet,
                                    labelFor = { if (isArabic) it.titleAr else it.titleEn },
                                    onSelect = onSelectSortBy
                                )
                            }

                            AnimatedVisibility(visible = watchSettings.sortBy != WatchSortBy.DEFAULT) {
                                Column {
                                    BentoDivider()
                                    SegmentedLabelBlock(
                                        title = if (isArabic) "اتجاه الترتيب" else "Sort Order",
                                        accentColor = aura.accentViolet
                                    ) {
                                        SegmentedPillTrack(
                                            options = WatchSortDirection.entries,
                                            selected = watchSettings.sortDirection,
                                            accentColor = aura.accentViolet,
                                            labelFor = { if (isArabic) it.titleAr else it.titleEn },
                                            onSelect = onSelectSortDirection
                                        )
                                    }
                                }
                            }

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.TimerOff,
                                iconTint = aura.accentViolet,
                                title = if (isArabic) "إخفاء المقاطع القصيرة (< 5 ثوانٍ)" else "Hide Short Clips (< 5s)",
                                subtitle = if (isArabic) "تجاهل المقاطع العابرة القصيرة جداً" else "Filter tiny GIFs & short clips under 5s",
                                checked = watchSettings.hideShortClips,
                                onCheckedChange = onToggleHideShortClips
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 3. VIDEO PLAYER SETTINGS (إعدادات مشغل الفيديو)
            // =========================================================================
            if (showPlayerSection) {
                item(key = "section_video_player") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionEyebrowHeader(
                            title = if (isArabic) "إعدادات مشغل الفيديو" else "VIDEO PLAYER ENGINE",
                            subtitle = if (isArabic) "التحكم الكامل بتجربة التشغيل والسينما" else "Cinema ambient blur, playback speed & gestures",
                            accentColor = aura.accentGold,
                            icon = Icons.Default.PlayCircleOutline
                        )

                        BentoSettingsPanel {
                            // Ambient Video Glow Fill Entry Button -> Opens dedicated sub-page with the 10 effects
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onOpenAmbientGlowModes()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .testTag("watch_settings_ambient_glow_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(aura.accentGold.copy(alpha = 0.14f))
                                            .border(0.8.dp, aura.accentGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BlurOn,
                                            contentDescription = null,
                                            tint = aura.accentGold,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Ambient Video Glow Fill",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = aura.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                        )
                                        Text(
                                            text = if (playerConfig.isAmbientGlowEnabled) {
                                                if (isArabic) playerConfig.blackBlurMode.titleAr else playerConfig.blackBlurMode.titleEn
                                            } else {
                                                if (isArabic) "معطل • خلفية OLED سوداء نقية" else "Off • Pure #000000 OLED black"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (playerConfig.isAmbientGlowEnabled) aura.accentGold else aura.textSecondary,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (playerConfig.isAmbientGlowEnabled) aura.accentGoldBg else aura.surfaceSubtle,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (playerConfig.isAmbientGlowEnabled) aura.accentGold.copy(alpha = 0.5f) else aura.borderMuted
                                        )
                                    ) {
                                        Text(
                                            text = if (playerConfig.isAmbientGlowEnabled) {
                                                if (isArabic) "مُفعّل" else "ON"
                                            } else {
                                                if (isArabic) "مُعطّل" else "OFF"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (playerConfig.isAmbientGlowEnabled) aura.accentGold else aura.textSecondary,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = aura.accentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            BentoDivider()

                            // Resize / Aspect Ratio
                            val resizeModes = remember { listOf("FIT", "CROP", "STRETCH") }
                            SegmentedLabelBlock(
                                title = if (isArabic) "أبعاد عرض الفيديو الافتراضية" else "Default Aspect Ratio",
                                accentColor = aura.accentGold
                            ) {
                                SegmentedPillTrack(
                                    options = resizeModes,
                                    selected = playerConfig.resizeModeName,
                                    accentColor = aura.accentGold,
                                    labelFor = { mode ->
                                        when (mode) {
                                            "CROP" -> if (isArabic) "ملء وقص" else "Crop"
                                            "STRETCH" -> if (isArabic) "تمديد" else "Stretch"
                                            else -> if (isArabic) "ملاءمة" else "Fit"
                                        }
                                    },
                                    onSelect = onSelectResizeMode
                                )
                            }

                            BentoDivider()

                            // Default Playback Speed
                            val speedOptions = remember { listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f) }
                            SegmentedLabelBlock(
                                title = if (isArabic) "سرعة التشغيل الافتراضية" else "Playback Speed",
                                accentColor = aura.accentGold
                            ) {
                                SegmentedPillTrack(
                                    options = speedOptions,
                                    selected = playerConfig.playbackSpeed,
                                    accentColor = aura.accentGold,
                                    labelFor = { spd -> "${spd}x" },
                                    onSelect = onSelectPlaybackSpeed
                                )
                            }

                            BentoDivider()

                            // Double-Tap Skip Duration
                            val skipOptions = remember { listOf(5, 10, 15, 30) }
                            SegmentedLabelBlock(
                                title = if (isArabic) "مدة التخطي بالنقر المزدوج" else "Double-Tap Skip Step",
                                accentColor = aura.accentGold
                            ) {
                                SegmentedPillTrack(
                                    options = skipOptions,
                                    selected = playerConfig.doubleTapSeekSeconds,
                                    accentColor = aura.accentGold,
                                    labelFor = { sec -> if (isArabic) "${sec} ث" else "${sec}s" },
                                    onSelect = onSelectDoubleTapSeekSeconds
                                )
                            }

                            BentoDivider()

                            // Controls Auto-Hide Timeout
                            val timeoutOptions = remember { listOf(3, 5, 10, 15) }
                            SegmentedLabelBlock(
                                title = if (isArabic) "إخفاء أزرار المشغل تلقائياً بعد" else "Auto-Hide Controls After",
                                accentColor = aura.accentGold
                            ) {
                                SegmentedPillTrack(
                                    options = timeoutOptions,
                                    selected = playerConfig.controlsAutoHideSeconds,
                                    accentColor = aura.accentGold,
                                    labelFor = { sec -> if (isArabic) "${sec} ث" else "${sec}s" },
                                    onSelect = onSelectControlsAutoHideSeconds
                                )
                            }

                            BentoDivider()

                            // Audio Volume Boost
                            val boostOptions = remember { listOf(1.0f, 1.25f, 1.5f, 2.0f) }
                            SegmentedLabelBlock(
                                title = if (isArabic) "مضخم الصوت (Audio Boost)" else "Studio Audio Boost",
                                accentColor = aura.accentGold
                            ) {
                                SegmentedPillTrack(
                                    options = boostOptions,
                                    selected = playerConfig.audioBoostMultiplier,
                                    accentColor = aura.accentGold,
                                    labelFor = { boost -> "${(boost * 100).toInt()}%" },
                                    onSelect = onSelectAudioBoost
                                )
                            }

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.Crop,
                                iconTint = aura.accentGold,
                                title = if (isArabic) "قص الحواف السوداء تلقائياً" else "Auto-Crop Black Bars",
                                subtitle = if (isArabic) "تكبير تلقائي لإزالة الأشرطة السينمائية السوداء" else "Automatically zoom past cinema black bars",
                                checked = playerConfig.isAutoCropBlackBars,
                                onCheckedChange = onToggleAutoCropBlackBars
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.Swipe,
                                iconTint = aura.accentGold,
                                title = if (isArabic) "إيماءات السحب (السطوع والصوت)" else "Touch Swipe Gestures",
                                subtitle = if (isArabic) "السحب يميناً للصوت ويساراً للسطوع وأفقياً للتقديم" else "Swipe vertically for volume/brightness & seek",
                                checked = playerConfig.enableSwipeGestures,
                                onCheckedChange = onToggleSwipeGestures
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.PlayArrow,
                                iconTint = aura.accentGold,
                                title = if (isArabic) "استئناف التشغيل تلقائياً" else "Auto-Resume Last Position",
                                subtitle = if (isArabic) "إكمال الفيديو تلقائياً من آخر ثانية توقفت عندها" else "Continue playback where you left off",
                                checked = autoResumePlayback,
                                onCheckedChange = onToggleAutoResumePlayback
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.GraphicEq,
                                iconTint = if (isPremiumActivated) aura.accentGold else aura.textTertiary,
                                title = if (isArabic) "التشغيل الصوتي في الخلفية" else "Background Audio Play",
                                subtitle = if (!isPremiumActivated) {
                                    if (isArabic) "ميزة Premium — افتح Premium لتفعيلها" else "Premium feature — unlock Premium to enable"
                                } else {
                                    if (isArabic) "استمرار الصوت عند إغلاق الشاشة أو الخروج" else "Keep audio playing when minimized"
                                },
                                checked = backgroundPlayEnabled && isPremiumActivated,
                                enabled = isPremiumActivated,
                                onCheckedChange = onToggleBackgroundPlay
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 4. SEEKBAR STUDIO SETTINGS (إعدادات الـ Seekbar)
            // =========================================================================
            if (showSeekbarSection) {
                item(key = "section_seekbar_studio") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionEyebrowHeader(
                            title = if (isArabic) "إعدادات شريط التمرير (Seekbar)" else "SEEKBAR STUDIO",
                            subtitle = if (isArabic) "النمط، السُمك، التوهج، ومعاينة السحب" else "Style switcher, thickness, glow & scrubbing",
                            accentColor = aura.accentFlame,
                            icon = Icons.Default.LinearScale
                        )

                        BentoSettingsPanel {
                            // Seekbar Style Expand/Collapse Accordion
                            val chevronRotation by animateFloatAsState(
                                targetValue = if (isSeekbarSkinExpanded) 180f else 0f,
                                animationSpec = tween(220),
                                label = "seekbar_skin_chevron_rotation"
                            )

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            isSeekbarSkinExpanded = !isSeekbarSkinExpanded
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                        .testTag("seekbar_skin_accordion_toggle"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(aura.accentFlameSoft)
                                                .border(
                                                    1.dp,
                                                    aura.accentFlame.copy(alpha = 0.35f),
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LinearScale,
                                                contentDescription = null,
                                                tint = aura.accentFlame,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (isArabic) "نمط شريط التمرير" else "Active Seekbar Skin",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = aura.textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text(
                                                text = if (isArabic) {
                                                    if (isSeekbarSkinExpanded) "اضغط للطي" else "اضغط للتمديد والاختيار"
                                                } else {
                                                    if (isSeekbarSkinExpanded) "Tap to collapse" else "Tap to expand & select"
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = aura.textSecondary,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = aura.accentFlameSoft,
                                            border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.35f))
                                        ) {
                                            Text(
                                                text = activeSeekbarStyle.displayName,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = aura.accentFlame,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isSeekbarSkinExpanded) "Collapse" else "Expand",
                                            tint = aura.textSecondary,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .rotate(chevronRotation)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isSeekbarSkinExpanded,
                                    enter = expandVertically(
                                        animationSpec = tween(250, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
                                    ) + fadeIn(tween(200)),
                                    exit = shrinkVertically(
                                        animationSpec = tween(200, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f))
                                    ) + fadeOut(tween(150))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        HorizontalDivider(
                                            thickness = 0.6.dp,
                                            color = aura.borderSubtle.copy(alpha = 0.6f)
                                        )

                                        PlayerSeekbarStyle.entries.chunked(2).forEach { rowPair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                rowPair.forEach { style ->
                                                    val isUnlocked = style in unlockedSeekbarStyles
                                                    val isSelected = activeSeekbarStyle == style
                                                    SeekbarPreviewBentoTile(
                                                        style = style,
                                                        isArabic = isArabic,
                                                        isSelected = isSelected,
                                                        isUnlocked = isUnlocked,
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            if (isUnlocked) {
                                                                onSelectSeekbarStyle(style)
                                                            } else {
                                                                seekbarLockNotice = if (isArabic) {
                                                                    "افتح نمط ${style.displayName} من المتجر أو Premium أولاً"
                                                                } else {
                                                                    "Unlock ${style.displayName} in Store or Premium first"
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (rowPair.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }

                                        AnimatedVisibility(visible = seekbarLockNotice != null) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = aura.accentFlameSoft,
                                                border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = seekbarLockNotice.orEmpty(),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = aura.accentFlame,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            BentoDivider()

                            // Seekbar Thickness
                            SegmentedLabelBlock(
                                title = if (isArabic) "سُمك وارتفاع الشريط" else "Seekbar Thickness",
                                accentColor = aura.accentFlame
                            ) {
                                SegmentedPillTrack(
                                    options = SeekbarThickness.entries,
                                    selected = playerConfig.seekbarThickness,
                                    accentColor = aura.accentFlame,
                                    labelFor = { if (isArabic) it.titleAr else it.titleEn },
                                    onSelect = onSelectSeekbarThickness
                                )
                            }

                            BentoDivider()

                            // Seekbar Glow Intensity
                            SegmentedLabelBlock(
                                title = if (isArabic) "شدة توهج وحركة الشريط" else "Glow & Halo Intensity",
                                accentColor = aura.accentFlame
                            ) {
                                SegmentedPillTrack(
                                    options = SeekbarGlowIntensity.entries,
                                    selected = playerConfig.seekbarGlowIntensity,
                                    accentColor = aura.accentFlame,
                                    labelFor = { if (isArabic) it.titleAr else it.titleEn },
                                    onSelect = onSelectSeekbarGlowIntensity
                                )
                            }

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.Bookmark,
                                iconTint = aura.accentFlame,
                                title = if (isArabic) "إظهار نقاط العلامات على الشريط" else "Bookmark Dots on Seekbar",
                                subtitle = if (isArabic) "عرض النقاط الذهبية للعلامات فوق مسار التشغيل" else "Render golden marker dots along the timeline",
                                checked = playerConfig.showBookmarkMarkersOnSeekbar,
                                onCheckedChange = onToggleShowBookmarkMarkersOnSeekbar
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.TouchApp,
                                iconTint = aura.accentFlame,
                                title = if (isArabic) "المعاينة الفورية أثناء السحب" else "Live Frame Drag Scrubbing",
                                subtitle = if (isArabic) "تحديث الفيديو لحظة بلحظة أثناء سحب الشريط" else "Seek video live while dragging (disable for heavy 4K)",
                                checked = playerConfig.liveSeekPreview,
                                onCheckedChange = onToggleLiveSeekPreview
                            )

                            BentoDivider()

                            EditorialSettingToggleRow(
                                icon = Icons.Default.SwapVert,
                                iconTint = aura.accentFlame,
                                title = if (isArabic) "نقل شريط التقدم أسفل الأزرار" else "Seekbar Below Bottom Controls",
                                subtitle = if (isArabic) "إظهار شريط التمرير أسفل أزرار السرعة والتحكم داخل المشغل" else "Place timeline seekbar beneath playback speed & tool buttons",
                                checked = playerConfig.seekbarBelowControls,
                                onCheckedChange = onToggleSeekbarBelowControls
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 5. LIBRARY & SCANNING ACTIONS
            // =========================================================================
            if (showLibrarySections) {
                item(key = "section_library_tools") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionEyebrowHeader(
                            title = if (isArabic) "المكتبة والفحص" else "LIBRARY TOOLS",
                            subtitle = if (isArabic) "إدارة واستيراد مقاطع الفيديو" else "Refresh local media or import files",
                            accentColor = aura.accentEmerald,
                            icon = Icons.Default.FilterList
                        )

                        BentoSettingsPanel {
                            EditorialActionRow(
                                icon = Icons.Default.Refresh,
                                iconTint = aura.accentEmerald,
                                title = if (isArabic) "إعادة فحص فيديوهات الهاتف" else "Rescan Device Videos",
                                subtitle = if (isArabic) "تحديث القائمة والمجلدات من الذاكرة" else "Refresh local library and folders",
                                onClick = onRescanVideos
                            )

                            BentoDivider()

                            EditorialActionRow(
                                icon = Icons.Default.FileOpen,
                                iconTint = aura.accentEmerald,
                                title = if (isArabic) "فتح ملفات الهاتف" else "Import Phone Files",
                                subtitle = if (isArabic) "اختيار ملفات فيديو من مستكشف الملفات" else "Browse device storage for videos",
                                onClick = onImportPhoneFiles
                            )

                            BentoDivider()

                            EditorialActionRow(
                                icon = Icons.Default.VideoLibrary,
                                iconTint = aura.accentEmerald,
                                title = if (isArabic) "معرض الفيديوهات" else "Open Photo & Video Picker",
                                subtitle = if (isArabic) "استيراد سريع بدون أذونات إضافية" else "Quick import via Android Photo Picker",
                                onClick = onOpenGalleryPicker
                            )
                        }
                    }
                }
            }

            item(key = "watch_settings_footer") {
                Text(
                    text = if (isArabic) {
                        "جميع إعدادات المشاهدة والمشغل وشريط التمرير تُحفظ تلقائياً ✓"
                    } else {
                        "All Watch, Player & Seekbar preferences auto-save instantly ✓"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 14.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = aura.textTertiary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BookmarksSpotlightHeroCard(
    isArabic: Boolean,
    videosCount: Int,
    totalMarksCount: Int,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val goldGlow = aura.accentGold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        aura.surfaceElevated,
                        aura.surfaceCard,
                        aura.accentGoldBg.copy(alpha = 0.65f)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            goldGlow.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.2f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.9f, size.height * 0.2f)
                )
            }
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            aura.accentGold.copy(alpha = 0.65f),
                            aura.borderSubtle
                        )
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag("watch_settings_bookmarks_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(aura.accentGold.copy(alpha = 0.16f))
                        .border(1.dp, aura.accentGold.copy(alpha = 0.5f), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = null,
                        tint = aura.accentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isArabic) "العلامة" else "Bookmarks Vault",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                        if (totalMarksCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = aura.accentGold
                            ) {
                                Text(
                                    text = "$totalMarksCount",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isArabic) {
                            if (totalMarksCount == 0) {
                                "كل الفيديوهات المعلّمة وأوقات اللحظات المحفوظة"
                            } else {
                                "$videosCount فيديو معلّم • $totalMarksCount وقت محفوظ للقفز السريع"
                            }
                        } else {
                            if (totalMarksCount == 0) {
                                "Access all bookmarked videos & instant timestamp jumps"
                            } else {
                                "$videosCount marked videos • $totalMarksCount saved timestamps"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(aura.accentGold.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = aura.accentGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionEyebrowHeader(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector
) {
    val aura = MaterialTheme.aura
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textTertiary,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor.copy(alpha = 0.75f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun BentoSettingsPanel(
    content: @Composable () -> Unit
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = aura.surfaceCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, aura.borderSubtle),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun BentoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.7.dp,
        color = MaterialTheme.aura.borderMuted
    )
}

@Composable
private fun SegmentedLabelBlock(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    val aura = MaterialTheme.aura
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
        content()
    }
}

@Composable
private fun <T> SegmentedPillTrack(
    options: List<T>,
    selected: T,
    accentColor: Color,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = aura.surfaceSubtle,
        border = BorderStroke(0.8.dp, aura.borderMuted),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                val bg by animateColorAsState(
                    targetValue = if (isSelected) accentColor else Color.Transparent,
                    animationSpec = tween(180),
                    label = "seg_pill_bg"
                )
                val fg by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (aura.isDark || accentColor == aura.accentViolet) Color.White else TextDark
                    } else {
                        aura.textSecondary
                    },
                    animationSpec = tween(180),
                    label = "seg_pill_fg"
                )
                val resolvedTextColor = if (isSelected && aura.isDark && accentColor != aura.accentViolet) {
                    TextDark
                } else {
                    fg
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(option)
                        }
                        .padding(vertical = 9.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelFor(option),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = resolvedTextColor,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorialSettingToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(!checked)
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (checked && enabled) iconTint.copy(alpha = 0.16f)
                        else aura.surfaceSubtle
                    )
                    .border(
                        0.8.dp,
                        if (checked && enabled) iconTint.copy(alpha = 0.35f) else aura.borderMuted,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked && enabled) iconTint else aura.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (enabled) aura.textPrimary else aura.textTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textSecondary,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = iconTint,
                checkedThumbColor = if (aura.isDark && iconTint != aura.accentViolet) TextDark else Color.White,
                uncheckedTrackColor = aura.progressTrack,
                uncheckedThumbColor = aura.textSecondary
            )
        )
    }
}

@Composable
private fun EditorialActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.14f))
                    .border(0.8.dp, iconTint.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textSecondary,
                        fontSize = 10.5.sp
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BlackBlurModeBentoOption(
    mode: AmbientBlackBlurMode,
    isArabic: Boolean,
    isSelected: Boolean,
    isUnlocked: Boolean = true,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val swatchColors = remember(mode) {
        when (mode) {
            AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> listOf(Color(0xFF000000), Color(0xFF222834), Color(0xFF000000))
            AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> listOf(Color(0xFF0D0E12), Color(0xFF242934), Color(0xFF050507))
            AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> listOf(Color(0xFF241F2B), Color(0xFF000000), Color(0xFF241F2B))
            AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> listOf(Color(0xFF2B2D30), Color(0xFF121315), Color(0xFF000000))
            AmbientBlackBlurMode.VELVET_SHADOW_MIST -> listOf(Color(0xFF251F2E), Color(0xFF0A090D), Color(0xFF000000))
            AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> listOf(Color(0xFF252E3E), Color(0xFF000000), Color(0xFF1D2430))
            AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> listOf(Color(0xFF272E3B), Color(0xFF101318), Color(0xFF000000))
            AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> listOf(Color(0xFF1A1E26), Color(0xFF282E3A), Color(0xFF090A0D))
            AmbientBlackBlurMode.VELVET_SHADOW_MELT -> listOf(Color(0xFF2A3140), Color(0xFF0E1015), Color(0xFF000000))
            AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> listOf(Color(0xFF2D231E), Color(0xFF120F0D), Color(0xFF000000))
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = when {
            isSelected -> aura.accentGoldBg.copy(alpha = 0.5f)
            !isUnlocked -> aura.surfaceSubtle.copy(alpha = 0.55f)
            else -> aura.surfaceSubtle
        },
        border = BorderStroke(
            if (isSelected) 1.2.dp else 0.8.dp,
            if (isSelected) aura.accentGold else aura.borderMuted
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag("watch_settings_ambient_mode_${mode.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature Cinema Black Blur Swatch
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.radialGradient(swatchColors))
                    .border(
                        1.dp,
                        if (isSelected) aura.accentGold else Color.White.copy(alpha = 0.18f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.shortBadge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) aura.accentGold else Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) mode.titleAr else mode.titleEn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = when {
                            isSelected -> aura.accentGold
                            !isUnlocked -> aura.textSecondary
                            else -> aura.textPrimary
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isArabic) mode.subtitleAr else mode.subtitleEn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(aura.accentGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else if (!isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = aura.accentGoldBg.copy(alpha = 0.7f),
                    border = BorderStroke(0.8.dp, aura.accentGold.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = aura.accentGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${mode.shieldsCost}🛡️+${mode.costAmount}🗝️",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AmbientGlowModesContent(
    isArabic: Boolean,
    playerConfig: PlayerAmbientConfig,
    unlockedModes: Set<AmbientBlackBlurMode>,
    isPremiumActivated: Boolean,
    onToggleAmbientGlow: (Boolean) -> Unit,
    onSelectBlackBlurMode: (AmbientBlackBlurMode) -> Unit
) {
    val aura = MaterialTheme.aura
    var lockNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lockNotice) {
        if (lockNotice != null) {
            kotlinx.coroutines.delay(2800L)
            lockNotice = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("watch_settings_ambient_glow_page"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "ambient_master_panel") {
            BentoSettingsPanel {
                EditorialSettingToggleRow(
                    icon = Icons.Default.BlurOn,
                    iconTint = aura.accentGold,
                    title = "Ambient Video Glow Fill",
                    subtitle = if (playerConfig.isAmbientGlowEnabled) {
                        if (isArabic) "توهج خلفية سينمائي ناعم يحافظ على عمق OLED" else "Pure cinema dark blur diffused around frame"
                    } else {
                        if (isArabic) "معطل • خلفية OLED سوداء نقية (#000000)" else "Off • Pure #000000 OLED black backdrop"
                    },
                    checked = playerConfig.isAmbientGlowEnabled,
                    onCheckedChange = onToggleAmbientGlow
                )
            }
        }

        if (lockNotice != null) {
            item(key = "ambient_lock_notice") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = aura.accentFlameSoft,
                    border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lockNotice.orEmpty(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = aura.accentFlame,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item(key = "ambient_modes_list_panel") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionEyebrowHeader(
                    title = if (isArabic) "أوضاع التمويه والتلاشي الفاخرة (10)" else "LUXURY BLUR & FADE MODES (10)",
                    subtitle = if (isArabic) "تخييم ضوئي بطيء نحو #000000 • محفوظة في Room DB" else "Damped OLED #000000 blur & fade • Saved via Room DB",
                    accentColor = aura.accentGold,
                    icon = Icons.Default.AutoAwesome
                )

                BentoSettingsPanel {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AmbientBlackBlurMode.entries.forEach { mode ->
                            val isUnlocked = mode.isUnlockedByDefault || (mode in unlockedModes) || isPremiumActivated
                            val isSelected = playerConfig.isAmbientGlowEnabled && playerConfig.blackBlurMode == mode
                            BlackBlurModeBentoOption(
                                mode = mode,
                                isArabic = isArabic,
                                isSelected = isSelected,
                                isUnlocked = isUnlocked,
                                onClick = {
                                    if (isUnlocked) {
                                        onSelectBlackBlurMode(mode)
                                    } else {
                                        val modeTitle = if (isArabic) mode.titleAr else mode.titleEn
                                        lockNotice = if (isArabic) {
                                            "🔒 افتح «$modeTitle» من المتجر ← Ambient Video Fill أو عبر Premium أولاً"
                                        } else {
                                            "🔒 Unlock \"$modeTitle\" in Shop → Ambient Video Fill or Premium first"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekbarPreviewBentoTile(
    style: PlayerSeekbarStyle,
    isArabic: Boolean,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val borderColor = when {
        isSelected -> aura.accentFlame
        !isUnlocked -> aura.borderMuted
        else -> aura.borderSubtle
    }
    val containerColor = when {
        isSelected -> aura.accentFlameSoft.copy(alpha = 0.75f)
        !isUnlocked -> aura.surfaceSubtle.copy(alpha = 0.55f)
        else -> aura.surfaceSubtle
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.3.dp else 0.8.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("seekbar_style_tile_${style.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isSelected -> aura.accentFlame
                        !isUnlocked -> aura.progressTrack
                        else -> aura.surfaceCard
                    }
                ) {
                    Text(
                        text = when {
                            isSelected -> if (isArabic) "مفعّل" else "ACTIVE"
                            !isUnlocked -> if (isArabic) "مقفل" else "LOCKED"
                            else -> if (isArabic) "متاح" else "READY"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color.White else aura.textSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 8.5.sp
                        )
                    )
                }

                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = aura.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = aura.accentFlame,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Miniature Canvas Preview representing each Seekbar's signature geometry
            SeekbarMiniCanvasPreview(
                style = style,
                isUnlocked = isUnlocked,
                accentColor = if (isSelected) aura.accentFlame else aura.accentCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            )

            Text(
                text = when (style) {
                    PlayerSeekbarStyle.CLASSIC -> if (isArabic) "الكلاسيكي" else "Classic"
                    PlayerSeekbarStyle.WAVEFORM -> if (isArabic) "الموجة الصوتية" else "Waveform"
                    PlayerSeekbarStyle.SHOOTING_STAR -> if (isArabic) "الشهاب الليزري" else "Shooting Star"
                    PlayerSeekbarStyle.ANGRY_SNAKE -> if (isArabic) "الأفعى الغاضبة" else "Angry Snake"
                    PlayerSeekbarStyle.PLASMA_KATANA -> if (isArabic) "سيف البلازما" else "Plasma Katana"
                    PlayerSeekbarStyle.QUANTUM_COLLIDER -> if (isArabic) "المصادم الكمي" else "Quantum Collider"
                    PlayerSeekbarStyle.DOUBLE_HELIX_GENE -> if (isArabic) "الحلزون الجيني" else "Double Helix"
                    PlayerSeekbarStyle.EVENT_HORIZON -> if (isArabic) "أفق الحدث" else "Event Horizon"
                    PlayerSeekbarStyle.CYBER_MATRIX_CODE -> if (isArabic) "شيفرة الماتريكس" else "Cyber Matrix"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isUnlocked) aura.textPrimary else aura.textSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SeekbarMiniCanvasPreview(
    style: PlayerSeekbarStyle,
    isUnlocked: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val trackMuted = MaterialTheme.aura.progressTrack
    Canvas(modifier = modifier) {
        val cy = size.height / 2f
        val pad = 4.dp.toPx()
        val w = (size.width - pad * 2f).coerceAtLeast(1f)
        val headX = pad + w * 0.68f
        val tint = if (isUnlocked) accentColor else Color(0xFF64748B)

        when (style) {
            PlayerSeekbarStyle.CLASSIC -> {
                drawLine(trackMuted, Offset(pad, cy), Offset(pad + w, cy), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(tint, Offset(pad, cy), Offset(headX, cy), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(tint.copy(alpha = 0.25f), radius = 8.dp.toPx(), center = Offset(headX, cy))
                drawCircle(Color.White, radius = 4.2.dp.toPx(), center = Offset(headX, cy))
            }
            PlayerSeekbarStyle.WAVEFORM -> {
                val count = 18
                val slot = w / count
                for (i in 0 until count) {
                    val cx = pad + (i + 0.5f) * slot
                    val h = (4.dp.toPx() + 14.dp.toPx() * kotlin.math.abs(sin(i * 0.72f)))
                    drawRoundRect(
                        color = if (cx <= headX) tint else trackMuted,
                        topLeft = Offset(cx - slot * 0.28f, cy - h / 2f),
                        size = Size(slot * 0.56f, h),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
            PlayerSeekbarStyle.SHOOTING_STAR -> {
                drawLine(trackMuted, Offset(pad, cy), Offset(pad + w, cy), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF4FD8))),
                    start = Offset(pad, cy),
                    end = Offset(headX, cy),
                    strokeWidth = 3.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(Color(0x55FF4FD8), radius = 8.dp.toPx(), center = Offset(headX, cy))
                drawLine(Color.White, Offset(headX - 6.dp.toPx(), cy), Offset(headX + 6.dp.toPx(), cy), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color.White, Offset(headX, cy - 6.dp.toPx()), Offset(headX, cy + 6.dp.toPx()), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            }
            PlayerSeekbarStyle.ANGRY_SNAKE -> {
                drawLine(trackMuted, Offset(pad, cy), Offset(pad + w, cy), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                val path = Path()
                val steps = 16
                for (i in 0..steps) {
                    val t = i / steps.toFloat()
                    val sx = pad + (headX - pad) * t
                    val sy = cy + sin(t * 3.2f * PI.toFloat()) * 4.5.dp.toPx() * t
                    if (i == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
                }
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFFFF1744))),
                    style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(Color(0xFFFF1744), radius = 4.6.dp.toPx(), center = Offset(headX, cy))
                drawCircle(Color(0xFFFACC15), radius = 1.6.dp.toPx(), center = Offset(headX + 1.dp.toPx(), cy - 1.dp.toPx()))
            }
            PlayerSeekbarStyle.PLASMA_KATANA -> {
                drawLine(Color(0xFF060A14), Offset(pad, cy), Offset(pad + w, cy), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0x6600F2FE), Offset(pad, cy), Offset(headX, cy), strokeWidth = 6.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF00F2FE), Color.White)),
                    start = Offset(pad, cy),
                    end = Offset(headX, cy),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(Color(0xFF00F2FE), radius = 3.6.dp.toPx(), center = Offset(headX, cy))
            }
            PlayerSeekbarStyle.QUANTUM_COLLIDER -> {
                drawRoundRect(
                    color = Color(0x33FFFFFF),
                    topLeft = Offset(pad, cy - 4.5.dp.toPx()),
                    size = Size(w, 9.dp.toPx()),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawLine(Color(0xFF00F2FE), Offset(pad + 2.dp.toPx(), cy), Offset(headX, cy), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0xFFA855F7), Offset(headX, cy), Offset(pad + w - 2.dp.toPx(), cy), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color(0x6600F2FE), radius = 7.dp.toPx(), center = Offset(headX, cy), style = Stroke(width = 1.2.dp.toPx()))
                drawCircle(Color.White, radius = 2.8.dp.toPx(), center = Offset(headX, cy))
            }
            PlayerSeekbarStyle.DOUBLE_HELIX_GENE -> {
                val pA = Path()
                val pB = Path()
                val steps = 18
                for (i in 0..steps) {
                    val t = i / steps.toFloat()
                    val sx = pad + w * t
                    val ang = t * 4f * PI.toFloat()
                    val yA = cy + sin(ang) * 5.dp.toPx()
                    val yB = cy - sin(ang) * 5.dp.toPx()
                    if (i == 0) {
                        pA.moveTo(sx, yA)
                        pB.moveTo(sx, yB)
                    } else {
                        pA.lineTo(sx, yA)
                        pB.lineTo(sx, yB)
                    }
                }
                drawPath(pA, color = Color(0xFF00F2FE), style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))
                drawPath(pB, color = Color(0xFFFF2E93), style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(Color.White, radius = 3.2.dp.toPx(), center = Offset(headX, cy))
            }
            PlayerSeekbarStyle.EVENT_HORIZON -> {
                drawLine(trackMuted, Offset(pad, cy), Offset(pad + w, cy), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF00F2FE))),
                    start = Offset(pad, cy),
                    end = Offset(headX, cy),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(Color(0x55A855F7), radius = 8.5.dp.toPx(), center = Offset(headX, cy))
                drawCircle(Color.Black, radius = 4.2.dp.toPx(), center = Offset(headX, cy))
                drawCircle(Color(0xFF00F2FE), radius = 4.5.dp.toPx(), center = Offset(headX, cy), style = Stroke(width = 1.4.dp.toPx()))
            }
            PlayerSeekbarStyle.CYBER_MATRIX_CODE -> {
                val count = 14
                val slot = w / count
                for (i in 0 until count) {
                    val cx = pad + (i + 0.5f) * slot
                    val c = if (cx <= headX) (if (i % 3 == 0) Color(0xFF00F2FE) else Color(0xFF10B981)) else Color(0x3310B981)
                    if (i % 2 == 0) {
                        drawLine(c, Offset(cx, cy - 4.dp.toPx()), Offset(cx, cy + 4.dp.toPx()), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
                    } else {
                        drawCircle(c, radius = 2.2.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 1.2.dp.toPx()))
                    }
                }
                drawRoundRect(
                    color = Color(0xFF00F2FE),
                    topLeft = Offset(headX - 2.dp.toPx(), cy - 6.dp.toPx()),
                    size = Size(4.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }
    }
}

@Composable
private fun BookmarkedVideosListContent(
    isArabic: Boolean,
    bookmarkGroups: List<VideoBookmarkGroup>,
    localVideos: List<LocalDeviceVideo>,
    onSelectVideo: (VideoBookmarkGroup) -> Unit,
    onDeleteVideoBookmarks: (VideoBookmarkGroup) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    if (bookmarkGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(aura.accentGoldBg)
                            .border(1.dp, aura.accentGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = null,
                            tint = aura.accentGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isArabic) "لا توجد فيديوهات معلّمة بعد" else "No Bookmarked Videos Yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) {
                            "أثناء مشاهدة أي فيديو، اضغط على زر (علامة ⭐) لحفظ اللحظات المهمة وستظهر هنا للعودة إليها بضغطة واحدة."
                        } else {
                            "While watching any video, tap the Bookmark button to save moments. Marked videos will appear here for one-tap jumping."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookmarked_videos_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bookmarkGroups, key = { it.videoKey }) { group ->
            val matchedVideo = remember(localVideos, group.videoId, group.uriString) {
                localVideos.find {
                    it.id == group.videoId || (group.uriString != null && it.uriString == group.uriString)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectVideo(group)
                    }
                    .testTag("bookmarked_video_card_${group.videoId}"),
                shape = RoundedCornerShape(20.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(62.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        when {
                            matchedVideo?.bitmap != null -> {
                                Image(
                                    bitmap = matchedVideo.bitmap.asImageBitmap(),
                                    contentDescription = group.videoTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.High
                                )
                            }
                            group.thumbnailRes != 0 -> {
                                Image(
                                    painter = painterResource(id = group.thumbnailRes),
                                    contentDescription = group.videoTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                SmartFallbackCover(
                                    title = group.videoTitle,
                                    aspectRatio = 1.55f,
                                    badgeText = null,
                                    gradientVariant = group.videoKey.hashCode(),
                                    showText = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.videoTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = aura.accentGoldBg,
                                border = BorderStroke(0.8.dp, aura.accentGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (isArabic) {
                                        "★ ${group.bookmarks.size} علامات"
                                    } else {
                                        "★ ${group.bookmarks.size} marks"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.accentGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            if (group.durationMs > 0L) {
                                Text(
                                    text = formatBookmarkTimestamp(group.durationMs),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = aura.textSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDeleteVideoBookmarks(group)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_video_bookmarks_${group.videoId}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(aura.accentFlameSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isArabic) "حذف علامات الفيديو" else "Delete video bookmarks",
                                tint = aura.accentFlame,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoBookmarksTimestampsContent(
    isArabic: Boolean,
    group: VideoBookmarkGroup?,
    localVideos: List<LocalDeviceVideo>,
    onPlayAtTimestamp: (Long) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    if (group == null) return

    val matchedVideo = remember(localVideos, group.videoId, group.uriString) {
        localVideos.find {
            it.id == group.videoId || (group.uriString != null && it.uriString == group.uriString)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("video_bookmarks_timestamps_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Video Summary Banner
        item(key = "video_bookmark_header") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.accentGold.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        when {
                            matchedVideo?.bitmap != null -> {
                                Image(
                                    bitmap = matchedVideo.bitmap.asImageBitmap(),
                                    contentDescription = group.videoTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.High
                                )
                            }
                            group.thumbnailRes != 0 -> {
                                Image(
                                    painter = painterResource(id = group.thumbnailRes),
                                    contentDescription = group.videoTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                SmartFallbackCover(
                                    title = group.videoTitle,
                                    aspectRatio = 1.57f,
                                    badgeText = null,
                                    gradientVariant = group.videoKey.hashCode(),
                                    showText = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.videoTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) {
                                "اضغط على أي توقيت للانتقال مباشرة إلى تلك الثانية"
                            } else {
                                "Tap any saved time to jump straight into playback"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        items(group.bookmarks, key = { it.id }) { bookmark: VideoBookmarkEntity ->
            val progress = if (group.durationMs > 0L) {
                (bookmark.timestampMs.toFloat() / group.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayAtTimestamp(bookmark.timestampMs)
                    }
                    .testTag("bookmark_timestamp_item_${bookmark.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(aura.accentCyanSoft)
                            .border(1.dp, aura.accentCyan.copy(alpha = 0.5f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Jump to timestamp",
                            tint = aura.accentCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = aura.accentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = formatBookmarkTimestamp(bookmark.timestampMs),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = aura.accentGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        if (group.durationMs > 0L) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(aura.progressTrack)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceAtLeast(0.03f))
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(listOf(aura.accentCyan, aura.accentGold))
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDeleteBookmark(bookmark.id)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_single_bookmark_${bookmark.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(aura.accentFlameSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isArabic) "حذف العلامة" else "Delete bookmark",
                                tint = aura.accentFlame,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatArabicDaysLabel(days: Int): String = when {
    days == 1 -> "يوم واحد"
    days == 2 -> "يومان"
    days in 3..10 -> "$days أيام"
    else -> "$days يوماً"
}

private fun formatBookmarkTimestamp(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
