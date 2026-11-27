package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LocalAudioTrack
import com.example.data.preferences.UserAudioPlaylist
import com.example.data.util.LocalMediaScanner
import com.example.media.ListenPlaybackManager
import com.example.ui.components.LocalFloatingBottomBarPadding
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding

@Composable
fun ListenScreen(
    isArabic: Boolean = false,
    onBack: () -> Unit = {},
    onFullPlayerVisibilityChange: (Boolean) -> Unit = {},
    openFullPlayerTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val playbackManager = remember(context) { ListenPlaybackManager.getInstance(context) }

    val playlist by playbackManager.playlist.collectAsStateWithLifecycle()
    val currentTrackIndex by playbackManager.currentTrackIndex.collectAsStateWithLifecycle()
    val currentTrack = playlist.getOrElse(currentTrackIndex) { playbackManager.getCurrentTrack() }

    // Persistent Library Telemetry & Collections backed by ListenPlaybackManager
    val totalListenSeconds by playbackManager.totalListenSeconds.collectAsStateWithLifecycle()
    val totalPlaysCount by playbackManager.totalPlaysCount.collectAsStateWithLifecycle()
    val memberSince = playbackManager.memberSince
    val trackPlaysMap by playbackManager.trackPlaysMap.collectAsStateWithLifecycle()
    val historyIds by playbackManager.historyIds.collectAsStateWithLifecycle()
    val favoriteIds by playbackManager.favoriteIds.collectAsStateWithLifecycle()
    val downloadedIds by playbackManager.downloadedIds.collectAsStateWithLifecycle()
    val userPlaylists by playbackManager.userPlaylists.collectAsStateWithLifecycle()

    val isPlaying by playbackManager.isPlaying.collectAsStateWithLifecycle()
    val hasSelectedTrackForSession by playbackManager.hasSelectedTrackForSession.collectAsStateWithLifecycle()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsStateWithLifecycle()
    val currentDurationMs by playbackManager.currentDurationMs.collectAsStateWithLifecycle()

    // View & Modal States
    var showLocalMusicScreen by remember { mutableStateOf(false) }
    var showFullNowPlaying by remember { mutableStateOf(false) }
    var activeCollectionSheet by remember { mutableStateOf<LibraryCollectionSheetType?>(null) }
    var showProfileStudioSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var activeUserPlaylist by remember { mutableStateOf<UserAudioPlaylist?>(null) }
    var showLyricsDialog by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    val snackbarMessage by playbackManager.snackbarMessage.collectAsStateWithLifecycle()
    var castDeviceName by remember { mutableStateOf(if (isArabic) "هذا الهاتف" else "This phone") }

    // Smart Sleep Timer
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    val sleepTimerMinutes by playbackManager.sleepTimerMinutes.collectAsStateWithLifecycle()
    val sleepTimerRemainingSeconds by playbackManager.sleepTimerRemainingSeconds.collectAsStateWithLifecycle()

    // Equalizer & Spatial Audio
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var selectedEqualizerPreset by remember { mutableStateOf("Bass Boost") }
    val eqBands = remember { mutableStateListOf(6f, 3f, 0f, 2f, 5f) }
    var isSpatialAudioEnabled by remember { mutableStateOf(true) }
    var bassBoostPercent by remember { mutableIntStateOf(75) }

    var hasAskedNotificationPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* playback proceeds either way; granted enables full notification shade banner */ }

    fun ensureNotificationPermissionPrompted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasAskedNotificationPermission) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                hasAskedNotificationPermission = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(openFullPlayerTrigger) {
        if (openFullPlayerTrigger > 0) {
            showFullNowPlaying = true
        }
    }

    fun playTrack(index: Int) {
        ensureNotificationPermissionPrompted()
        playbackManager.playTrack(index, isArabic)
    }

    fun playTrackObject(track: LocalAudioTrack) {
        ensureNotificationPermissionPrompted()
        playbackManager.playTrackObject(track, isArabic)
    }

    fun toggleCurrentPlayPause() {
        if (!isPlaying) {
            ensureNotificationPermissionPrompted()
        }
        playbackManager.toggleCurrentPlayPause(isArabic)
    }

    fun handleTrackTap(
        track: LocalAudioTrack,
        onBeforeOpenFullPlayer: () -> Unit = {}
    ) {
        if (hasSelectedTrackForSession && currentTrack.id == track.id) {
            if (!isPlaying) {
                toggleCurrentPlayPause()
            }
            onBeforeOpenFullPlayer()
            showFullNowPlaying = true
        } else {
            playTrackObject(track)
        }
    }

    fun scanDeviceAudios() {
        playbackManager.scanDeviceAudios(isArabic)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            scanDeviceAudios()
        } else {
            playbackManager.showMessage(
                if (isArabic) "تم رفض إذن الوصول للملفات الصوتية" else "Audio storage permission denied"
            )
        }
    }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            ensureNotificationPermissionPrompted()
            playbackManager.appendAndPlayPickedTracks(uris, isArabic)
        }
    }

    // Auto-scan device audio if permission is already granted
    LaunchedEffect(Unit) {
        playbackManager.autoScanDeviceAudiosIfPermitted()
    }

    // Derived collections & stats
    val topEntry = trackPlaysMap.maxByOrNull { it.value }
    val topTrack = remember(topEntry, playlist.size, currentTrack.id) {
        playlist.firstOrNull { it.id == topEntry?.key } ?: currentTrack
    }
    val topTrackPlays = topEntry?.value ?: (if (totalPlaysCount > 0) 1 else 0)
    val minutesListened = (totalListenSeconds / 60L)
    val localTracks = playlist.filter { it.isFromDevice }
    val localDisplayTracks = if (localTracks.isNotEmpty()) localTracks else playlist.toList()
    val mostListenedLocalTrack = remember(trackPlaysMap, localDisplayTracks, topTrack) {
        val bestLocalId = trackPlaysMap
            .filterKeys { id -> localDisplayTracks.any { it.id == id } }
            .maxByOrNull { it.value }
            ?.key
        localDisplayTracks.firstOrNull { it.id == bestLocalId }
            ?: localDisplayTracks.firstOrNull()
            ?: topTrack
    }
    val downloadedTracks = playlist.filter { downloadedIds.contains(it.id) }
    val historyTracks = historyIds.mapNotNull { id -> playlist.firstOrNull { it.id == id } }
    val favoriteTracks = playlist.filter { favoriteIds.contains(it.id) }

    LaunchedEffect(showFullNowPlaying) {
        onFullPlayerVisibilityChange(showFullNowPlaying)
    }

    DisposableEffect(Unit) {
        onDispose { onFullPlayerVisibilityChange(false) }
    }

    BackHandler(enabled = showFullNowPlaying) {
        showFullNowPlaying = false
    }

    val aura = MaterialTheme.aura
    val bottomDockPadding = LocalFloatingBottomBarPadding.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("listen_screen")
    ) {
        AuraAtmosphericLayer()

        // Main Library Surface themed with active AuraTheme
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(bottom = bottomDockPadding + 92.dp)
        ) {
            item {
                Spacer(modifier = Modifier.stableStatusBarsPadding())
                LibraryTopUtilityBar(
                    isArabic = isArabic,
                    onBack = onBack,
                    onHistoryClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeCollectionSheet = LibraryCollectionSheetType.HISTORY
                    },
                    onProfileClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showProfileStudioSheet = true
                    }
                )
            }

            item {
                Text(
                    text = if (isArabic) "المكتبة" else "Library",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 35.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                ListeningExperienceCarousel(
                    isArabic = isArabic,
                    minutesListened = minutesListened,
                    totalPlays = totalPlaysCount,
                    memberSince = memberSince,
                    topTrack = topTrack,
                    topTrackPlays = topTrackPlays,
                    libraryTrackCount = playlist.size,
                    onTopSongClick = {
                        handleTrackTap(topTrack)
                    },
                    onStatsCardClick = {
                        showProfileStudioSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                LibraryMenuSection(
                    isArabic = isArabic,
                    localMusicCount = localTracks.size,
                    likedSongsCount = favoriteTracks.size,
                    onLocalMusicClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showLocalMusicScreen = true
                    },
                    onLikedSongsClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeCollectionSheet = LibraryCollectionSheetType.FAVORITES
                    }
                )
                Spacer(modifier = Modifier.height(22.dp))
            }

            item {
                LibraryPlaylistsSection(
                    isArabic = isArabic,
                    playlists = userPlaylists,
                    favoritesCount = favoriteTracks.size,
                    onNewPlaylistClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showCreatePlaylistDialog = true
                    },
                    onOpenFavoritesPlaylist = {
                        activeCollectionSheet = LibraryCollectionSheetType.FAVORITES
                    },
                    onOpenUserPlaylist = { selected ->
                        activeUserPlaylist = selected
                    }
                )
            }
        }

        // Full-Screen Local Music Hub
        AnimatedVisibility(
            visible = showLocalMusicScreen,
            enter = fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            LocalMusicScreen(
                isArabic = isArabic,
                tracks = localDisplayTracks,
                mostListenedTrack = mostListenedLocalTrack,
                currentTrackId = if (hasSelectedTrackForSession) currentTrack.id else "",
                userPlaylists = userPlaylists,
                favoriteIds = favoriteIds,
                onBack = { showLocalMusicScreen = false },
                onPlayTrack = { track ->
                    handleTrackTap(track)
                },
                onShufflePlay = { candidateTracks ->
                    if (candidateTracks.isNotEmpty()) {
                        val picked = candidateTracks.random()
                        playTrackObject(picked)
                    }
                },
                onOpenProfileStudio = { showProfileStudioSheet = true },
                onScanDeviceMusic = {
                    if (!LocalMediaScanner.hasStorageOrAudioPermission(context)) {
                        permissionLauncher.launch(LocalMediaScanner.getAudioPermissions())
                    } else {
                        scanDeviceAudios()
                    }
                },
                onOpenAudioFilePicker = { audioFilePicker.launch(arrayOf("audio/*")) },
                onOpenFavoritesPlaylist = {
                    activeCollectionSheet = LibraryCollectionSheetType.FAVORITES
                },
                onOpenUserPlaylist = { selected ->
                    activeUserPlaylist = selected
                }
            )
        }

        // Circular Mini-Player trigger anchored above the app's bottom navigation bar
        AnimatedVisibility(
            visible = !showFullNowPlaying,
            enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 640.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingMiniPlayerCircleButton(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    isArabic = isArabic,
                    onOpenFullPlayer = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showFullNowPlaying = true
                    },
                    modifier = Modifier.padding(
                        end = 20.dp,
                        bottom = bottomDockPadding + 14.dp
                    )
                )
            }
        }

        // Full-Screen Immersive Now Playing Overlay
        AnimatedVisibility(
            visible = showFullNowPlaying,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = 0.88f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialOffsetY = { it }
            ) + fadeIn(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                animationSpec = tween(
                    durationMillis = 260,
                    easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                ),
                targetOffsetY = { it }
            ) + fadeOut(
                animationSpec = tween(durationMillis = 190, easing = FastOutLinearInEasing)
            )
        ) {
            ListenNowPlayingOverlay(
                isArabic = isArabic,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                isFavorite = favoriteIds.contains(currentTrack.id),
                currentPositionMs = currentPositionMs,
                currentDurationMs = currentDurationMs,
                sleepTimerMinutes = sleepTimerMinutes,
                sleepTimerRemainingSeconds = sleepTimerRemainingSeconds,
                castDeviceName = castDeviceName,
                onCollapse = { showFullNowPlaying = false },
                onSeekDrag = { pos ->
                    playbackManager.setDraggingSlider(true, pos)
                },
                onSeekFinish = {
                    playbackManager.seekTo(currentPositionMs.toLong())
                },
                onPlayPause = { toggleCurrentPlayPause() },
                onSkipPrevious = {
                    ensureNotificationPermissionPrompted()
                    playbackManager.skipPrevious(isArabic)
                },
                onSkipNext = {
                    ensureNotificationPermissionPrompted()
                    playbackManager.skipNext(isArabic)
                },
                onToggleFavorite = {
                    playbackManager.toggleFavorite(currentTrack.id, isArabic)
                },
                onOpenEqualizer = { showEqualizerSheet = true },
                onOpenSleepTimer = { showSleepTimerSheet = true },
                onOpenFiles = {
                    if (!LocalMediaScanner.hasStorageOrAudioPermission(context)) {
                        permissionLauncher.launch(LocalMediaScanner.getAudioPermissions())
                    } else {
                        audioFilePicker.launch(arrayOf("audio/*"))
                    }
                },
                onOpenLyrics = { showLyricsDialog = true },
                onOpenOptions = { showOptionsSheet = true },
                onOpenQueue = { showQueueSheet = true },
                onDeviceClick = {
                    playbackManager.showMessage(
                        if (isArabic) "الصوت يعمل عبر: $castDeviceName"
                        else "Audio routed through: $castDeviceName"
                    )
                },
                onCastClick = {
                    playbackManager.showMessage(
                        if (isArabic) "جاري البحث عن أجهزة البث المتاحة..."
                        else "Scanning for Cast devices..."
                    )
                }
            )
        }

        // Toast Feedback Banner
        val toastText = snackbarMessage
        if (toastText != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = aura.dialogBg,
                border = BorderStroke(1.dp, aura.borderSubtle),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .stableStatusBarsPadding()
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp)
            ) {
                Text(
                    text = toastText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Collection Sheet (Downloads / Local Music / History / Liked Songs)
        val sheetType = activeCollectionSheet
        if (sheetType != null) {
            val collectionTracks = when (sheetType) {
                LibraryCollectionSheetType.DOWNLOADS -> downloadedTracks
                LibraryCollectionSheetType.LOCAL_MUSIC -> localTracks
                LibraryCollectionSheetType.HISTORY -> historyTracks
                LibraryCollectionSheetType.FAVORITES -> favoriteTracks
            }
            LibraryTrackCollectionSheet(
                type = sheetType,
                isArabic = isArabic,
                tracks = collectionTracks,
                currentTrackId = if (hasSelectedTrackForSession) currentTrack.id else "",
                isPlaying = isPlaying,
                downloadedIds = downloadedIds,
                onDismiss = { activeCollectionSheet = null },
                onPlayTrack = { track ->
                    handleTrackTap(track) {
                        activeCollectionSheet = null
                    }
                },
                onToggleDownload = { track ->
                    playbackManager.toggleDownloaded(track.id, isArabic)
                },
                onScanDeviceMusic = {
                    if (!LocalMediaScanner.hasStorageOrAudioPermission(context)) {
                        permissionLauncher.launch(LocalMediaScanner.getAudioPermissions())
                    } else {
                        scanDeviceAudios()
                    }
                },
                onOpenAudioFilePicker = {
                    audioFilePicker.launch(arrayOf("audio/*"))
                }
            )
        }

        // Listener Profile & Studio Sheet
        if (showProfileStudioSheet) {
            LibraryProfileAndStudioSheet(
                isArabic = isArabic,
                minutesListened = minutesListened,
                totalPlays = totalPlaysCount,
                memberSince = memberSince,
                playlistsCount = userPlaylists.size,
                onDismiss = { showProfileStudioSheet = false },
                onOpenEqualizer = { showEqualizerSheet = true },
                onOpenSleepTimer = { showSleepTimerSheet = true },
                onOpenFiles = { audioFilePicker.launch(arrayOf("audio/*")) },
                onScanDeviceMusic = {
                    if (!LocalMediaScanner.hasStorageOrAudioPermission(context)) {
                        permissionLauncher.launch(LocalMediaScanner.getAudioPermissions())
                    } else {
                        scanDeviceAudios()
                    }
                }
            )
        }

        // Create New Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreateAudioPlaylistDialog(
                isArabic = isArabic,
                availableTracks = playlist.toList(),
                onDismiss = { showCreatePlaylistDialog = false },
                onCreate = { name, trackIds ->
                    val newPlaylist = UserAudioPlaylist(
                        id = "pl_${System.currentTimeMillis()}",
                        name = name,
                        trackIds = trackIds
                    )
                    val updated = listOf(newPlaylist) + userPlaylists
                    playbackManager.saveUserPlaylists(updated)
                    showCreatePlaylistDialog = false
                    playbackManager.showMessage(
                        if (isArabic) "تم إنشاء القائمة: $name" else "Created playlist: $name"
                    )
                }
            )
        }

        // Selected Playlist Detail Sheet
        val viewingPlaylist = activeUserPlaylist
        if (viewingPlaylist != null) {
            val resolvedTracks = viewingPlaylist.trackIds.mapNotNull { tid ->
                playlist.firstOrNull { it.id == tid }
            }
            UserPlaylistDetailSheet(
                playlist = viewingPlaylist,
                tracks = resolvedTracks,
                isArabic = isArabic,
                onDismiss = { activeUserPlaylist = null },
                onPlayTrack = { track ->
                    handleTrackTap(track) {
                        activeUserPlaylist = null
                    }
                },
                onDeletePlaylist = {
                    val updated = userPlaylists.filterNot { it.id == viewingPlaylist.id }
                    playbackManager.saveUserPlaylists(updated)
                    activeUserPlaylist = null
                    playbackManager.showMessage(
                        if (isArabic) "تم حذف القائمة" else "Playlist deleted"
                    )
                }
            )
        }

        if (showQueueSheet) {
            PlayQueueBottomSheet(
                isArabic = isArabic,
                playlist = playlist.toList(),
                currentTrackIndex = currentTrackIndex,
                isPlaying = isPlaying,
                onDismiss = { showQueueSheet = false },
                onSelectTrackIndex = { idx ->
                    showQueueSheet = false
                    playTrack(idx)
                }
            )
        }

        if (showLyricsDialog) {
            LyricsBottomSheet(
                track = currentTrack,
                onDismiss = { showLyricsDialog = false }
            )
        }

        if (showOptionsSheet) {
            SongOptionsBottomSheet(
                isArabic = isArabic,
                track = currentTrack,
                isDownloaded = downloadedIds.contains(currentTrack.id),
                onDismiss = { showOptionsSheet = false },
                onScanDeviceMusic = {
                    if (!LocalMediaScanner.hasStorageOrAudioPermission(context)) {
                        permissionLauncher.launch(LocalMediaScanner.getAudioPermissions())
                    } else {
                        scanDeviceAudios()
                    }
                },
                onToggleDownload = {
                    playbackManager.toggleDownloaded(currentTrack.id, isArabic)
                },
                onAddToPlaylistClick = { showCreatePlaylistDialog = true },
                onPlayNext = {
                    val nextIdx = (currentTrackIndex + 1) % playlist.size
                    playbackManager.showMessage(
                        if (isArabic) "التالي: ${playlist[nextIdx].title}"
                        else "Up next: ${playlist[nextIdx].title}"
                    )
                },
                onOpenSleepTimer = { showSleepTimerSheet = true },
                onShowMessage = { msg -> playbackManager.showMessage(msg) }
            )
        }

        if (showSleepTimerSheet) {
            SleepTimerBottomSheet(
                isArabic = isArabic,
                sleepTimerMinutes = sleepTimerMinutes,
                sleepTimerRemainingSeconds = sleepTimerRemainingSeconds,
                onDismiss = { showSleepTimerSheet = false },
                onSelectMinutes = { minutes, label ->
                    showSleepTimerSheet = false
                    playbackManager.setSleepTimer(minutes, label, isArabic)
                }
            )
        }

        if (showEqualizerSheet) {
            EqualizerBottomSheet(
                isArabic = isArabic,
                selectedPreset = selectedEqualizerPreset,
                eqBands = eqBands,
                isSpatialAudioEnabled = isSpatialAudioEnabled,
                bassBoostPercent = bassBoostPercent,
                onDismiss = { showEqualizerSheet = false },
                onSelectPreset = { preset ->
                    selectedEqualizerPreset = preset
                    when (preset) {
                        "Bass Boost" -> {
                            eqBands[0] = 8f; eqBands[1] = 5f; eqBands[2] = 0f; eqBands[3] = 2f; eqBands[4] = 4f
                            bassBoostPercent = 85
                        }
                        "Vocal" -> {
                            eqBands[0] = -2f; eqBands[1] = 2f; eqBands[2] = 7f; eqBands[3] = 4f; eqBands[4] = 1f
                            bassBoostPercent = 30
                        }
                        "Flat" -> {
                            eqBands[0] = 0f; eqBands[1] = 0f; eqBands[2] = 0f; eqBands[3] = 0f; eqBands[4] = 0f
                            bassBoostPercent = 50
                        }
                        "Electronic" -> {
                            eqBands[0] = 6f; eqBands[1] = 4f; eqBands[2] = -1f; eqBands[3] = 3f; eqBands[4] = 7f
                            bassBoostPercent = 80
                        }
                    }
                    playbackManager.updateAudioEffects(eqBands, bassBoostPercent)
                },
                onToggleSpatialAudio = { isSpatialAudioEnabled = it },
                onBassBoostChange = {
                    bassBoostPercent = it
                    playbackManager.updateAudioEffects(eqBands, it)
                }
            )
        }
    }
}
