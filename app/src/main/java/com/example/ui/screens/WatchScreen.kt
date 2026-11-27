package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.example.ui.theme.LocalStableStatusBarTop
import com.example.ui.theme.rememberStableStatusBarTopDp
import com.example.ui.theme.stableStatusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.local.WatchSortBy
import com.example.data.local.WatchSortDirection
import com.example.data.model.PlayerSeekbarStyle
import com.example.data.preferences.StreakPreferencesRepository
import com.example.data.preferences.VideoProgressInfo
import com.example.data.util.EpisodeParser
import com.example.data.util.OnlineFolderCoverFetcher
import java.io.File
import com.example.ui.components.ContinueWatchingSection
import com.example.ui.components.DeleteFolderConfirmationDialog
import com.example.ui.components.DeleteVideoConfirmationDialog
import com.example.ui.components.FolderInfoDialog
import com.example.ui.components.LuxuryEpisodeThumbnailBadge
import com.example.ui.components.MoveVideoFolderDialog
import com.example.ui.components.PinnedBadgePill
import com.example.ui.components.RenameFolderDialog
import com.example.ui.components.RenameVideoDialog
import com.example.ui.components.SmoothMarqueeTitleText
import com.example.ui.components.VideoInfoDialog
import com.example.ui.components.WatchInlineActionItem
import com.example.ui.components.WatchInlineSelectionActionBar
import com.example.ui.components.WatchSettingsOverlay
import com.example.ui.components.formatCleanTitle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MediaItem
import com.example.data.model.isNewWithinDays
import com.example.data.util.LocalMediaScanner
import com.example.ui.components.SmartFallbackCover
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.TextDark
import com.example.ui.theme.aura
import com.example.ui.viewmodel.PlayerAmbientViewModel
import com.example.ui.viewmodel.StreakViewModel
import com.example.ui.viewmodel.WatchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

enum class WatchCategory(val titleEn: String, val titleAr: String, val icon: ImageVector) {
    FOLDERS("Folder", "مجلدات", Icons.Default.Folder),
    FILES("Files", "ملفات", Icons.Default.VideoLibrary)
}

enum class WatchFileDisplayMode(val titleEn: String, val titleAr: String, val icon: ImageVector) {
    LIST("List", "قائمة", Icons.Default.ViewAgenda),
    GRID("Grid", "شبكة", Icons.Default.GridView),
    MAGAZINE("Magazine", "مجلة", Icons.Default.AutoStories),
    GALLERY_VIEW("Gallery View", "المعرض", Icons.Default.PhotoLibrary)
}

data class VideoFolder(
    val id: String,
    val name: String,
    val count: Int,
    val totalSize: String,
    val videos: List<LocalDeviceVideo>
)

private data class WatchContentKey(
    val category: WatchCategory,
    val fileDisplayMode: WatchFileDisplayMode,
    val folderId: String?
)

private data class GalleryMosaicRow(
    val id: String,
    val videos: List<LocalDeviceVideo>,
    val weights: List<Float>,
    val height: Dp
)

private sealed class MagazineFeedRow(val rowId: String) {
    data class HeroSpread(
        val id: String,
        val video: LocalDeviceVideo,
        val issueNumber: Int
    ) : MagazineFeedRow(id)

    data class DualPortraitSpread(
        val id: String,
        val left: LocalDeviceVideo,
        val leftIssue: Int,
        val right: LocalDeviceVideo?,
        val rightIssue: Int?
    ) : MagazineFeedRow(id)

    data class SpotlightStrip(
        val id: String,
        val video: LocalDeviceVideo,
        val issueNumber: Int
    ) : MagazineFeedRow(id)
}

private interface WatchScrollTracker {
    val firstVisibleItemIndex: Int
    val firstVisibleItemScrollOffset: Int
    val isScrollInProgress: Boolean
    val canScroll: Boolean
}

private class ListScrollTracker(private val state: LazyListState) : WatchScrollTracker {
    override val firstVisibleItemIndex: Int get() = state.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int get() = state.firstVisibleItemScrollOffset
    override val isScrollInProgress: Boolean get() = state.isScrollInProgress
    override val canScroll: Boolean get() = state.canScrollForward || state.canScrollBackward
}

private class GridScrollTracker(private val state: LazyGridState) : WatchScrollTracker {
    override val firstVisibleItemIndex: Int get() = state.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int get() = state.firstVisibleItemScrollOffset
    override val isScrollInProgress: Boolean get() = state.isScrollInProgress
    override val canScroll: Boolean get() = state.canScrollForward || state.canScrollBackward
}

@Composable
fun WatchScreen(
    onPlayMedia: (MediaItem) -> Unit,
    isArabic: Boolean = false,
    streakViewModel: StreakViewModel? = null,
    bottomBarPadding: Dp = 0.dp,
    onBottomBarHiddenChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val aura = MaterialTheme.aura
    val prefsRepo = remember { StreakPreferencesRepository.getInstance(context) }
    val watchViewModel: WatchViewModel = viewModel()
    val playerAmbientViewModel: PlayerAmbientViewModel = viewModel()
    val watchSettings by watchViewModel.watchSettings.collectAsStateWithLifecycle()
    val playerConfig by playerAmbientViewModel.config.collectAsStateWithLifecycle()
    val bookmarkGroups by watchViewModel.bookmarkGroups.collectAsStateWithLifecycle()
    val cachedFolderCovers by watchViewModel.cachedFolderCovers.collectAsStateWithLifecycle()

    var showWatchSettings by remember { mutableStateOf(false) }
    val isPremiumActivatedState by (streakViewModel?.isPremiumActivated?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(prefsRepo.isPremiumActivated()) })
    val purchasedSeekbarIdsState by (streakViewModel?.purchasedSeekbarIds?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(prefsRepo.getPurchasedSeekbarIds()) })
    var localSeekbarStyle by remember { mutableStateOf(prefsRepo.getActiveSeekbarStyle()) }
    val streakSeekbarStyle by (streakViewModel?.activeSeekbarStyle?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(localSeekbarStyle) })
    val activeSeekbarStyle = if (streakViewModel != null) streakSeekbarStyle else localSeekbarStyle
    val unlockedSeekbarStyles = remember(
        isPremiumActivatedState,
        activeSeekbarStyle,
        purchasedSeekbarIdsState,
        showWatchSettings
    ) {
        PlayerSeekbarStyle.entries.filter {
            it == PlayerSeekbarStyle.CLASSIC ||
                purchasedSeekbarIdsState.contains(it.id) ||
                prefsRepo.isSeekbarUnlocked(it) ||
                isPremiumActivatedState
        }.toSet()
    }
    val unlockedAmbientModes = remember(isPremiumActivatedState, playerConfig.blackBlurMode, showWatchSettings) {
        com.example.data.local.AmbientBlackBlurMode.entries
            .filter { prefsRepo.isAmbientModeUnlocked(it) || isPremiumActivatedState }
            .toSet()
    }
    var autoResumePlayback by remember { mutableStateOf(prefsRepo.getAutoResumePlayback()) }
    var backgroundPlayEnabled by remember { mutableStateOf(prefsRepo.isBackgroundPlayEnabled()) }

    val initialModePair = remember {
        val raw = prefsRepo.getWatchViewMode()
        if (raw.contains(":")) {
            val parts = raw.split(":")
            val cat = runCatching { WatchCategory.valueOf(parts[0]) }.getOrDefault(WatchCategory.FILES)
            val disp = runCatching { WatchFileDisplayMode.valueOf(parts[1]) }.getOrDefault(WatchFileDisplayMode.LIST)
            cat to disp
        } else {
            when (raw) {
                "FOLDERS" -> WatchCategory.FOLDERS to WatchFileDisplayMode.LIST
                "MAGAZINE" -> WatchCategory.FILES to WatchFileDisplayMode.MAGAZINE
                else -> WatchCategory.FILES to WatchFileDisplayMode.LIST
            }
        }
    }
    var selectedCategory by remember { mutableStateOf(initialModePair.first) }
    var selectedFileDisplayMode by remember { mutableStateOf(initialModePair.second) }

    var searchQuery by remember { mutableStateOf("") }
    var activeFolderId by remember { mutableStateOf<String?>(null) }
    var isContinueWatchingDismissed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var hideFileExtensions by remember { mutableStateOf(prefsRepo.getHideFileExtensions()) }

    LaunchedEffect(showWatchSettings) {
        if (showWatchSettings) {
            autoResumePlayback = prefsRepo.getAutoResumePlayback()
            backgroundPlayEnabled = prefsRepo.isBackgroundPlayEnabled()
            localSeekbarStyle = prefsRepo.getActiveSeekbarStyle()
        }
    }

    LaunchedEffect(watchSettings.showSearchBar) {
        if (!watchSettings.showSearchBar && searchQuery.isNotEmpty()) {
            searchQuery = ""
        }
    }

    var contextMenuVideo by remember { mutableStateOf<LocalDeviceVideo?>(null) }
    var contextMenuFolder by remember { mutableStateOf<VideoFolder?>(null) }
    val hasInlineSelection = contextMenuVideo != null || contextMenuFolder != null

    LaunchedEffect(selectedCategory, activeFolderId, showWatchSettings) {
        contextMenuVideo = null
        contextMenuFolder = null
    }

    // Guarantee system Back steps back 1 level inside Watch (Selection -> Search -> Folder -> previous tab)
    BackHandler(
        enabled = !showWatchSettings && (hasInlineSelection || searchQuery.isNotEmpty() || activeFolderId != null)
    ) {
        when {
            hasInlineSelection -> {
                contextMenuVideo = null
                contextMenuFolder = null
            }
            searchQuery.isNotEmpty() -> searchQuery = ""
            activeFolderId != null -> activeFolderId = null
        }
    }

    var hasStoragePermission by remember {
        mutableStateOf(LocalMediaScanner.hasStorageOrVideoPermission(context))
    }

    // ONLY real local videos from the device storage (starts empty with NO mock/dummy videos)
    val localVideos = remember { mutableStateListOf<LocalDeviceVideo>() }
    // Decoded thumbnails stored separately by video.id so decoding never mutates localVideos or triggers list diffing
    val thumbnailBitmaps = remember { mutableStateMapOf<String, ImageBitmap>() }
    var activeThumbJob by remember { mutableStateOf<Job?>(null) }

    val launchParallelThumbExtraction: (List<LocalDeviceVideo>) -> Unit = { targets ->
        val needThumbs = targets.filter {
            it.bitmap == null &&
                thumbnailBitmaps[it.id] == null &&
                !it.uriString.isNullOrBlank()
        }
        if (needThumbs.isNotEmpty()) {
            activeThumbJob?.cancel()
            activeThumbJob = scope.launch {
                LocalMediaScanner.extractThumbnailsParallel(
                    context = context,
                    videos = needThumbs,
                    concurrency = 6,
                    batchSize = 2
                ) { batchMap ->
                    if (batchMap.isEmpty()) return@extractThumbnailsParallel
                    val converted = withContext(Dispatchers.Default) {
                        batchMap.mapValues { (_, bmp) -> bmp.asImageBitmap() }
                    }
                    thumbnailBitmaps.putAll(converted)
                }
            }
        }
    }

    // Helper to refresh / scan device videos instantaneously
    val scanVideosJob: () -> Unit = {
        scope.launch {
            isLoading = true
            activeThumbJob?.cancel()
            val scanned = withContext(Dispatchers.IO) {
                LocalMediaScanner.scanDeviceVideos(context)
            }
            // Preserve any user-imported files from the system picker
            val userImported = localVideos.filter { it.id.startsWith("picked_") }
            val existingUris = userImported.mapNotNull { it.uriString }.toSet()
            val freshList = userImported + scanned.filter { it.uriString !in existingUris }
            val cachedBitmaps = withContext(Dispatchers.Default) {
                buildMap<String, ImageBitmap> {
                    for (vid in freshList) {
                        val cached = vid.bitmap ?: LocalMediaScanner.getMemoryCachedThumbnail(vid.uriString)
                        if (cached != null) {
                            put(vid.id, cached.asImageBitmap())
                        }
                    }
                }
            }

            // Instant display of all videos (plus already-cached thumbnails) in zero milliseconds
            thumbnailBitmaps.putAll(cachedBitmaps)
            localVideos.clear()
            localVideos.addAll(freshList)
            isLoading = false

            // Extract remaining thumbnails concurrently with RAM + disk JPEG caching
            launchParallelThumbExtraction(freshList)
        }
    }

    // Permission launcher for accessing local device videos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val granted = LocalMediaScanner.hasStorageOrVideoPermission(context)
        hasStoragePermission = granted
        if (granted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scanVideosJob()
        }
    }

    // Android System File Picker command launcher (Opens Phone Files / SAF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                val newVideos = withContext(Dispatchers.IO) {
                    uris.map { uri -> LocalMediaScanner.parseUriToLocalVideo(context, uri) }
                }
                var anyAdded = false
                for (vid in newVideos) {
                    if (localVideos.none { it.uriString == vid.uriString }) {
                        val cached = vid.bitmap ?: LocalMediaScanner.getMemoryCachedThumbnail(vid.uriString)
                        if (cached != null) {
                            thumbnailBitmaps[vid.id] = cached.asImageBitmap()
                        }
                        localVideos.add(0, vid)
                        anyAdded = true
                    }
                }
                if (anyAdded) {
                    launchParallelThumbExtraction(localVideos.toList())
                }
            }
        }
    }

    // Android Visual Media Picker launcher (Photo & Video Picker)
    val quickMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                val newVideos = withContext(Dispatchers.IO) {
                    uris.map { uri -> LocalMediaScanner.parseUriToLocalVideo(context, uri) }
                }
                var anyAdded = false
                for (vid in newVideos) {
                    if (localVideos.none { it.uriString == vid.uriString }) {
                        val cached = vid.bitmap ?: LocalMediaScanner.getMemoryCachedThumbnail(vid.uriString)
                        if (cached != null) {
                            thumbnailBitmaps[vid.id] = cached.asImageBitmap()
                        }
                        localVideos.add(0, vid)
                        anyAdded = true
                    }
                }
                if (anyAdded) {
                    launchParallelThumbExtraction(localVideos.toList())
                }
            }
        }
    }

    // Scan videos automatically when storage permission is available
    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) {
            scanVideosJob()
        }
    }

    // Preserve scroll position when returning from a folder to Folders list
    val foldersListState = rememberLazyListState()

    var pinnedVideoKeys by remember { mutableStateOf(prefsRepo.getPinnedVideoKeys()) }
    var pinnedFolderNames by remember { mutableStateOf(prefsRepo.getPinnedFolderNames()) }

    val isVideoPinned: (LocalDeviceVideo) -> Boolean = { video ->
        pinnedVideoKeys.contains(prefsRepo.canonicalVideoKey(video.id, video.uriString))
    }
    val isFolderPinned: (VideoFolder) -> Boolean = { folder ->
        pinnedFolderNames.contains(prefsRepo.canonicalFolderPinKey(folder.name))
    }

    // Dynamic folders grouped strictly from actual local videos found on the phone, with pinned folders on top
    val folders by remember {
        derivedStateOf {
            val grouped = localVideos.groupBy { it.folderName.ifBlank { "Internal Storage" } }.map { (folderName, videos) ->
                val safeFolderName = folderName.ifBlank { "Internal Storage" }
                VideoFolder(
                    id = "folder_${safeFolderName.hashCode()}_${videos.size}",
                    name = safeFolderName,
                    count = videos.size,
                    totalSize = calculateTotalFolderSize(videos),
                    videos = videos
                )
            }.distinctBy { it.id }

            val pinOrder = pinnedFolderNames
            val pinned = grouped
                .filter { pinOrder.contains(prefsRepo.canonicalFolderPinKey(it.name)) }
                .sortedBy { pinOrder.indexOf(prefsRepo.canonicalFolderPinKey(it.name)) }
            val unpinned = grouped
                .filterNot { pinOrder.contains(prefsRepo.canonicalFolderPinKey(it.name)) }
                .sortedByDescending { it.count }
            pinned + unpinned
        }
    }

    LaunchedEffect(folders, watchSettings.autoFetchOnlineFolderCovers) {
        if (watchSettings.autoFetchOnlineFolderCovers && folders.isNotEmpty()) {
            watchViewModel.syncFolderCovers(
                folderNames = folders.map { it.name },
                isMusicFolder = false
            )
        }
    }

    val filteredVideos by remember {
        derivedStateOf {
            val baseList = if (activeFolderId != null) {
                folders.find { it.id == activeFolderId }?.videos ?: localVideos
            } else {
                localVideos
            }
            val searched = if (searchQuery.isBlank()) baseList
            else baseList.filter { it.title.contains(searchQuery, ignoreCase = true) }

            val lengthFiltered = if (watchSettings.hideShortClips) {
                searched.filter { video ->
                    val secs = parseDurationSeconds(video.durationText)
                    secs == 0L || secs >= 5L
                }
            } else {
                searched
            }

            val deduped = lengthFiltered.distinctBy { it.id }
            val ascending = watchSettings.sortDirection == WatchSortDirection.ASCENDING
            val sorted = when (watchSettings.sortBy) {
                WatchSortBy.DEFAULT -> deduped
                WatchSortBy.NAME -> if (ascending) {
                    deduped.sortedBy { it.title.lowercase() }
                } else {
                    deduped.sortedByDescending { it.title.lowercase() }
                }
                WatchSortBy.DURATION -> if (ascending) {
                    deduped.sortedBy { parseDurationSeconds(it.durationText) }
                } else {
                    deduped.sortedByDescending { parseDurationSeconds(it.durationText) }
                }
                WatchSortBy.SIZE -> if (ascending) {
                    deduped.sortedBy { parseSizeBytes(it.sizeText) }
                } else {
                    deduped.sortedByDescending { parseSizeBytes(it.sizeText) }
                }
            }

            val pinOrder = pinnedVideoKeys
            if (pinOrder.isEmpty()) {
                sorted
            } else {
                val pinned = sorted
                    .filter { pinOrder.contains(prefsRepo.canonicalVideoKey(it.id, it.uriString)) }
                    .sortedBy { pinOrder.indexOf(prefsRepo.canonicalVideoKey(it.id, it.uriString)) }
                val unpinned = sorted
                    .filterNot { pinOrder.contains(prefsRepo.canonicalVideoKey(it.id, it.uriString)) }
                pinned + unpinned
            }
        }
    }

    var openedVideoKeys by remember { mutableStateOf(prefsRepo.getOpenedVideoKeys()) }
    val videoProgressByKey = remember { mutableStateMapOf<String, VideoProgressInfo>() }
    var progressRefreshTick by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                progressRefreshTick += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(localVideos.size, progressRefreshTick) {
        val indexed = withContext(Dispatchers.IO) {
            val history = prefsRepo.getWatchHistory()
            buildMap<String, VideoProgressInfo>(history.size * 2) {
                for (item in history) {
                    put(prefsRepo.canonicalVideoKey(item.id, item.uriString), item)
                    put("raw_id:${item.id}", item)
                }
            }
        }
        videoProgressByKey.clear()
        videoProgressByKey.putAll(indexed)
    }

    val openVideoForPlayback: (MediaItem) -> Unit = { media ->
        openedVideoKeys = prefsRepo.markVideoOpened(media.id, media.uriString)
        onPlayMedia(media)
    }

    val nowMs = remember(localVideos.size, watchSettings.showNewVideoBadge, watchSettings.newVideoBadgeDays) {
        System.currentTimeMillis()
    }
    val newBadgeDays = watchSettings.newVideoBadgeDays.coerceIn(1, 365)
    val isVideoNew: (LocalDeviceVideo) -> Boolean = { video ->
        watchSettings.showNewVideoBadge &&
            !openedVideoKeys.contains(prefsRepo.canonicalVideoKey(video.id, video.uriString)) &&
            video.isNewWithinDays(newBadgeDays, nowMs)
    }

    var videoToRename by remember { mutableStateOf<LocalDeviceVideo?>(null) }
    var videoToMove by remember { mutableStateOf<LocalDeviceVideo?>(null) }
    var videoToDelete by remember { mutableStateOf<LocalDeviceVideo?>(null) }
    var videoDetailsPair by remember { mutableStateOf<Pair<LocalDeviceVideo, LocalMediaScanner.VideoDetailsInfo>?>(null) }
    var folderToRename by remember { mutableStateOf<VideoFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<VideoFolder?>(null) }
    var folderDetailsPair by remember { mutableStateOf<Pair<VideoFolder, LocalMediaScanner.FolderDetailsInfo>?>(null) }
    var pendingMediaOp by remember { mutableStateOf<LocalMediaScanner.PendingMediaOp?>(null) }
    var pendingOpAlreadyHandled by remember { mutableStateOf(false) }
    var mediaOpFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mediaOpFeedback) {
        if (mediaOpFeedback != null) {
            delay(2600L)
            mediaOpFeedback = null
        }
    }

    val applyMediaOpSuccess: (LocalMediaScanner.PendingMediaOp, LocalMediaScanner.MediaFileOpResult.Success) -> Unit = { op, res ->
        when (op) {
            is LocalMediaScanner.PendingMediaOp.Rename -> {
                val idx = localVideos.indexOfFirst { it.id == op.video.id }
                val newUri = res.newUriString ?: op.video.uriString
                if (idx >= 0) {
                    val current = localVideos[idx]
                    localVideos[idx] = current.copy(
                        title = res.updatedTitle ?: op.newDisplayName,
                        uriString = res.newUriString ?: current.uriString
                    )
                }
                pinnedVideoKeys = prefsRepo.migratePinnedVideoKey(
                    op.video.id,
                    op.video.uriString,
                    op.video.id,
                    newUri
                )
                mediaOpFeedback = if (isArabic) "تم تعديل اسم الفيديو" else "Video renamed"
            }
            is LocalMediaScanner.PendingMediaOp.Move -> {
                val idx = localVideos.indexOfFirst { it.id == op.video.id }
                if (idx >= 0) {
                    val current = localVideos[idx]
                    localVideos[idx] = current.copy(
                        folderName = res.updatedFolder ?: op.targetFolderName,
                        relativePath = res.updatedRelativePath ?: op.targetRelativePath ?: current.relativePath
                    )
                }
                mediaOpFeedback = if (isArabic) "تم نقل الفيديو إلى ${op.targetFolderName}" else "Moved to ${op.targetFolderName}"
            }
            is LocalMediaScanner.PendingMediaOp.Delete -> {
                localVideos.removeAll { it.id == op.video.id }
                thumbnailBitmaps.remove(op.video.id)
                videoProgressByKey.remove(prefsRepo.canonicalVideoKey(op.video.id, op.video.uriString))
                videoProgressByKey.remove("raw_id:${op.video.id}")
                prefsRepo.removeWatchHistoryItem(op.video.id, op.video.uriString)
                pinnedVideoKeys = prefsRepo.removePinnedVideo(op.video.id, op.video.uriString)
                mediaOpFeedback = if (isArabic) "تم حذف الفيديو" else "Video deleted"
            }
            is LocalMediaScanner.PendingMediaOp.RenameFolder -> {
                val newFolder = res.updatedFolder ?: op.newFolderName
                for (i in localVideos.indices) {
                    val current = localVideos[i]
                    if (current.folderName.equals(op.oldFolderName, ignoreCase = true)) {
                        val updatedRel = res.updatedRelativePath ?: current.relativePath
                        localVideos[i] = current.copy(
                            folderName = newFolder,
                            relativePath = updatedRel
                        )
                    }
                }
                pinnedFolderNames = prefsRepo.renamePinnedFolder(op.oldFolderName, newFolder)
                mediaOpFeedback = if (isArabic) "تم تعديل اسم المجلد إلى $newFolder" else "Folder renamed to $newFolder"
            }
            is LocalMediaScanner.PendingMediaOp.DeleteFolder -> {
                val targetIds = op.videos.map { it.id }.toSet()
                localVideos.removeAll { it.id in targetIds }
                op.videos.forEach { vid ->
                    thumbnailBitmaps.remove(vid.id)
                    videoProgressByKey.remove(prefsRepo.canonicalVideoKey(vid.id, vid.uriString))
                    videoProgressByKey.remove("raw_id:${vid.id}")
                    prefsRepo.removeWatchHistoryItem(vid.id, vid.uriString)
                    pinnedVideoKeys = prefsRepo.removePinnedVideo(vid.id, vid.uriString)
                }
                pinnedFolderNames = prefsRepo.removePinnedFolder(op.folderName)
                if (activeFolderId != null) {
                    activeFolderId = null
                }
                mediaOpFeedback = if (isArabic) "تم حذف المجلد" else "Folder deleted"
            }
        }
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        val op = pendingMediaOp
        val alreadyHandled = pendingOpAlreadyHandled
        pendingMediaOp = null
        pendingOpAlreadyHandled = false
        if (op != null && activityResult.resultCode == Activity.RESULT_OK) {
            if (alreadyHandled && (op is LocalMediaScanner.PendingMediaOp.Delete || op is LocalMediaScanner.PendingMediaOp.DeleteFolder)) {
                applyMediaOpSuccess(op, LocalMediaScanner.MediaFileOpResult.Success())
            } else {
                scope.launch {
                    val retryResult = withContext(Dispatchers.IO) {
                        when (op) {
                            is LocalMediaScanner.PendingMediaOp.Rename ->
                                LocalMediaScanner.renameVideo(context, op.video, op.newDisplayName, allowSystemPrompt = false)
                            is LocalMediaScanner.PendingMediaOp.Move ->
                                LocalMediaScanner.moveVideoToFolder(context, op.video, op.targetFolderName, op.targetRelativePath, allowSystemPrompt = false)
                            is LocalMediaScanner.PendingMediaOp.Delete ->
                                LocalMediaScanner.deleteVideo(context, op.video, allowSystemPrompt = false)
                            is LocalMediaScanner.PendingMediaOp.RenameFolder ->
                                LocalMediaScanner.renameFolder(context, op.oldFolderName, op.videos, op.newFolderName, allowSystemPrompt = false)
                            is LocalMediaScanner.PendingMediaOp.DeleteFolder ->
                                LocalMediaScanner.deleteFolderVideos(context, op.folderName, op.videos, allowSystemPrompt = false)
                        }
                    }
                    when (retryResult) {
                        is LocalMediaScanner.MediaFileOpResult.Success -> applyMediaOpSuccess(op, retryResult)
                        is LocalMediaScanner.MediaFileOpResult.Error ->
                            mediaOpFeedback = if (isArabic) retryResult.messageAr else retryResult.messageEn
                        is LocalMediaScanner.MediaFileOpResult.RequiresUserIntent ->
                            mediaOpFeedback = if (isArabic) "تعذر إكمال العملية." else "Could not complete operation."
                    }
                }
            }
        }
    }

    val dispatchMediaOp: (LocalMediaScanner.PendingMediaOp) -> Unit = { op ->
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                when (op) {
                    is LocalMediaScanner.PendingMediaOp.Rename ->
                        LocalMediaScanner.renameVideo(context, op.video, op.newDisplayName, allowSystemPrompt = true)
                    is LocalMediaScanner.PendingMediaOp.Move ->
                        LocalMediaScanner.moveVideoToFolder(context, op.video, op.targetFolderName, op.targetRelativePath, allowSystemPrompt = true)
                    is LocalMediaScanner.PendingMediaOp.Delete ->
                        LocalMediaScanner.deleteVideo(context, op.video, allowSystemPrompt = true)
                    is LocalMediaScanner.PendingMediaOp.RenameFolder ->
                        LocalMediaScanner.renameFolder(context, op.oldFolderName, op.videos, op.newFolderName, allowSystemPrompt = true)
                    is LocalMediaScanner.PendingMediaOp.DeleteFolder ->
                        LocalMediaScanner.deleteFolderVideos(context, op.folderName, op.videos, allowSystemPrompt = true)
                }
            }
            when (result) {
                is LocalMediaScanner.MediaFileOpResult.Success -> applyMediaOpSuccess(op, result)
                is LocalMediaScanner.MediaFileOpResult.RequiresUserIntent -> {
                    pendingMediaOp = result.pendingOp
                    pendingOpAlreadyHandled = result.alreadyHandledBySystemDialog
                    mediaPermissionLauncher.launch(IntentSenderRequest.Builder(result.intentSender).build())
                }
                is LocalMediaScanner.MediaFileOpResult.Error ->
                    mediaOpFeedback = if (isArabic) result.messageAr else result.messageEn
            }
        }
    }

    var headerFullHeightPx by remember { mutableFloatStateOf(0f) }
    var activeScrollTracker by remember { mutableStateOf<WatchScrollTracker?>(null) }
    var isBottomBarHidden by remember { mutableStateOf(false) }

    val activeListIsScrolling by remember {
        derivedStateOf { activeScrollTracker?.isScrollInProgress == true }
    }
    val scrollSignalFlow = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    LaunchedEffect(isBottomBarHidden) {
        onBottomBarHiddenChange(isBottomBarHidden)
    }

    DisposableEffect(Unit) {
        onDispose { onBottomBarHiddenChange(false) }
    }

    LaunchedEffect(showWatchSettings, filteredVideos.isEmpty()) {
        if (filteredVideos.isEmpty()) {
            activeScrollTracker = null
            isBottomBarHidden = false
        } else if (showWatchSettings) {
            isBottomBarHidden = false
        }
    }

    // Reappear only after 1 full second of stillness; any continued scrolling cancels & restarts timer
    LaunchedEffect(Unit) {
        combine(
            snapshotFlow { isBottomBarHidden },
            snapshotFlow { activeListIsScrolling },
            scrollSignalFlow.onStart { emit(Unit) }
        ) { hidden, scrolling, _ ->
            hidden to scrolling
        }.collectLatest { (hidden, scrolling) ->
            if (hidden && !scrolling) {
                delay(1000L)
                isBottomBarHidden = false
            }
        }
    }

    // Track scroll direction/activity with zero recompositions per scroll frame
    val bottomBarNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val canScroll = activeScrollTracker?.canScroll == true
                if (source == NestedScrollSource.UserInput && canScroll) {
                    if (available.y < -1.5f) {
                        if (!isBottomBarHidden) {
                            isBottomBarHidden = true
                        }
                        scrollSignalFlow.tryEmit(Unit)
                    } else if (isBottomBarHidden && abs(available.y) > 1.5f) {
                        scrollSignalFlow.tryEmit(Unit)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (consumed.y < -1.5f) {
                    if (!isBottomBarHidden) {
                        isBottomBarHidden = true
                    }
                    scrollSignalFlow.tryEmit(Unit)
                } else if (isBottomBarHidden && abs(consumed.y) > 1.5f) {
                    scrollSignalFlow.tryEmit(Unit)
                }
                return Offset.Zero
            }
        }
    }

    val fadeToBlurProgress by animateFloatAsState(
        targetValue = if (isBottomBarHidden) 1f else 0f,
        animationSpec = tween(
            durationMillis = 320,
            easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
        ),
        label = "watch_bottom_fade_to_blur"
    )

    val density = LocalDensity.current
    val statusBarTopDp = LocalStableStatusBarTop.current ?: rememberStableStatusBarTopDp()
    val headerTopPaddingDp = with(density) { headerFullHeightPx.toDp() }.coerceAtLeast(statusBarTopDp)
    val bottomFadeHeightDp = (bottomBarPadding + 42.dp).coerceAtLeast(108.dp)
    val topFadeHeightDp = statusBarTopDp + 72.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("watch_screen_part2")
    ) {
        // Liquid Glass M5 Ambient Glow flowing seamlessly from top edge behind transparent status bar
        AuraAtmosphericLayer()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(bottomBarNestedScrollConnection)
                .clipToBounds()
        ) {
            // 1. Scrollable Content Area at FIXED full height (never remeasures on scroll!)
            if (!isLoading && filteredVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = headerTopPaddingDp,
                            bottom = bottomBarPadding,
                            start = 24.dp,
                            end = 24.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(aura.accentCyanSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = aura.accentCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    if (isArabic) "لا توجد نتائج مطابقة" else "No Matching Videos"
                                } else {
                                    if (isArabic) "لا توجد فيديوهات في الهاتف" else "No Videos Found on Phone"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = aura.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    if (isArabic) "جرب البحث باسم ملف آخر." else "Try searching with a different file name."
                                } else {
                                    if (isArabic) {
                                        "لم يتم العثور على أي مقطع فيديو في ذاكرة الهاتف. اضغط على زر فتح ملفات الهاتف لاختيار الفيديوهات أو امنح صلاحية الوصول."
                                    } else {
                                        "No local videos found on your phone. Tap 'Open Phone Files' to choose videos from device storage, or grant permissions."
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    filePickerLauncher.launch(arrayOf("video/*"))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = aura.accentCyan,
                                    contentColor = if (aura.isDark) TextDark else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "فتح ملفات الهاتف" else "Open Phone Files",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    quickMediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = aura.textPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, aura.borderSubtle),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Text(
                                    text = if (isArabic) "معرض الفيديوهات (Picker)" else "Video Gallery Picker",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                val layoutDir = LocalLayoutDirection.current
                val dirMultiplier = if (layoutDir == LayoutDirection.Rtl) -1 else 1
                val watchSmoothEnter = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
                val watchSmoothExit = remember { CubicBezierEasing(0.4f, 0f, 1f, 1f) }
                val contentKey = WatchContentKey(
                    category = if (activeFolderId != null) WatchCategory.FILES else selectedCategory,
                    fileDisplayMode = selectedFileDisplayMode,
                    folderId = activeFolderId
                )

                val magazineRows = remember(filteredVideos) {
                    buildMagazineRows(filteredVideos)
                }
                val galleryMosaicRows = remember(filteredVideos) {
                    buildGalleryMosaicRows(filteredVideos)
                }

                AnimatedContent(
                    targetState = contentKey,
                    transitionSpec = {
                        val openingFolder = initialState.folderId == null && targetState.folderId != null
                        val closingFolder = initialState.folderId != null && targetState.folderId == null
                        when {
                            openingFolder -> {
                                ContentTransform(
                                    targetContentEnter = slideInHorizontally(
                                        animationSpec = tween(290, easing = watchSmoothEnter),
                                        initialOffsetX = { it * dirMultiplier }
                                    ) + fadeIn(tween(220, easing = watchSmoothEnter)),
                                    initialContentExit = slideOutHorizontally(
                                        animationSpec = tween(290, easing = watchSmoothEnter),
                                        targetOffsetX = { -(it * 0.22f * dirMultiplier).toInt() }
                                    ) + fadeOut(tween(180)),
                                    targetContentZIndex = 1f,
                                    sizeTransform = SizeTransform(clip = true)
                                )
                            }
                            closingFolder -> {
                                ContentTransform(
                                    targetContentEnter = slideInHorizontally(
                                        animationSpec = tween(260, easing = watchSmoothEnter),
                                        initialOffsetX = { -(it * 0.22f * dirMultiplier).toInt() }
                                    ) + fadeIn(tween(220, easing = watchSmoothEnter)),
                                    initialContentExit = slideOutHorizontally(
                                        animationSpec = tween(250, easing = watchSmoothExit),
                                        targetOffsetX = { it * dirMultiplier }
                                    ) + fadeOut(tween(180)),
                                    targetContentZIndex = -1f,
                                    sizeTransform = SizeTransform(clip = true)
                                )
                            }
                            else -> {
                                ContentTransform(
                                    targetContentEnter = fadeIn(
                                        animationSpec = tween(185, delayMillis = 65, easing = FastOutSlowInEasing)
                                    ) + scaleIn(
                                        initialScale = 0.985f,
                                        animationSpec = tween(195, delayMillis = 65, easing = FastOutSlowInEasing)
                                    ),
                                    initialContentExit = fadeOut(
                                        animationSpec = tween(65, easing = FastOutLinearInEasing)
                                    ) + scaleOut(
                                        targetScale = 0.985f,
                                        animationSpec = tween(65, easing = FastOutLinearInEasing)
                                    ),
                                    sizeTransform = SizeTransform(clip = true)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds(),
                    label = "watch_view_mode_transition"
                ) { stateKey ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        if (stateKey.category == WatchCategory.FOLDERS && stateKey.folderId == null) {
                        when (stateKey.fileDisplayMode) {
                            WatchFileDisplayMode.LIST -> {
                                val tracker = remember(foldersListState) { ListScrollTracker(foldersListState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyColumn(
                                    state = foldersListState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 16.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item(key = "__top_header_spacer__", contentType = "top_spacer") {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(folders, key = { it.id }, contentType = { "folder_list" }) { folder ->
                                        val coverPath = if (watchSettings.autoFetchOnlineFolderCovers) {
                                            cachedFolderCovers[OnlineFolderCoverFetcher.buildFolderCacheKey(folder.name, false)]?.localFilePath
                                        } else null
                                        val leadThumb = folder.videos.firstOrNull()?.let { thumbnailBitmaps[it.id] }
                                        FolderItemCard(
                                            folder = folder,
                                            isPinned = isFolderPinned(folder),
                                            isSelected = contextMenuFolder?.id == folder.id,
                                            coverFilePath = coverPath,
                                            leadThumbnailImage = leadThumb,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                contextMenuVideo = null
                                                contextMenuFolder = null
                                                activeFolderId = folder.id
                                            },
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVideo = null
                                                contextMenuFolder = if (contextMenuFolder?.id == folder.id) null else folder
                                            }
                                        )
                                    }
                                }
                            }

                            WatchFileDisplayMode.GRID -> {
                                val gridState = rememberLazyGridState()
                                val tracker = remember(gridState) { GridScrollTracker(gridState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("watch_folders_grid_list"),
                                    contentPadding = PaddingValues(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 16.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item(
                                        key = "__top_header_spacer__",
                                        span = { GridItemSpan(maxLineSpan) },
                                        contentType = "top_spacer"
                                    ) {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(folders, key = { it.id }, contentType = { "folder_grid" }) { folder ->
                                        val coverPath = if (watchSettings.autoFetchOnlineFolderCovers) {
                                            cachedFolderCovers[OnlineFolderCoverFetcher.buildFolderCacheKey(folder.name, false)]?.localFilePath
                                        } else null
                                        val leadThumb = folder.videos.firstOrNull()?.let { thumbnailBitmaps[it.id] }
                                        FolderGridCard(
                                            folder = folder,
                                            isPinned = isFolderPinned(folder),
                                            isSelected = contextMenuFolder?.id == folder.id,
                                            coverFilePath = coverPath,
                                            leadThumbnailImage = leadThumb,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                contextMenuVideo = null
                                                contextMenuFolder = null
                                                activeFolderId = folder.id
                                            },
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVideo = null
                                                contextMenuFolder = if (contextMenuFolder?.id == folder.id) null else folder
                                            }
                                        )
                                    }
                                }
                            }

                            WatchFileDisplayMode.MAGAZINE -> {
                                val listState = rememberLazyListState()
                                val tracker = remember(listState) { ListScrollTracker(listState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("watch_folders_magazine_list"),
                                    contentPadding = PaddingValues(
                                        start = 18.dp,
                                        end = 18.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 18.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    item(key = "__top_header_spacer__", contentType = "top_spacer") {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    itemsIndexed(
                                        items = folders,
                                        key = { _, folder -> "mag_folder_${folder.id}" },
                                        contentType = { _, _ -> "folder_magazine" }
                                    ) { index, folder ->
                                        val coverPath = if (watchSettings.autoFetchOnlineFolderCovers) {
                                            cachedFolderCovers[OnlineFolderCoverFetcher.buildFolderCacheKey(folder.name, false)]?.localFilePath
                                        } else null
                                        val leadThumb = folder.videos.firstOrNull()?.let { thumbnailBitmaps[it.id] }
                                        FolderMagazineCard(
                                            folder = folder,
                                            issueNumber = index + 1,
                                            isArabic = isArabic,
                                            isPinned = isFolderPinned(folder),
                                            isSelected = contextMenuFolder?.id == folder.id,
                                            coverFilePath = coverPath,
                                            leadThumbnailImage = leadThumb,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                contextMenuVideo = null
                                                contextMenuFolder = null
                                                activeFolderId = folder.id
                                            },
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVideo = null
                                                contextMenuFolder = if (contextMenuFolder?.id == folder.id) null else folder
                                            }
                                        )
                                    }
                                }
                            }

                            WatchFileDisplayMode.GALLERY_VIEW -> {
                                val gridState = rememberLazyGridState()
                                val tracker = remember(gridState) { GridScrollTracker(gridState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("watch_folders_gallery_grid"),
                                    contentPadding = PaddingValues(
                                        start = 4.dp,
                                        end = 4.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 12.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    item(
                                        key = "__top_header_spacer__",
                                        span = { GridItemSpan(maxLineSpan) },
                                        contentType = "top_spacer"
                                    ) {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(folders, key = { "gal_folder_${it.id}" }, contentType = { "folder_gallery" }) { folder ->
                                        val coverPath = if (watchSettings.autoFetchOnlineFolderCovers) {
                                            cachedFolderCovers[OnlineFolderCoverFetcher.buildFolderCacheKey(folder.name, false)]?.localFilePath
                                        } else null
                                        val leadThumb = folder.videos.firstOrNull()?.let { thumbnailBitmaps[it.id] }
                                        FolderGalleryTileCard(
                                            folder = folder,
                                            isArabic = isArabic,
                                            isPinned = isFolderPinned(folder),
                                            isSelected = contextMenuFolder?.id == folder.id,
                                            coverFilePath = coverPath,
                                            leadThumbnailImage = leadThumb,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                contextMenuVideo = null
                                                contextMenuFolder = null
                                                activeFolderId = folder.id
                                            },
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVideo = null
                                                contextMenuFolder = if (contextMenuFolder?.id == folder.id) null else folder
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        } else {
                        when (stateKey.fileDisplayMode) {
                            WatchFileDisplayMode.LIST -> {
                                val listState = rememberLazyListState()
                                val tracker = remember(listState) { ListScrollTracker(listState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 16.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item(key = "__top_header_spacer__", contentType = "top_spacer") {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(filteredVideos, key = { it.id }, contentType = { "video_list" }) { video ->
                                        val savedProg = videoProgressByKey[prefsRepo.canonicalVideoKey(video.id, video.uriString)]
                                            ?: videoProgressByKey["raw_id:${video.id}"]
                                        FileListItemCard(
                                            video = video,
                                            thumbnailImage = thumbnailBitmaps[video.id],
                                            savedProgress = savedProg,
                                            isArabic = isArabic,
                                            hideFileExtensions = hideFileExtensions,
                                            isNewVideo = isVideoNew(video),
                                            isPinned = isVideoPinned(video),
                                            isSelected = contextMenuVideo?.id == video.id,
                                            onPlay = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVideo = null
                                                contextMenuFolder = null
                                                openVideoForPlayback(video.toMediaItem())
                                            },
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuFolder = null
                                                contextMenuVideo = if (contextMenuVideo?.id == video.id) null else video
                                            }
                                        )
                                    }
                                }
                            }

                            WatchFileDisplayMode.GRID -> {
                                val gridState = rememberLazyGridState()
                                val tracker = remember(gridState) { GridScrollTracker(gridState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 16.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item(
                                        key = "__top_header_spacer__",
                                        span = { GridItemSpan(maxLineSpan) },
                                        contentType = "top_spacer"
                                    ) {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(filteredVideos, key = { it.id }, contentType = { "video_grid" }) { video ->
                                        val savedProg = videoProgressByKey[prefsRepo.canonicalVideoKey(video.id, video.uriString)]
                                            ?: videoProgressByKey["raw_id:${video.id}"]
                                        MagazineGridItemCard(
                                            video = video,
                                            thumbnailImage = thumbnailBitmaps[video.id],
                                            savedProgress = savedProg,
                                            isArabic = isArabic,
                                            hideFileExtensions = hideFileExtensions,
                                            isNewVideo = isVideoNew(video),
                                            isPinned = isVideoPinned(video),
                                            isSelected = contextMenuVideo?.id == video.id,
                                            onPlay = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVideo = null
                                                contextMenuFolder = null
                                                openVideoForPlayback(video.toMediaItem())
                                            },
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuFolder = null
                                                contextMenuVideo = if (contextMenuVideo?.id == video.id) null else video
                                            }
                                        )
                                    }
                                }
                            }

                            WatchFileDisplayMode.MAGAZINE -> {
                                val listState = rememberLazyListState()
                                val tracker = remember(listState) { ListScrollTracker(listState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("watch_magazine_editorial_list"),
                                    contentPadding = PaddingValues(
                                        start = 18.dp,
                                        end = 18.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 18.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item(key = "__top_header_spacer__", contentType = "top_spacer") {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(
                                        items = magazineRows,
                                        key = { it.rowId },
                                        contentType = { row ->
                                            when (row) {
                                                is MagazineFeedRow.HeroSpread -> "hero"
                                                is MagazineFeedRow.DualPortraitSpread -> "dual"
                                                is MagazineFeedRow.SpotlightStrip -> "spotlight"
                                            }
                                        }
                                    ) { row ->
                                        when (row) {
                                            is MagazineFeedRow.HeroSpread -> {
                                                val savedProg = videoProgressByKey[prefsRepo.canonicalVideoKey(row.video.id, row.video.uriString)]
                                                    ?: videoProgressByKey["raw_id:${row.video.id}"]
                                                MagazineEditorialHeroCard(
                                                    video = row.video,
                                                    issueNumber = row.issueNumber,
                                                    isArabic = isArabic,
                                                    thumbnailImage = thumbnailBitmaps[row.video.id],
                                                    savedProgress = savedProg,
                                                    hideFileExtensions = hideFileExtensions,
                                                    isNewVideo = isVideoNew(row.video),
                                                    isPinned = isVideoPinned(row.video),
                                                    isSelected = contextMenuVideo?.id == row.video.id,
                                                    onPlay = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        contextMenuVideo = null
                                                        contextMenuFolder = null
                                                        openVideoForPlayback(row.video.toMediaItem())
                                                    },
                                                    onLongPress = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        contextMenuFolder = null
                                                        contextMenuVideo = if (contextMenuVideo?.id == row.video.id) null else row.video
                                                    }
                                                )
                                            }

                                            is MagazineFeedRow.DualPortraitSpread -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        val leftProg = videoProgressByKey[prefsRepo.canonicalVideoKey(row.left.id, row.left.uriString)]
                                                            ?: videoProgressByKey["raw_id:${row.left.id}"]
                                                        MagazinePortraitCoverCard(
                                                            video = row.left,
                                                            issueNumber = row.leftIssue,
                                                            isArabic = isArabic,
                                                            thumbnailImage = thumbnailBitmaps[row.left.id],
                                                            savedProgress = leftProg,
                                                            hideFileExtensions = hideFileExtensions,
                                                            isNewVideo = isVideoNew(row.left),
                                                            isPinned = isVideoPinned(row.left),
                                                            isSelected = contextMenuVideo?.id == row.left.id,
                                                            onPlay = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                contextMenuVideo = null
                                                                contextMenuFolder = null
                                                                openVideoForPlayback(row.left.toMediaItem())
                                                            },
                                                            onLongPress = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                contextMenuFolder = null
                                                                contextMenuVideo = if (contextMenuVideo?.id == row.left.id) null else row.left
                                                            }
                                                        )
                                                    }
                                                    if (row.right != null && row.rightIssue != null) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            val rightProg = videoProgressByKey[prefsRepo.canonicalVideoKey(row.right.id, row.right.uriString)]
                                                                ?: videoProgressByKey["raw_id:${row.right.id}"]
                                                            MagazinePortraitCoverCard(
                                                                video = row.right,
                                                                issueNumber = row.rightIssue,
                                                                isArabic = isArabic,
                                                                thumbnailImage = thumbnailBitmaps[row.right.id],
                                                                savedProgress = rightProg,
                                                                hideFileExtensions = hideFileExtensions,
                                                                isNewVideo = isVideoNew(row.right),
                                                                isPinned = isVideoPinned(row.right),
                                                                isSelected = contextMenuVideo?.id == row.right.id,
                                                                onPlay = {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    contextMenuVideo = null
                                                                    contextMenuFolder = null
                                                                    openVideoForPlayback(row.right.toMediaItem())
                                                                },
                                                                onLongPress = {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    contextMenuFolder = null
                                                                    contextMenuVideo = if (contextMenuVideo?.id == row.right.id) null else row.right
                                                                }
                                                            )
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }

                                            is MagazineFeedRow.SpotlightStrip -> {
                                                val savedProg = videoProgressByKey[prefsRepo.canonicalVideoKey(row.video.id, row.video.uriString)]
                                                    ?: videoProgressByKey["raw_id:${row.video.id}"]
                                                MagazineSpotlightCard(
                                                    video = row.video,
                                                    issueNumber = row.issueNumber,
                                                    isArabic = isArabic,
                                                    thumbnailImage = thumbnailBitmaps[row.video.id],
                                                    savedProgress = savedProg,
                                                    hideFileExtensions = hideFileExtensions,
                                                    isNewVideo = isVideoNew(row.video),
                                                    isPinned = isVideoPinned(row.video),
                                                    isSelected = contextMenuVideo?.id == row.video.id,
                                                    onPlay = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        contextMenuVideo = null
                                                        contextMenuFolder = null
                                                        openVideoForPlayback(row.video.toMediaItem())
                                                    },
                                                    onLongPress = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        contextMenuFolder = null
                                                        contextMenuVideo = if (contextMenuVideo?.id == row.video.id) null else row.video
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            WatchFileDisplayMode.GALLERY_VIEW -> {
                                val listState = rememberLazyListState()
                                val tracker = remember(listState) { ListScrollTracker(listState) }
                                if (stateKey == contentKey) {
                                    SideEffect {
                                        if (activeScrollTracker !== tracker) {
                                            activeScrollTracker = tracker
                                        }
                                    }
                                }
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("watch_gallery_mosaic_wall"),
                                    contentPadding = PaddingValues(
                                        start = 2.dp,
                                        end = 2.dp,
                                        top = 0.dp,
                                        bottom = bottomBarPadding + 12.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    item(key = "__top_header_spacer__", contentType = "top_spacer") {
                                        Spacer(modifier = Modifier.height(headerTopPaddingDp))
                                    }
                                    items(galleryMosaicRows, key = { it.id }, contentType = { "gallery_row" }) { row ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(row.height),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            row.videos.forEachIndexed { index, video ->
                                                val tileWeight = row.weights.getOrElse(index) { 1f }
                                                val savedProg = videoProgressByKey[prefsRepo.canonicalVideoKey(video.id, video.uriString)]
                                                    ?: videoProgressByKey["raw_id:${video.id}"]
                                                GalleryMosaicTileCard(
                                                    video = video,
                                                    isArabic = isArabic,
                                                    thumbnailImage = thumbnailBitmaps[video.id],
                                                    savedProgress = savedProg,
                                                    isNewVideo = isVideoNew(video),
                                                    isPinned = isVideoPinned(video),
                                                    isSelected = contextMenuVideo?.id == video.id,
                                                    onPlay = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        contextMenuVideo = null
                                                        contextMenuFolder = null
                                                        openVideoForPlayback(video.toMediaItem())
                                                    },
                                                    onLongPress = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        contextMenuFolder = null
                                                        contextMenuVideo = if (contextMenuVideo?.id == video.id) null else video
                                                    },
                                                    modifier = Modifier
                                                        .weight(tileWeight)
                                                        .fillMaxHeight()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = headerTopPaddingDp + 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = aura.accentCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "جاري فحص فيديوهات الهاتف..." else "Scanning device videos...",
                            style = MaterialTheme.typography.bodySmall.copy(color = aura.textSecondary)
                        )
                    }
                }
            }

            // Bottom Fade-to-Blur atmospheric veil replacing the bottom bar while scrolling
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(bottomFadeHeightDp)
                    .graphicsLayer { alpha = fadeToBlurProgress }
            ) {
                // Soft Gaussian frosted-diffusion layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 14.dp)
                        .blur(radius = 26.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    aura.surfaceElevated.copy(alpha = 0.30f),
                                    aura.background.copy(alpha = 0.70f),
                                    aura.background.copy(alpha = 0.94f)
                                )
                            )
                        )
                )
                // Multi-stop vertical fade scrim dissolving scrolling cards into bottom chrome
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.28f to aura.background.copy(alpha = 0.22f),
                                    0.58f to aura.background.copy(alpha = 0.62f),
                                    0.82f to aura.background.copy(alpha = 0.88f),
                                    1.0f to aura.background.copy(alpha = 0.98f)
                                )
                            )
                        )
                )
            }

            // Permanent Top Fade-to-Blur atmospheric veil softening scrolling cards from the very top edge
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(topFadeHeightDp)
            ) {
                // Soft Gaussian frosted-diffusion layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 14.dp)
                        .blur(radius = 26.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    aura.background.copy(alpha = 0.92f),
                                    aura.background.copy(alpha = 0.66f),
                                    aura.surfaceElevated.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Multi-stop vertical fade scrim dissolving scrolling cards at the top edge
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to aura.background.copy(alpha = 0.96f),
                                    0.20f to aura.background.copy(alpha = 0.84f),
                                    0.46f to aura.background.copy(alpha = 0.56f),
                                    0.74f to aura.background.copy(alpha = 0.20f),
                                    1.0f to Color.Transparent
                                )
                            )
                        )
                )
            }

            // 2. Overlaid Collapsible Top Header (Stays hidden during downward scroll until top videos are reached, or uncollapses while long-press actions are active)
            val selectionHeaderReveal by animateFloatAsState(
                targetValue = if (hasInlineSelection) 1f else 0f,
                animationSpec = tween(220, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
                label = "watch_selection_header_reveal"
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        val newH = size.height.toFloat()
                        if (newH > 0f && headerFullHeightPx != newH) {
                            headerFullHeightPx = newH
                        }
                    }
                    .graphicsLayer {
                        val maxH = headerFullHeightPx
                        val tracker = activeScrollTracker
                        val rawCollapsePx = if (maxH <= 1f || tracker == null) {
                            0f
                        } else if (tracker.firstVisibleItemIndex == 0) {
                            tracker.firstVisibleItemScrollOffset.toFloat().coerceIn(0f, maxH)
                        } else {
                            maxH
                        }
                        val collapsePx = rawCollapsePx * (1f - selectionHeaderReveal)
                        translationY = if (collapsePx >= maxH - 0.5f && maxH > 1f) {
                            -maxH - 8f
                        } else {
                            -collapsePx
                        }
                        val fraction = if (maxH > 1f) (collapsePx / maxH).coerceIn(0f, 1f) else 0f
                        alpha = (1f - fraction * 1.15f).coerceIn(0f, 1f)
                    }
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to aura.background.copy(alpha = 0.78f),
                                0.65f to aura.background.copy(alpha = 0.68f),
                                0.90f to aura.background.copy(alpha = 0.34f),
                                1.0f to Color.Transparent
                            )
                        )
                    )
                    .stableStatusBarsPadding()
            ) {
            // Watch Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val headerSmoothEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
                    AnimatedContent(
                        targetState = activeFolderId != null,
                        transitionSpec = {
                            (fadeIn(tween(220, easing = headerSmoothEasing)) +
                                scaleIn(initialScale = 0.85f, animationSpec = tween(220, easing = headerSmoothEasing))) togetherWith
                                (fadeOut(tween(150)) + scaleOut(targetScale = 0.85f, animationSpec = tween(150)))
                        },
                        label = "watch_header_icon_transition"
                    ) { inFolder ->
                        if (inFolder) {
                            IconButton(
                                onClick = { activeFolderId = null },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(aura.iconButtonBg)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to folders",
                                    tint = aura.textPrimary
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(aura.accentCyan, aura.accentViolet))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = if (aura.isDark) TextDark else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AnimatedContent(
                        targetState = activeFolderId,
                        transitionSpec = {
                            val enteringFolder = targetState != null
                            (slideInVertically(tween(240, easing = headerSmoothEasing)) {
                                if (enteringFolder) it / 3 else -it / 3
                            } + fadeIn(tween(220, easing = headerSmoothEasing))) togetherWith
                                (slideOutVertically(tween(160)) {
                                    if (enteringFolder) -it / 3 else it / 3
                                } + fadeOut(tween(140)))
                        },
                        label = "watch_header_title_transition"
                    ) { folderKey ->
                        val activeFolder = folderKey?.let { id -> folders.find { it.id == id } }
                        val shownCount = activeFolder?.count ?: localVideos.size
                        Column {
                            Text(
                                text = activeFolder?.name ?: if (isArabic) "فيديوهات" else "Videos",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = aura.textPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) "$shownCount فيديو" else "$shownCount videos",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Top-Right 3-Line Stacked Menu Button (Watch-only Settings & Bookmarks)
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showWatchSettings = true
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("watch_settings_menu_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(aura.iconButtonBg)
                            .border(1.dp, aura.borderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = if (isArabic) "إعدادات صفحة Watch" else "Watch settings",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            // Permission Request Banner (if permission is not yet granted)
            if (!hasStoragePermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, aura.accentCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .testTag("storage_permission_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = aura.accentCyanSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = aura.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "إذن قراءة ملفات الفيديو في الهاتف" else "Device Storage Permission Required",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = aura.textPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) {
                                "يحتاج التطبيق لإذن قراءة ملفات الفيديو لعرض جميع مقاطع الفيديو المحفوظة في ذاكرة هاتفك وتشغيلها مباشرة."
                            } else {
                                "Grant video access permission to automatically discover and list all videos on your device storage."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    permissionLauncher.launch(LocalMediaScanner.getRequiredPermissions())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = aura.accentCyan,
                                    contentColor = if (aura.isDark) TextDark else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isArabic) "منح الصلاحية الآن" else "Grant Permission",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    filePickerLauncher.launch(arrayOf("video/*"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, aura.borderSubtle),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = aura.textPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isArabic) "تصفح ملفات الهاتف" else "Browse Files",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Classification & File Display Selector: Folder | Files | Display Cycler (List / Grid / Magazine / Gallery View)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Folder Chip (no double-tap layout switching)
                val isFolderSelected = selectedCategory == WatchCategory.FOLDERS && activeFolderId == null
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFolderSelected) aura.accentCyanSoft else aura.unselectedChipBg,
                    border = BorderStroke(
                        1.dp,
                        if (isFolderSelected) aura.accentCyan else aura.borderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = WatchCategory.FOLDERS
                            activeFolderId = null
                            prefsRepo.saveWatchViewMode("${selectedCategory.name}:${selectedFileDisplayMode.name}")
                        }
                        .testTag("view_mode_folders")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = WatchCategory.FOLDERS.icon,
                            contentDescription = null,
                            tint = if (isFolderSelected) aura.accentCyan else aura.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isArabic) WatchCategory.FOLDERS.titleAr else WatchCategory.FOLDERS.titleEn,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isFolderSelected) aura.accentCyan else aura.textPrimary,
                                fontWeight = if (isFolderSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // 2. Files Chip
                val isFilesSelected = selectedCategory == WatchCategory.FILES && activeFolderId == null
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFilesSelected) aura.accentCyanSoft else aura.unselectedChipBg,
                    border = BorderStroke(
                        1.dp,
                        if (isFilesSelected) aura.accentCyan else aura.borderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = WatchCategory.FILES
                            activeFolderId = null
                            prefsRepo.saveWatchViewMode("${selectedCategory.name}:${selectedFileDisplayMode.name}")
                        }
                        .testTag("view_mode_files")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = WatchCategory.FILES.icon,
                            contentDescription = null,
                            tint = if (isFilesSelected) aura.accentCyan else aura.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isArabic) WatchCategory.FILES.titleAr else WatchCategory.FILES.titleEn,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isFilesSelected) aura.accentCyan else aura.textPrimary,
                                fontWeight = if (isFilesSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // 3. Dynamic File Display Cycler (List -> Grid -> Magazine -> Gallery View) - Applies to both Folders & Files
                val isDisplayActive = true
                val chipSmoothEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDisplayActive) aura.accentCyanSoft else aura.unselectedChipBg,
                    border = BorderStroke(
                        1.dp,
                        if (isDisplayActive) aura.accentCyan else aura.borderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val allModes = WatchFileDisplayMode.entries
                            val nextMode = allModes[(selectedFileDisplayMode.ordinal + 1) % allModes.size]
                            selectedFileDisplayMode = nextMode
                            prefsRepo.saveWatchViewMode("${selectedCategory.name}:${nextMode.name}")
                        }
                        .testTag("view_mode_display_cycler")
                ) {
                    AnimatedContent(
                        targetState = selectedFileDisplayMode,
                        transitionSpec = {
                            (slideInVertically(tween(220, easing = chipSmoothEasing)) { it / 2 } +
                                fadeIn(tween(180))) togetherWith
                                (slideOutVertically(tween(160)) { -it / 2 } + fadeOut(tween(140)))
                        },
                        label = "display_mode_chip_label"
                    ) { currentMode ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) currentMode.titleAr else currentMode.titleEn,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDisplayActive) aura.accentCyan else aura.textPrimary,
                                    fontWeight = if (isDisplayActive) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Compact / Slim Search Bar OR Inline Side-by-Side Long-Press Action Buttons
            val inlineSlotState = when {
                contextMenuVideo != null -> 1
                contextMenuFolder != null -> 2
                else -> 0
            }
            AnimatedVisibility(visible = hasInlineSelection || watchSettings.showSearchBar) {
                AnimatedContent(
                    targetState = inlineSlotState,
                    transitionSpec = {
                        (fadeIn(tween(180, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.97f, animationSpec = tween(180, easing = FastOutSlowInEasing))) togetherWith
                            (fadeOut(tween(120)) + scaleOut(targetScale = 0.97f, animationSpec = tween(120)))
                    },
                    label = "watch_search_slot_inline_actions"
                ) { slotState ->
                    when (slotState) {
                        1 -> {
                            val selectedVideo = contextMenuVideo
                            if (selectedVideo != null) {
                                val videoPinned = isVideoPinned(selectedVideo)
                                val videoActions = listOf(
                                    WatchInlineActionItem(
                                        icon = Icons.Default.DeleteOutline,
                                        label = if (isArabic) "حذف" else "Delete",
                                        accent = Color(0xFFFF5C77),
                                        testTag = "video_action_delete",
                                        isDestructive = true,
                                        onClick = {
                                            contextMenuVideo = null
                                            videoToDelete = selectedVideo
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.DriveFileRenameOutline,
                                        label = if (isArabic) "تسمية" else "Rename",
                                        accent = aura.accentCyan,
                                        testTag = "video_action_rename",
                                        onClick = {
                                            contextMenuVideo = null
                                            videoToRename = selectedVideo
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.DriveFileMove,
                                        label = if (isArabic) "نقل" else "Move",
                                        accent = aura.accentGold,
                                        testTag = "video_action_move",
                                        onClick = {
                                            contextMenuVideo = null
                                            videoToMove = selectedVideo
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.Share,
                                        label = if (isArabic) "مشاركة" else "Share",
                                        accent = aura.accentViolet,
                                        testTag = "video_action_share",
                                        onClick = {
                                            contextMenuVideo = null
                                            val shared = LocalMediaScanner.shareVideo(context, selectedVideo)
                                            if (!shared) {
                                                mediaOpFeedback = if (isArabic) "تعذر مشاركة هذا الفيديو." else "Unable to share this video."
                                            }
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.PushPin,
                                        label = if (videoPinned) {
                                            if (isArabic) "إلغاء التثبيت" else "Unpin"
                                        } else {
                                            if (isArabic) "تثبيت" else "Pin"
                                        },
                                        accent = aura.accentGold,
                                        testTag = "video_action_pin_to_top",
                                        isHighlighted = videoPinned,
                                        onClick = {
                                            contextMenuVideo = null
                                            pinnedVideoKeys = prefsRepo.togglePinnedVideo(selectedVideo.id, selectedVideo.uriString)
                                            mediaOpFeedback = if (videoPinned) {
                                                if (isArabic) "تم إلغاء تثبيت الفيديو" else "Video unpinned"
                                            } else {
                                                if (isArabic) "تم تثبيت الفيديو في الأعلى" else "Pinned video to top"
                                            }
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.Info,
                                        label = if (isArabic) "معلومات" else "Info",
                                        accent = aura.textPrimary,
                                        testTag = "video_action_info",
                                        onClick = {
                                            contextMenuVideo = null
                                            scope.launch {
                                                val details = withContext(Dispatchers.IO) {
                                                    LocalMediaScanner.resolveVideoDetails(context, selectedVideo)
                                                }
                                                videoDetailsPair = selectedVideo to details
                                            }
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.PlayArrow,
                                        label = if (isArabic) "تشغيل" else "Play",
                                        accent = aura.accentCyan,
                                        testTag = "sheet_play_video_button",
                                        onClick = {
                                            contextMenuVideo = null
                                            openVideoForPlayback(selectedVideo.toMediaItem())
                                        }
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                ) {
                                    WatchInlineSelectionActionBar(
                                        isArabic = isArabic,
                                        actions = videoActions,
                                        onClose = { contextMenuVideo = null }
                                    )
                                }
                            }
                        }

                        2 -> {
                            val selectedFolder = contextMenuFolder
                            if (selectedFolder != null) {
                                val folderPinned = isFolderPinned(selectedFolder)
                                val folderActions = listOf(
                                    WatchInlineActionItem(
                                        icon = Icons.Default.DeleteOutline,
                                        label = if (isArabic) "حذف" else "Delete",
                                        accent = Color(0xFFFF5C77),
                                        testTag = "folder_action_delete",
                                        isDestructive = true,
                                        onClick = {
                                            contextMenuFolder = null
                                            folderToDelete = selectedFolder
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.DriveFileRenameOutline,
                                        label = if (isArabic) "تسمية" else "Rename",
                                        accent = aura.accentCyan,
                                        testTag = "folder_action_rename",
                                        onClick = {
                                            contextMenuFolder = null
                                            folderToRename = selectedFolder
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.Share,
                                        label = if (isArabic) "مشاركة" else "Share",
                                        accent = aura.accentViolet,
                                        testTag = "folder_action_share",
                                        onClick = {
                                            contextMenuFolder = null
                                            val shared = LocalMediaScanner.shareFolderVideos(context, selectedFolder.name, selectedFolder.videos)
                                            if (!shared) {
                                                mediaOpFeedback = if (isArabic) "تعذر مشاركة هذا المجلد." else "Unable to share this folder."
                                            }
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.PushPin,
                                        label = if (folderPinned) {
                                            if (isArabic) "إلغاء التثبيت" else "Unpin"
                                        } else {
                                            if (isArabic) "تثبيت" else "Pin"
                                        },
                                        accent = aura.accentGold,
                                        testTag = "folder_action_pin_to_top",
                                        isHighlighted = folderPinned,
                                        onClick = {
                                            contextMenuFolder = null
                                            pinnedFolderNames = prefsRepo.togglePinnedFolder(selectedFolder.name)
                                            mediaOpFeedback = if (folderPinned) {
                                                if (isArabic) "تم إلغاء تثبيت المجلد" else "Folder unpinned"
                                            } else {
                                                if (isArabic) "تم تثبيت المجلد في الأعلى" else "Pinned folder to top"
                                            }
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.Info,
                                        label = if (isArabic) "معلومات" else "Info",
                                        accent = aura.textPrimary,
                                        testTag = "folder_action_info",
                                        onClick = {
                                            contextMenuFolder = null
                                            val details = LocalMediaScanner.resolveFolderDetails(selectedFolder.name, selectedFolder.videos)
                                            folderDetailsPair = selectedFolder to details
                                        }
                                    ),
                                    WatchInlineActionItem(
                                        icon = Icons.Default.FolderOpen,
                                        label = if (isArabic) "فتح" else "Open",
                                        accent = aura.accentCyan,
                                        testTag = "sheet_open_folder_button",
                                        onClick = {
                                            contextMenuFolder = null
                                            activeFolderId = selectedFolder.id
                                        }
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                ) {
                                    WatchInlineSelectionActionBar(
                                        isArabic = isArabic,
                                        actions = folderActions,
                                        onClose = { contextMenuFolder = null }
                                    )
                                }
                            }
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    placeholder = {
                                        Text(
                                            text = if (isArabic) "بحث..." else "Search videos...",
                                            color = aura.textTertiary,
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = aura.accentCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = aura.accentCyan,
                                        unfocusedBorderColor = aura.borderSubtle,
                                        focusedContainerColor = aura.surfaceCard,
                                        unfocusedContainerColor = aura.surfaceSubtle,
                                        focusedTextColor = aura.textPrimary,
                                        unfocusedTextColor = aura.textPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Production-ready "Continue Watching" Section (متابعة المشاهدة)
            ContinueWatchingSection(
                localVideos = localVideos,
                onPlayMedia = { media ->
                    isContinueWatchingDismissed = false
                    openVideoForPlayback(media)
                },
                isArabic = isArabic,
                visible = watchSettings.showContinueWatching && activeFolderId == null && searchQuery.isBlank(),
                isDismissed = isContinueWatchingDismissed,
                onDismiss = { isContinueWatchingDismissed = true },
                hideFileExtensions = hideFileExtensions
            )
            }
        }

        // Categorized Watch-only Settings & Bookmarks Overlay (Auto-saved in Room DB)
        WatchSettingsOverlay(
            visible = showWatchSettings,
            isArabic = isArabic,
            watchSettings = watchSettings,
            playerConfig = playerConfig,
            selectedCategory = selectedCategory,
            selectedFileDisplayMode = selectedFileDisplayMode,
            hideFileExtensions = hideFileExtensions,
            autoResumePlayback = autoResumePlayback,
            backgroundPlayEnabled = backgroundPlayEnabled,
            isPremiumActivated = isPremiumActivatedState,
            activeSeekbarStyle = activeSeekbarStyle,
            unlockedSeekbarStyles = unlockedSeekbarStyles,
            unlockedAmbientModes = unlockedAmbientModes,
            bookmarkGroups = bookmarkGroups,
            localVideos = localVideos,
            onSelectCategory = { cat ->
                selectedCategory = cat
                activeFolderId = null
                prefsRepo.saveWatchViewMode("${cat.name}:${selectedFileDisplayMode.name}")
            },
            onSelectFileDisplayMode = { mode ->
                selectedFileDisplayMode = mode
                prefsRepo.saveWatchViewMode("${selectedCategory.name}:${mode.name}")
            },
            onToggleShowContinueWatching = { show ->
                watchViewModel.setShowContinueWatching(show)
                if (show) isContinueWatchingDismissed = false
            },
            onToggleShowSearchBar = { show ->
                watchViewModel.setShowSearchBar(show)
            },
            onToggleHideFileExtensions = { hide ->
                hideFileExtensions = hide
                prefsRepo.saveHideFileExtensions(hide)
            },
            onToggleShowNewVideoBadge = { show ->
                watchViewModel.setShowNewVideoBadge(show)
            },
            onSelectNewVideoBadgeDays = { days ->
                watchViewModel.setNewVideoBadgeDays(days)
            },
            onSelectSortBy = { sortBy ->
                watchViewModel.setSortBy(sortBy)
            },
            onSelectSortDirection = { dir ->
                watchViewModel.setSortDirection(dir)
            },
            onToggleHideShortClips = { hide ->
                watchViewModel.setHideShortClips(hide)
            },
            onToggleAutoFetchOnlineFolderCovers = { enabled ->
                watchViewModel.setAutoFetchOnlineFolderCovers(enabled)
            },
            onClearAndRefreshFolderCovers = {
                watchViewModel.clearAndRefreshFolderCovers(
                    folderNames = folders.map { it.name },
                    isMusicFolder = false
                )
            },
            cachedFolderCoversCount = cachedFolderCovers.size,
            onRescanVideos = {
                if (hasStoragePermission) {
                    scanVideosJob()
                } else {
                    permissionLauncher.launch(LocalMediaScanner.getRequiredPermissions())
                }
            },
            onImportPhoneFiles = {
                filePickerLauncher.launch(arrayOf("video/*"))
            },
            onOpenGalleryPicker = {
                quickMediaPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onToggleAmbientGlow = { enabled ->
                playerAmbientViewModel.setAmbientGlowEnabled(enabled)
            },
            onSelectBlackBlurMode = { mode ->
                playerAmbientViewModel.selectBlackBlurMode(mode)
            },
            onSelectResizeMode = { modeName ->
                playerAmbientViewModel.setResizeModeName(modeName)
            },
            onToggleAutoCropBlackBars = { enabled ->
                playerAmbientViewModel.setAutoCropBlackBars(enabled)
            },
            onSelectPlaybackSpeed = { speed ->
                playerAmbientViewModel.setPlaybackSpeed(speed)
            },
            onSelectDoubleTapSeekSeconds = { seconds ->
                playerAmbientViewModel.setDoubleTapSeekSeconds(seconds)
            },
            onSelectControlsAutoHideSeconds = { seconds ->
                playerAmbientViewModel.setControlsAutoHideSeconds(seconds)
            },
            onToggleSwipeGestures = { enabled ->
                playerAmbientViewModel.setEnableSwipeGestures(enabled)
            },
            onSelectAudioBoost = { boost ->
                playerAmbientViewModel.setAudioBoostMultiplier(boost)
            },
            onToggleAutoResumePlayback = { enabled ->
                autoResumePlayback = enabled
                if (streakViewModel != null) {
                    streakViewModel.saveAutoResume(enabled)
                } else {
                    prefsRepo.saveAutoResumePlayback(enabled)
                }
            },
            onToggleBackgroundPlay = { enabled ->
                backgroundPlayEnabled = enabled
                if (streakViewModel != null) {
                    streakViewModel.setBackgroundPlayEnabled(enabled)
                } else {
                    prefsRepo.setBackgroundPlayEnabled(enabled)
                }
            },
            onSelectSeekbarStyle = { style ->
                if (streakViewModel != null) {
                    streakViewModel.selectSeekbarStyle(style, isArabic = isArabic)
                } else {
                    prefsRepo.setActiveSeekbarStyle(style)
                }
                localSeekbarStyle = style
            },
            onSelectSeekbarThickness = { thickness ->
                playerAmbientViewModel.setSeekbarThickness(thickness)
            },
            onSelectSeekbarGlowIntensity = { intensity ->
                playerAmbientViewModel.setSeekbarGlowIntensity(intensity)
            },
            onToggleShowBookmarkMarkersOnSeekbar = { show ->
                playerAmbientViewModel.setShowBookmarkMarkersOnSeekbar(show)
            },
            onToggleLiveSeekPreview = { enabled ->
                playerAmbientViewModel.setLiveSeekPreview(enabled)
            },
            onToggleSeekbarBelowControls = { enabled ->
                playerAmbientViewModel.setSeekbarBelowControls(enabled)
            },
            onPlayBookmarkAtTimestamp = { group, timestampMs ->
                val matched = localVideos.find {
                    it.id == group.videoId || (group.uriString != null && it.uriString == group.uriString)
                }
                showWatchSettings = false
                openVideoForPlayback(
                    if (matched != null) {
                        matched.toMediaItem().copy(initialSeekPositionMs = timestampMs)
                    } else {
                        MediaItem(
                            id = group.videoId,
                            title = group.videoTitle,
                            titleArabic = group.videoTitle,
                            subtitleBadge = "BOOKMARKED",
                            episodesFreeBadge = "HD",
                            year = "Local",
                            seasonsOrDuration = formatDurationMsForItem(group.durationMs),
                            maturityRating = "All",
                            qualityTag = "HD",
                            genresText = "Bookmarked Video",
                            description = "Saved bookmark playback.",
                            posterRes = if (group.thumbnailRes != 0) group.thumbnailRes else R.drawable.img_hero_poster,
                            backdropRes = if (group.thumbnailRes != 0) group.thumbnailRes else R.drawable.img_hero_poster,
                            uriString = group.uriString,
                            initialSeekPositionMs = timestampMs
                        )
                    }
                )
            },
            onDeleteBookmark = { bookmarkId ->
                watchViewModel.deleteBookmarkById(bookmarkId)
            },
            onDeleteBookmarksForVideo = { group ->
                watchViewModel.deleteBookmarksForVideo(group.videoId, group.uriString)
            },
            onDeleteAllBookmarks = {
                watchViewModel.deleteAllBookmarks()
            },
            onDismiss = {
                showWatchSettings = false
            }
        )

        // Rename Video Dialog
        videoToRename?.let { target ->
            RenameVideoDialog(
                video = target,
                isArabic = isArabic,
                onConfirmRename = { newName ->
                    videoToRename = null
                    dispatchMediaOp(LocalMediaScanner.PendingMediaOp.Rename(target, newName))
                },
                onDismiss = { videoToRename = null }
            )
        }

        // Rename Folder Dialog
        folderToRename?.let { targetFolder ->
            RenameFolderDialog(
                currentFolderName = targetFolder.name,
                isArabic = isArabic,
                onConfirmRename = { newName ->
                    folderToRename = null
                    dispatchMediaOp(
                        LocalMediaScanner.PendingMediaOp.RenameFolder(
                            oldFolderName = targetFolder.name,
                            videos = targetFolder.videos,
                            newFolderName = newName
                        )
                    )
                },
                onDismiss = { folderToRename = null }
            )
        }

        // Move Video Folder Dialog
        videoToMove?.let { target ->
            val existingMoveTargets = remember(localVideos.toList()) {
                localVideos
                    .groupBy { it.folderName.ifBlank { "Movies" } }
                    .map { (name, vids) ->
                        name to vids.firstNotNullOfOrNull { it.relativePath }
                    }
                    .sortedBy { it.first.lowercase() }
            }
            MoveVideoFolderDialog(
                video = target,
                existingFolders = existingMoveTargets,
                isArabic = isArabic,
                onSelectFolder = { folderName, relPath ->
                    videoToMove = null
                    dispatchMediaOp(LocalMediaScanner.PendingMediaOp.Move(target, folderName, relPath))
                },
                onDismiss = { videoToMove = null }
            )
        }

        // Delete Video Confirmation Dialog
        videoToDelete?.let { target ->
            DeleteVideoConfirmationDialog(
                video = target,
                isArabic = isArabic,
                hideFileExtensions = hideFileExtensions,
                onConfirmDelete = {
                    videoToDelete = null
                    dispatchMediaOp(LocalMediaScanner.PendingMediaOp.Delete(target))
                },
                onDismiss = { videoToDelete = null }
            )
        }

        // Delete Folder Confirmation Dialog
        folderToDelete?.let { targetFolder ->
            DeleteFolderConfirmationDialog(
                folderName = targetFolder.name,
                videoCount = targetFolder.count,
                isArabic = isArabic,
                onConfirmDelete = {
                    folderToDelete = null
                    dispatchMediaOp(
                        LocalMediaScanner.PendingMediaOp.DeleteFolder(
                            folderName = targetFolder.name,
                            videos = targetFolder.videos
                        )
                    )
                },
                onDismiss = { folderToDelete = null }
            )
        }

        // Video Info / Details Dialog
        videoDetailsPair?.let { (target, details) ->
            val episodeInfo = remember(target.id, target.title, target.folderName, target.relativePath, target.uriString) {
                EpisodeParser.parseForVideo(target)
            }
            VideoInfoDialog(
                details = details,
                episodeInfo = episodeInfo,
                isArabic = isArabic,
                onDismiss = { videoDetailsPair = null }
            )
        }

        // Folder Info / Details Dialog
        folderDetailsPair?.let { (targetFolder, details) ->
            FolderInfoDialog(
                details = details,
                isPinned = isFolderPinned(targetFolder),
                isArabic = isArabic,
                onDismiss = { folderDetailsPair = null }
            )
        }

        // Sleek transient feedback toast for Rename / Move / Delete / Share
        AnimatedVisibility(
            visible = mediaOpFeedback != null,
            enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xE60B101D),
                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.55f)),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = mediaOpFeedback ?: "",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderGridCard(
    folder: VideoFolder,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    coverFilePath: String? = null,
    leadThumbnailImage: ImageBitmap? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val leadVideo = folder.videos.firstOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> aura.accentCyan
                    isPinned -> aura.accentGold.copy(alpha = 0.65f)
                    else -> aura.borderSubtle
                },
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .testTag("folder_grid_card_${folder.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
    ) {
        Column {
            // Validated HD online cover -> local high-res video frame -> SmartFallbackCover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
            ) {
                FolderCoverArtwork(
                    folder = folder,
                    coverFilePath = coverFilePath,
                    leadVideo = leadVideo,
                    leadThumbnailImage = leadThumbnailImage,
                    aspectRatio = 1.35f
                )
                if (isPinned) {
                    PinnedBadgePill(
                        compact = true,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = aura.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = aura.accentCyanSoft
                    ) {
                        Text(
                            text = "${folder.count} Videos",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        )
                    }

                    if (folder.totalSize.isNotBlank()) {
                        Text(
                            text = folder.totalSize,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderMagazineCard(
    folder: VideoFolder,
    issueNumber: Int,
    isArabic: Boolean,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    coverFilePath: String? = null,
    leadThumbnailImage: ImageBitmap? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val leadVideo = folder.videos.firstOrNull()
    val issueTag = issueNumber.toString().padStart(2, '0')

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.68f)
            .clip(RoundedCornerShape(22.dp))
            .background(aura.surfaceCard)
            .border(
                BorderStroke(
                    if (isSelected) 2.2.dp else 1.1.dp,
                    if (isSelected) {
                        Brush.linearGradient(listOf(aura.accentCyan, aura.accentCyan))
                    } else {
                        Brush.linearGradient(
                            listOf(
                                aura.accentGold.copy(alpha = 0.65f),
                                aura.accentCyan.copy(alpha = 0.55f),
                                aura.accentViolet.copy(alpha = 0.45f)
                            )
                        )
                    }
                ),
                RoundedCornerShape(22.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .testTag("folder_magazine_card_${folder.id}")
    ) {
        FolderCoverArtwork(
            folder = folder,
            coverFilePath = coverFilePath,
            leadVideo = leadVideo,
            leadThumbnailImage = leadThumbnailImage,
            aspectRatio = 1.68f
        )

        // Luxury editorial dark vignette scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x44000000),
                            Color(0x1C000000),
                            Color(0xCC070B14),
                            Color(0xF5050810)
                        )
                    )
                )
        )

        // Top editorial folder masthead row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0x990B101B),
                    border = BorderStroke(0.9.dp, aura.accentGold.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = aura.accentGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "مجلد #$issueTag" else "FOLDER #$issueTag",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }
                if (isPinned) {
                    PinnedBadgePill()
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xA60B101B),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = if (isArabic) "${folder.count} فيديو" else "${folder.count} Clips",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = aura.accentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                )
            }
        }

        // Bottom folder title & Open CTA
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    lineHeight = 22.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append(if (isArabic) "${folder.count} مقاطع" else "${folder.count} Videos")
                        if (folder.totalSize.isNotBlank()) {
                            append(" • ")
                            append(folder.totalSize)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = aura.accentCyan
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color(0xFF06101B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "فتح" else "OPEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF06101B),
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.7.sp,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderGalleryTileCard(
    folder: VideoFolder,
    isArabic: Boolean,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    coverFilePath: String? = null,
    leadThumbnailImage: ImageBitmap? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val leadVideo = folder.videos.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.88f)
            .clip(RoundedCornerShape(6.dp))
            .background(aura.surfaceCard)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, aura.accentCyan, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .testTag("folder_gallery_tile_${folder.id}")
    ) {
        FolderCoverArtwork(
            folder = folder,
            coverFilePath = coverFilePath,
            leadVideo = leadVideo,
            leadThumbnailImage = leadThumbnailImage,
            aspectRatio = 0.88f
        )

        if (isPinned) {
            PinnedBadgePill(
                compact = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
            )
        }

        // Top-end folder count pill
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xAA060A12),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${folder.count}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Bottom contrast gradient scrim for folder name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (folder.totalSize.isNotBlank()) {
                    folder.totalSize
                } else if (isArabic) {
                    "${folder.count} فيديو"
                } else {
                    "${folder.count} vids"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 9.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FolderCoverArtwork(
    folder: VideoFolder,
    coverFilePath: String?,
    leadVideo: LocalDeviceVideo?,
    leadThumbnailImage: ImageBitmap?,
    aspectRatio: Float
) {
    val context = LocalContext.current
    val coverFile = remember(coverFilePath) {
        coverFilePath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }
    }
    if (coverFile != null) {
        val request = remember(coverFile.absolutePath, coverFile.lastModified()) {
            ImageRequest.Builder(context)
                .data(coverFile)
                .crossfade(false)
                .build()
        }
        AsyncImage(
            model = request,
            contentDescription = folder.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, -0.25f),
            filterQuality = FilterQuality.High
        )
    } else if (leadVideo != null && leadThumbnailImage != null) {
        VideoPosterContent(
            video = leadVideo,
            aspectRatio = aspectRatio,
            thumbnailImage = leadThumbnailImage
        )
    } else if (leadVideo != null && leadVideo.bitmap != null) {
        VideoPosterContent(
            video = leadVideo,
            aspectRatio = aspectRatio,
            thumbnailImage = null
        )
    } else {
        SmartFallbackCover(
            title = "",
            subtitle = null,
            icon = Icons.Default.Folder,
            aspectRatio = aspectRatio,
            badgeText = null,
            gradientVariant = folder.id.hashCode(),
            showText = false
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItemCard(
    folder: VideoFolder,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    coverFilePath: String? = null,
    leadThumbnailImage: ImageBitmap? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val leadVideo = folder.videos.firstOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> aura.accentCyan
                    isPinned -> aura.accentGold.copy(alpha = 0.65f)
                    else -> aura.borderSubtle
                },
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .testTag("folder_card_${folder.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Validated HD online cover -> local high-res video frame -> SmartFallbackCover
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                FolderCoverArtwork(
                    folder = folder,
                    coverFilePath = coverFilePath,
                    leadVideo = leadVideo,
                    leadThumbnailImage = leadThumbnailImage,
                    aspectRatio = 1f
                )
                if (isPinned) {
                    PinnedBadgePill(
                        compact = true,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = aura.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPinned) {
                        PinnedBadgePill(compact = true)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = aura.accentCyanSoft
                    ) {
                        Text(
                            text = "${folder.count} Videos",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = folder.totalSize,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NewVideoBadgePill(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(if (compact) 4.dp else 6.dp),
        color = Color.Transparent,
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.45f)),
        modifier = modifier.testTag("video_new_badge")
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(aura.accentCyan, Color(0xFF00B4FF))
                    )
                )
                .padding(
                    horizontal = if (compact) 5.dp else 7.dp,
                    vertical = if (compact) 1.5.dp else 2.5.dp
                )
        ) {
            Text(
                text = "NEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextDark,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.7.sp,
                    fontSize = if (compact) 7.5.sp else 9.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItemCard(
    video: LocalDeviceVideo,
    onPlay: () -> Unit,
    thumbnailImage: ImageBitmap? = null,
    savedProgress: VideoProgressInfo? = null,
    isArabic: Boolean = false,
    hideFileExtensions: Boolean = false,
    isNewVideo: Boolean = false,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }
    val resolvedBitmap = thumbnailImage ?: remember(video.id, video.bitmap) {
        video.bitmap?.asImageBitmap()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> aura.accentCyan
                    isPinned -> aura.accentGold.copy(alpha = 0.65f)
                    else -> aura.borderSubtle
                },
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("file_list_card_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or Smart Fallback Cover
            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (resolvedBitmap != null) {
                    Image(
                        bitmap = resolvedBitmap,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High
                    )
                } else if (video.thumbnailRes != 0) {
                    Image(
                        painter = painterResource(id = video.thumbnailRes),
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    SmartFallbackCover(
                        title = video.title,
                        aspectRatio = 1.58f,
                        badgeText = video.resolutionText,
                        gradientVariant = video.id.hashCode()
                    )
                }

                if (episodeInfo != null) {
                    LuxuryEpisodeThumbnailBadge(
                        episodeInfo = episodeInfo,
                        isArabic = isArabic,
                        compact = true,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = video.durationText,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Persistent Progress Bar for this video
                if (savedProgress != null && savedProgress.progress > 0.02f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0x66000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(savedProgress.progress.coerceIn(0.02f, 1f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PulseCyan, Color(0xFF0088FF))
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                SmoothMarqueeTitleText(
                    text = formatCleanTitle(video.title, hideFileExtensions),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = aura.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPinned) {
                        PinnedBadgePill(compact = true)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (isNewVideo) {
                        NewVideoBadgePill(compact = true)
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = aura.accentCyanSoft
                    ) {
                        Text(
                            text = video.resolutionText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = video.sizeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(aura.primaryCtaBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = aura.primaryCtaContent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MagazineGridItemCard(
    video: LocalDeviceVideo,
    onPlay: () -> Unit,
    thumbnailImage: ImageBitmap? = null,
    savedProgress: VideoProgressInfo? = null,
    isArabic: Boolean = false,
    hideFileExtensions: Boolean = false,
    isNewVideo: Boolean = false,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }
    val resolvedBitmap = thumbnailImage ?: remember(video.id, video.bitmap) {
        video.bitmap?.asImageBitmap()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> aura.accentCyan
                    isPinned -> aura.accentGold.copy(alpha = 0.65f)
                    else -> aura.borderSubtle
                },
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("magazine_card_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
            ) {
                if (resolvedBitmap != null) {
                    Image(
                        bitmap = resolvedBitmap,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High
                    )
                } else if (video.thumbnailRes != 0) {
                    Image(
                        painter = painterResource(id = video.thumbnailRes),
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    SmartFallbackCover(
                        title = video.title,
                        aspectRatio = 1.5f,
                        badgeText = video.resolutionText,
                        gradientVariant = video.id.hashCode()
                    )
                }

                if (isPinned || isNewVideo) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPinned) {
                            PinnedBadgePill(compact = true)
                        }
                        if (isNewVideo) {
                            NewVideoBadgePill()
                        }
                    }
                }

                if (episodeInfo != null) {
                    LuxuryEpisodeThumbnailBadge(
                        episodeInfo = episodeInfo,
                        isArabic = isArabic,
                        compact = true,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    )
                }

                // Play icon pill in center
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = video.durationText,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Persistent Progress Bar for this video
                if (savedProgress != null && savedProgress.progress > 0.02f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0x66000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(savedProgress.progress.coerceIn(0.02f, 1f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PulseCyan, Color(0xFF0088FF))
                                    )
                                )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                SmoothMarqueeTitleText(
                    text = formatCleanTitle(video.title, hideFileExtensions),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = aura.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = aura.accentCyanSoft
                    ) {
                        Text(
                            text = video.resolutionText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    Text(
                        text = video.sizeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

private fun buildMagazineRows(videos: List<LocalDeviceVideo>): List<MagazineFeedRow> {
    if (videos.isEmpty()) return emptyList()
    val rows = mutableListOf<MagazineFeedRow>()
    var idx = 0
    var cycle = 0
    while (idx < videos.size) {
        when (cycle % 3) {
            0 -> {
                val vid = videos[idx]
                rows.add(
                    MagazineFeedRow.HeroSpread(
                        id = "mag_hero_${vid.id}_$idx",
                        video = vid,
                        issueNumber = idx + 1
                    )
                )
                idx += 1
            }
            1 -> {
                val left = videos[idx]
                val right = videos.getOrNull(idx + 1)
                rows.add(
                    MagazineFeedRow.DualPortraitSpread(
                        id = "mag_dual_${left.id}_$idx",
                        left = left,
                        leftIssue = idx + 1,
                        right = right,
                        rightIssue = if (right != null) idx + 2 else null
                    )
                )
                idx += if (right != null) 2 else 1
            }
            else -> {
                val vid = videos[idx]
                rows.add(
                    MagazineFeedRow.SpotlightStrip(
                        id = "mag_spot_${vid.id}_$idx",
                        video = vid,
                        issueNumber = idx + 1
                    )
                )
                idx += 1
            }
        }
        cycle += 1
    }
    return rows
}

private fun buildGalleryMosaicRows(videos: List<LocalDeviceVideo>): List<GalleryMosaicRow> {
    if (videos.isEmpty()) return emptyList()
    val rows = mutableListOf<GalleryMosaicRow>()
    var idx = 0
    var patternIndex = 0
    while (idx < videos.size) {
        val remaining = videos.size - idx
        val (count, weights, height) = when {
            remaining == 1 -> Triple(1, listOf(1f), 196.dp)
            remaining == 2 -> Triple(2, listOf(1f, 1f), 170.dp)
            else -> when (patternIndex % 4) {
                0 -> Triple(3, listOf(0.92f, 0.92f, 1.28f), 188.dp)
                1 -> Triple(3, listOf(1.28f, 0.92f, 0.92f), 188.dp)
                2 -> Triple(3, listOf(1f, 1f, 1f), 182.dp)
                else -> Triple(2, listOf(1f, 1f), 162.dp)
            }
        }
        val slice = videos.subList(idx, idx + count)
        rows.add(
            GalleryMosaicRow(
                id = "gal_row_${idx}_${slice.first().id}",
                videos = slice,
                weights = weights,
                height = height
            )
        )
        idx += count
        patternIndex += 1
    }
    return rows
}

@Composable
private fun VideoPosterContent(
    video: LocalDeviceVideo,
    aspectRatio: Float,
    thumbnailImage: ImageBitmap? = null,
    modifier: Modifier = Modifier
) {
    val resolvedBitmap = thumbnailImage ?: remember(video.id, video.bitmap) {
        video.bitmap?.asImageBitmap()
    }
    if (resolvedBitmap != null) {
        Image(
            bitmap = resolvedBitmap,
            contentDescription = video.title,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.High
        )
    } else if (video.thumbnailRes != 0) {
        Image(
            painter = painterResource(id = video.thumbnailRes),
            contentDescription = video.title,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        SmartFallbackCover(
            title = video.title,
            aspectRatio = aspectRatio,
            badgeText = null,
            gradientVariant = video.id.hashCode(),
            showText = false
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MagazineEditorialHeroCard(
    video: LocalDeviceVideo,
    issueNumber: Int,
    isArabic: Boolean,
    thumbnailImage: ImageBitmap? = null,
    savedProgress: VideoProgressInfo? = null,
    hideFileExtensions: Boolean = false,
    isNewVideo: Boolean = false,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    onPlay: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val issueTag = issueNumber.toString().padStart(2, '0')
    val folderKicker = video.folderName.ifBlank { "CINEMA" }.uppercase()
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.48f)
            .clip(RoundedCornerShape(24.dp))
            .background(aura.surfaceCard)
            .border(
                BorderStroke(
                    if (isSelected) 2.2.dp else 1.2.dp,
                    if (isSelected) {
                        Brush.linearGradient(listOf(aura.accentCyan, aura.accentCyan))
                    } else {
                        Brush.linearGradient(
                            listOf(
                                aura.accentGold.copy(alpha = 0.7f),
                                aura.accentCyan.copy(alpha = 0.55f),
                                aura.accentViolet.copy(alpha = 0.45f)
                            )
                        )
                    }
                ),
                RoundedCornerShape(24.dp)
            )
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("magazine_hero_card_${video.id}")
    ) {
        VideoPosterContent(video = video, aspectRatio = 1.48f, thumbnailImage = thumbnailImage)

        // Luxury editorial dark vignette scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x3D000000),
                            Color(0x18000000),
                            Color(0xC8070B14),
                            Color(0xF5050810)
                        )
                    )
                )
        )

        // Top editorial masthead row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0x990B101B),
                    border = BorderStroke(0.9.dp, aura.accentGold.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(aura.accentGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "العدد #$issueTag • رئيسي" else "ISSUE #$issueTag • FEATURED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }
                if (isPinned) {
                    PinnedBadgePill()
                }
                if (isNewVideo) {
                    NewVideoBadgePill()
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xA60B101B),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "${video.resolutionText} • ${video.durationText}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    )
                }
                if (episodeInfo != null) {
                    LuxuryEpisodeThumbnailBadge(
                        episodeInfo = episodeInfo,
                        isArabic = isArabic,
                        compact = false
                    )
                }
            }
        }

        // Bottom magazine headline & luxury Watch CTA
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "EDITION • $folderKicker",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = aura.accentGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    fontSize = 9.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            SmoothMarqueeTitleText(
                text = formatCleanTitle(video.title, hideFileExtensions),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.5.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${video.sizeText} • ${video.durationText}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = aura.accentCyan
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF06101B),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isArabic) "مشاهدة" else "WATCH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF06101B),
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.7.sp,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        if (savedProgress != null && savedProgress.progress > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0x55000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(savedProgress.progress.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(aura.accentGold, aura.accentCyan))
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MagazinePortraitCoverCard(
    video: LocalDeviceVideo,
    issueNumber: Int,
    isArabic: Boolean = false,
    thumbnailImage: ImageBitmap? = null,
    savedProgress: VideoProgressInfo? = null,
    hideFileExtensions: Boolean = false,
    isNewVideo: Boolean = false,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    onPlay: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val issueTag = issueNumber.toString().padStart(2, '0')
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(20.dp))
            .background(aura.surfaceCard)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> aura.accentCyan
                    isPinned -> aura.accentGold.copy(alpha = 0.65f)
                    else -> aura.borderSubtle
                },
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("magazine_portrait_card_${video.id}")
    ) {
        VideoPosterContent(video = video, aspectRatio = 0.78f, thumbnailImage = thumbnailImage)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x28000000),
                            Color.Transparent,
                            Color(0xB8070B14),
                            Color(0xF2050810)
                        )
                    )
                )
        )

        // Issue tag & frosted play corner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xA6080E1A),
                    border = BorderStroke(0.7.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "#$issueTag",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.accentCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.5.sp
                        )
                    )
                }
                if (isPinned) {
                    PinnedBadgePill(compact = true)
                }
                if (isNewVideo) {
                    NewVideoBadgePill(compact = true)
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .border(0.8.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (episodeInfo != null) {
                    LuxuryEpisodeThumbnailBadge(
                        episodeInfo = episodeInfo,
                        isArabic = isArabic,
                        compact = true
                    )
                }
                Text(
                    text = video.resolutionText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = aura.accentGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 9.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            SmoothMarqueeTitleText(
                text = formatCleanTitle(video.title, hideFileExtensions),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${video.durationText} • ${video.sizeText}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }

        if (savedProgress != null && savedProgress.progress > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0x55000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(savedProgress.progress.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .background(aura.accentCyan)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MagazineSpotlightCard(
    video: LocalDeviceVideo,
    issueNumber: Int,
    isArabic: Boolean,
    thumbnailImage: ImageBitmap? = null,
    savedProgress: VideoProgressInfo? = null,
    hideFileExtensions: Boolean = false,
    isNewVideo: Boolean = false,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    onPlay: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val aura = MaterialTheme.aura
    val issueTag = issueNumber.toString().padStart(2, '0')
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = aura.surfaceCard,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) {
                Brush.horizontalGradient(listOf(aura.accentCyan, aura.accentCyan))
            } else {
                Brush.horizontalGradient(
                    listOf(
                        if (isPinned) aura.accentGold.copy(alpha = 0.72f) else aura.accentViolet.copy(alpha = 0.55f),
                        aura.borderSubtle
                    )
                )
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("magazine_spotlight_card_${video.id}")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.46f)
                    .fillMaxHeight()
            ) {
                VideoPosterContent(video = video, aspectRatio = 1.1f, thumbnailImage = thumbnailImage)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0x66060911))
                            )
                        )
                )
                if (isPinned || isNewVideo) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPinned) {
                            PinnedBadgePill(compact = true)
                        }
                        if (isNewVideo) {
                            NewVideoBadgePill(compact = true)
                        }
                    }
                }
                if (episodeInfo != null) {
                    LuxuryEpisodeThumbnailBadge(
                        episodeInfo = episodeInfo,
                        isArabic = isArabic,
                        compact = true,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xB3000000),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.durationText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }

                if (savedProgress != null && savedProgress.progress > 0.02f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0x55000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(savedProgress.progress.coerceIn(0.02f, 1f))
                                .fillMaxHeight()
                                .background(aura.accentViolet)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.54f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مختارات #$issueTag" else "SPOTLIGHT #$issueTag",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.accentGold,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            fontSize = 9.5.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = aura.accentCyanSoft
                    ) {
                        Text(
                            text = video.resolutionText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp
                            )
                        )
                    }
                }

                SmoothMarqueeTitleText(
                    text = formatCleanTitle(video.title, hideFileExtensions),
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.sizeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 10.5.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(aura.primaryCtaBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = aura.primaryCtaContent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryMosaicTileCard(
    video: LocalDeviceVideo,
    isArabic: Boolean = false,
    thumbnailImage: ImageBitmap? = null,
    savedProgress: VideoProgressInfo? = null,
    isNewVideo: Boolean = false,
    isPinned: Boolean = false,
    isSelected: Boolean = false,
    onPlay: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }

    Box(
        modifier = modifier
            .background(aura.surfaceCard)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, aura.accentCyan)
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("gallery_mosaic_tile_${video.id}")
    ) {
        VideoPosterContent(video = video, aspectRatio = 0.85f, thumbnailImage = thumbnailImage)

        if (isPinned || isNewVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPinned) {
                    PinnedBadgePill(compact = true)
                }
                if (isNewVideo) {
                    NewVideoBadgePill(compact = true)
                }
            }
        }

        if (episodeInfo != null) {
            LuxuryEpisodeThumbnailBadge(
                episodeInfo = episodeInfo,
                isArabic = isArabic,
                compact = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
            )
        }

        // Subtle bottom contrast gradient so white play glyph & timestamp stay legible on bright scenes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.36f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x99000000))
                    )
                )
        )

        // Exact Gallery-style tiny white play triangle + compact duration at bottom-start
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = formatGalleryDuration(video.durationText),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }

        if (savedProgress != null && savedProgress.progress > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0x55000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(savedProgress.progress.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .background(PulseCyan)
                )
            }
        }
    }
}

private fun formatGalleryDuration(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.length >= 5 && trimmed.startsWith("0") && trimmed[1] != ':') {
        trimmed.substring(1)
    } else {
        trimmed
    }
}

private fun parseDurationSeconds(durationText: String): Long {
    val clean = durationText.trim()
    if (clean.isEmpty()) return 0L
    return try {
        val parts = clean.split(":").map { it.trim().toLong() }
        when (parts.size) {
            2 -> parts[0] * 60L + parts[1]
            3 -> parts[0] * 3600L + parts[1] * 60L + parts[2]
            else -> 0L
        }
    } catch (_: Exception) {
        0L
    }
}

private fun parseSizeBytes(sizeText: String): Long {
    val clean = sizeText.trim().uppercase()
    if (clean.isEmpty()) return 0L
    return try {
        val numberPart = clean.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return 0L
        when {
            clean.contains("GB") -> (numberPart * 1024.0 * 1024.0 * 1024.0).toLong()
            clean.contains("MB") -> (numberPart * 1024.0 * 1024.0).toLong()
            clean.contains("KB") -> (numberPart * 1024.0).toLong()
            else -> numberPart.toLong()
        }
    } catch (_: Exception) {
        0L
    }
}

private fun formatDurationMsForItem(millis: Long): String {
    return if (millis <= 0L) "--:--" else LocalMediaScanner.formatDuration(millis)
}

private fun calculateTotalFolderSize(videos: List<LocalDeviceVideo>): String {
    return if (videos.isEmpty()) "" else "${videos.size} vids"
}

private fun LocalDeviceVideo.toMediaItem(): MediaItem {
    return MediaItem(
        id = id,
        title = title,
        titleArabic = title,
        subtitleBadge = "OFFLINE MEDIA",
        episodesFreeBadge = resolutionText,
        year = "Local",
        seasonsOrDuration = durationText,
        maturityRating = "All",
        qualityTag = resolutionText,
        genresText = "Local Storage • $sizeText",
        description = "Offline video file ready for playback.",
        posterRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_poster,
        backdropRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_poster,
        uriString = uriString
    )
}
