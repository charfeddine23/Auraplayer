package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.R
import com.example.data.local.MediaPlaybackActivityBus
import com.example.data.model.LocalAudioTrack
import com.example.data.preferences.StreakPreferencesRepository
import com.example.data.preferences.UserAudioPlaylist
import com.example.data.util.LocalMediaScanner
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ListenScreen(
    isArabic: Boolean = false,
    onBack: () -> Unit = {},
    onFullPlayerVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember(context) { StreakPreferencesRepository.getInstance(context) }

    val initialTracks = remember {
        listOf(
            LocalAudioTrack(
                id = "track_1",
                title = "Cyber Pulse Symphony",
                artist = "Charfeddine Kahlessenane",
                album = "Neon Horizon 2026",
                durationSeconds = 245,
                albumCoverRes = R.drawable.img_poster_cyberpunk,
                lyricsLines = listOf(
                    "Midnight shadows dancing in the neon glow...",
                    "Drifting through the pulse of city lights below.",
                    "Electronic echoes whisper through the night,",
                    "Lost inside the frequency of liquid light.",
                    "Feel the heartbeat of the sound tonight."
                ),
                audioUri = null,
                isFromDevice = false
            ),
            LocalAudioTrack(
                id = "track_2",
                title = "Liquid Horizon Chill",
                artist = "Pulse Soundscapes",
                album = "Ambient Waves Vol. 1",
                durationSeconds = 198,
                albumCoverRes = R.drawable.img_hero_poster,
                lyricsLines = listOf(
                    "Breathe in the calm of endless seas...",
                    "Soft waves washing over memories.",
                    "Gentle breeze in deep dark blue,",
                    "Finding peace in every acoustic hue."
                ),
                audioUri = null,
                isFromDevice = false
            ),
            LocalAudioTrack(
                id = "track_3",
                title = "Odyssey of the Desert",
                artist = "Atlas Melodies",
                album = "Arabian Fusion Sessions",
                durationSeconds = 280,
                albumCoverRes = R.drawable.img_poster_action,
                lyricsLines = listOf(
                    "Golden sands beneath the starlit sky...",
                    "Echoes of an ancient melody flying high.",
                    "Strings that speak where words cannot find,",
                    "A journey of the spirit and of the mind."
                ),
                audioUri = null,
                isFromDevice = false
            )
        )
    }

    val playlist = remember { mutableStateListOf<LocalAudioTrack>().apply { addAll(initialTracks) } }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    val currentTrack = playlist.getOrElse(currentTrackIndex) { initialTracks[0] }

    // Persistent Library Telemetry & Collections
    var totalListenSeconds by remember { mutableLongStateOf(prefsRepo.getListenTotalSeconds()) }
    var totalPlaysCount by remember { mutableIntStateOf(prefsRepo.getListenTotalPlays()) }
    val memberSince = remember { prefsRepo.getListenMemberSince() }
    var trackPlaysMap by remember { mutableStateOf(prefsRepo.getListenTrackPlays()) }
    var historyIds by remember { mutableStateOf(prefsRepo.getListenHistoryIds()) }
    var favoriteIds by remember { mutableStateOf(prefsRepo.getListenFavoriteIds()) }
    var downloadedIds by remember { mutableStateOf(prefsRepo.getListenDownloadedIds()) }
    var userPlaylists by remember { mutableStateOf(prefsRepo.getUserAudioPlaylists()) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableFloatStateOf(0f) }
    var currentDurationMs by remember { mutableFloatStateOf(currentTrack.durationSeconds * 1000f) }
    var isDraggingSlider by remember { mutableStateOf(false) }

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
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var castDeviceName by remember { mutableStateOf(if (isArabic) "هذا الهاتف" else "This phone") }

    // Smart Sleep Timer
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var sleepTimerRemainingSeconds by remember { mutableIntStateOf(0) }

    // Equalizer & Spatial Audio
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var selectedEqualizerPreset by remember { mutableStateOf("Bass Boost") }
    val eqBands = remember { mutableStateListOf(6f, 3f, 0f, 2f, 5f) }
    var isSpatialAudioEnabled by remember { mutableStateOf(true) }
    var bassBoostPercent by remember { mutableIntStateOf(75) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    LaunchedEffect(sleepTimerRemainingSeconds, isPlaying) {
        if (isPlaying && sleepTimerRemainingSeconds > 0) {
            delay(1000)
            val next = sleepTimerRemainingSeconds - 1
            sleepTimerRemainingSeconds = next
            if (next in 1..5) {
                exoPlayer.volume = (next.toFloat() / 5f).coerceIn(0f, 1f)
            } else if (next == 0) {
                exoPlayer.pause()
                isPlaying = false
                exoPlayer.volume = 1f
                sleepTimerMinutes = 0
                snackbarMessage = if (isArabic) "انتهى مؤقت النوم وتم إيقاف التشغيل" else "Sleep timer finished. Playback stopped"
            }
        }
    }

    // Track real listening time in seconds
    LaunchedEffect(isPlaying) {
        var pendingSeconds = 0L
        while (isPlaying) {
            delay(1000L)
            pendingSeconds += 1L
            totalListenSeconds += 1L
            if (pendingSeconds >= 5L) {
                prefsRepo.addListenSeconds(pendingSeconds)
                pendingSeconds = 0L
            }
        }
        if (pendingSeconds > 0L) {
            prefsRepo.addListenSeconds(pendingSeconds)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                MediaPlaybackActivityBus.setAudioPlaying(playing)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > 0) {
                        currentDurationMs = dur.toFloat()
                    }
                } else if (state == Player.STATE_ENDED && sleepTimerMinutes == -1) {
                    exoPlayer.pause()
                    isPlaying = false
                    sleepTimerMinutes = 0
                    snackbarMessage = if (isArabic) "اكتمل المقطع وتوقف التشغيل حسب مؤقت النوم" else "Track ended. Sleep timer stopped playback"
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            MediaPlaybackActivityBus.setAudioPlaying(false)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    fun recordPlayTelemetry(track: LocalAudioTrack) {
        trackPlaysMap = prefsRepo.recordListenTrackPlay(track.id)
        historyIds = prefsRepo.getListenHistoryIds()
        totalPlaysCount = prefsRepo.getListenTotalPlays()
    }

    fun playTrack(index: Int) {
        if (index !in playlist.indices) return
        currentTrackIndex = index
        val track = playlist[index]
        currentPositionMs = 0f
        currentDurationMs = (track.durationSeconds * 1000).toFloat()
        recordPlayTelemetry(track)

        if (track.audioUri != null) {
            try {
                val mediaItem = ExoMediaItem.fromUri(Uri.parse(track.audioUri))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                isPlaying = true
            } catch (_: Exception) {
                snackbarMessage = if (isArabic) "تعذر تشغيل الملف الصوتي" else "Failed to load audio"
            }
        } else {
            exoPlayer.stop()
            isPlaying = true
        }
    }

    fun playTrackObject(track: LocalAudioTrack) {
        val existingIdx = playlist.indexOfFirst { it.id == track.id }
        if (existingIdx >= 0) {
            playTrack(existingIdx)
        } else {
            playlist.add(0, track)
            playTrack(0)
        }
    }

    fun toggleCurrentPlayPause() {
        if (currentTrack.audioUri != null) {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                isPlaying = false
            } else {
                if (exoPlayer.playbackState == Player.STATE_IDLE) {
                    playTrack(currentTrackIndex)
                } else {
                    exoPlayer.play()
                    isPlaying = true
                }
            }
        } else {
            if (!isPlaying && totalPlaysCount == 0) {
                recordPlayTelemetry(currentTrack)
            }
            isPlaying = !isPlaying
        }
    }

    fun scanDeviceAudios() {
        scope.launch(Dispatchers.IO) {
            val deviceAudios = LocalMediaScanner.scanDeviceAudios(context)
            withContext(Dispatchers.Main) {
                if (deviceAudios.isNotEmpty()) {
                    val existingIds = playlist.map { it.id }.toSet()
                    val newlyFound = deviceAudios.filter { it.id !in existingIds }
                    if (newlyFound.isNotEmpty()) {
                        playlist.addAll(0, newlyFound)
                    } else if (!playlist.any { it.isFromDevice }) {
                        playlist.clear()
                        playlist.addAll(deviceAudios)
                    }
                    snackbarMessage = if (isArabic) {
                        "تم العثور على ${deviceAudios.size} ملف صوتي من الهاتف"
                    } else {
                        "Found ${deviceAudios.size} device audio tracks"
                    }
                } else {
                    snackbarMessage = if (isArabic) {
                        "لم يتم العثور على ملفات صوتية في التخزين"
                    } else {
                        "No audio files found in storage"
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            scanDeviceAudios()
        } else {
            snackbarMessage = if (isArabic) "تم رفض إذن الوصول للملفات الصوتية" else "Audio storage permission denied"
        }
    }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            scope.launch(Dispatchers.IO) {
                val parsedTracks = uris.map { uri ->
                    LocalMediaScanner.parseUriToAudioTrack(context, uri)
                }
                withContext(Dispatchers.Main) {
                    val startIndex = playlist.size
                    playlist.addAll(parsedTracks)
                    playTrack(startIndex)
                    snackbarMessage = if (isArabic) {
                        "تم فتح ${parsedTracks.size} ملف صوتي بنجاح"
                    } else {
                        "Loaded ${parsedTracks.size} audio files"
                    }
                }
            }
        }
    }

    // Auto-scan device audio if permission is already granted
    LaunchedEffect(Unit) {
        if (LocalMediaScanner.hasStorageOrAudioPermission(context)) {
            val deviceAudios = withContext(Dispatchers.IO) {
                LocalMediaScanner.scanDeviceAudios(context)
            }
            if (deviceAudios.isNotEmpty()) {
                playlist.clear()
                playlist.addAll(deviceAudios)
            }
        }
    }

    // Track position loop
    LaunchedEffect(isPlaying, isDraggingSlider) {
        while (isPlaying && !isDraggingSlider) {
            delay(250)
            if (currentTrack.audioUri != null) {
                val pos = exoPlayer.currentPosition
                currentPositionMs = pos.toFloat()
                val dur = exoPlayer.duration
                if (dur > 0) currentDurationMs = dur.toFloat()
            } else {
                if (currentPositionMs < currentDurationMs) {
                    currentPositionMs += 250f
                } else {
                    val nextIdx = (currentTrackIndex + 1) % playlist.size
                    playTrack(nextIdx)
                }
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            delay(2500)
            snackbarMessage = null
        }
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
    val navBarBottomPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
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
            contentPadding = PaddingValues(bottom = 96.dp + navBarBottomPad)
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
                        playTrackObject(topTrack)
                        showFullNowPlaying = true
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
                    cachedCount = playlist.size,
                    onLocalMusicClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showLocalMusicScreen = true
                    },
                    onCachedClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeCollectionSheet = LibraryCollectionSheetType.CACHED_SONGS
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
                    onImportOrToolsClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        audioFilePicker.launch(arrayOf("audio/*"))
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
                currentTrackId = currentTrack.id,
                userPlaylists = userPlaylists,
                favoriteIds = favoriteIds,
                onBack = { showLocalMusicScreen = false },
                onPlayTrack = { track ->
                    playTrackObject(track)
                    showFullNowPlaying = true
                },
                onShufflePlay = { candidateTracks ->
                    if (candidateTracks.isNotEmpty()) {
                        val picked = candidateTracks.random()
                        playTrackObject(picked)
                        showFullNowPlaying = true
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

        // Floating Capsule Mini-Player anchored cleanly at the bottom edge (dock hidden on Listen)
        FloatingMiniPlayerCapsule(
            track = currentTrack,
            isPlaying = isPlaying,
            isArabic = isArabic,
            onOpenFullPlayer = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showFullNowPlaying = true
            },
            onPlayPauseClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                toggleCurrentPlayPause()
            },
            onNextClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val nextIndex = (currentTrackIndex + 1) % playlist.size
                playTrack(nextIndex)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 640.dp)
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
        )

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
                onSeekDrag = {
                    isDraggingSlider = true
                    currentPositionMs = it
                },
                onSeekFinish = {
                    isDraggingSlider = false
                    if (currentTrack.audioUri != null) {
                        exoPlayer.seekTo(currentPositionMs.toLong())
                    }
                },
                onPlayPause = { toggleCurrentPlayPause() },
                onSkipPrevious = {
                    val prevIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else playlist.size - 1
                    playTrack(prevIndex)
                },
                onSkipNext = {
                    val nextIndex = (currentTrackIndex + 1) % playlist.size
                    playTrack(nextIndex)
                },
                onToggleFavorite = {
                    favoriteIds = prefsRepo.toggleListenFavorite(currentTrack.id)
                    val added = favoriteIds.contains(currentTrack.id)
                    snackbarMessage = if (added) {
                        if (isArabic) "تمت الإضافة إلى المفضلة" else "Added to Favorites"
                    } else {
                        if (isArabic) "تمت الإزالة من المفضلة" else "Removed from Favorites"
                    }
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
                    snackbarMessage = if (isArabic) {
                        "الصوت يعمل عبر: $castDeviceName"
                    } else {
                        "Audio routed through: $castDeviceName"
                    }
                },
                onCastClick = {
                    snackbarMessage = if (isArabic) {
                        "جاري البحث عن أجهزة البث المتاحة..."
                    } else {
                        "Scanning for Cast devices..."
                    }
                }
            )
        }

        // Toast Feedback Banner
        if (snackbarMessage != null) {
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
                    text = snackbarMessage!!,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Collection Sheet (Downloads / Local Music / Cached / History / Favorites)
        val sheetType = activeCollectionSheet
        if (sheetType != null) {
            val collectionTracks = when (sheetType) {
                LibraryCollectionSheetType.DOWNLOADS -> downloadedTracks
                LibraryCollectionSheetType.LOCAL_MUSIC -> localTracks
                LibraryCollectionSheetType.CACHED_SONGS -> playlist.toList()
                LibraryCollectionSheetType.HISTORY -> historyTracks
                LibraryCollectionSheetType.FAVORITES -> favoriteTracks
            }
            LibraryTrackCollectionSheet(
                type = sheetType,
                isArabic = isArabic,
                tracks = collectionTracks,
                currentTrackId = currentTrack.id,
                isPlaying = isPlaying,
                downloadedIds = downloadedIds,
                onDismiss = { activeCollectionSheet = null },
                onPlayTrack = { track ->
                    playTrackObject(track)
                    activeCollectionSheet = null
                    showFullNowPlaying = true
                },
                onToggleDownload = { track ->
                    downloadedIds = prefsRepo.toggleListenDownloaded(track.id)
                    snackbarMessage = if (downloadedIds.contains(track.id)) {
                        if (isArabic) "تم الحفظ في التنزيلات" else "Saved to Downloads"
                    } else {
                        if (isArabic) "تمت الإزالة من التنزيلات" else "Removed from Downloads"
                    }
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
                    userPlaylists = updated
                    prefsRepo.saveUserAudioPlaylists(updated)
                    showCreatePlaylistDialog = false
                    snackbarMessage = if (isArabic) "تم إنشاء القائمة: $name" else "Created playlist: $name"
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
                    playTrackObject(track)
                    activeUserPlaylist = null
                    showFullNowPlaying = true
                },
                onDeletePlaylist = {
                    val updated = userPlaylists.filterNot { it.id == viewingPlaylist.id }
                    userPlaylists = updated
                    prefsRepo.saveUserAudioPlaylists(updated)
                    activeUserPlaylist = null
                    snackbarMessage = if (isArabic) "تم حذف القائمة" else "Playlist deleted"
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
                    downloadedIds = prefsRepo.toggleListenDownloaded(currentTrack.id)
                    snackbarMessage = if (downloadedIds.contains(currentTrack.id)) {
                        if (isArabic) "تم الحفظ في التنزيلات" else "Saved to Downloads"
                    } else {
                        if (isArabic) "تمت الإزالة من التنزيلات" else "Removed from Downloads"
                    }
                },
                onAddToPlaylistClick = { showCreatePlaylistDialog = true },
                onPlayNext = {
                    val nextIdx = (currentTrackIndex + 1) % playlist.size
                    snackbarMessage = if (isArabic) {
                        "التالي: ${playlist[nextIdx].title}"
                    } else {
                        "Up next: ${playlist[nextIdx].title}"
                    }
                },
                onOpenSleepTimer = { showSleepTimerSheet = true },
                onShowMessage = { msg -> snackbarMessage = msg }
            )
        }

        if (showSleepTimerSheet) {
            SleepTimerBottomSheet(
                isArabic = isArabic,
                sleepTimerMinutes = sleepTimerMinutes,
                sleepTimerRemainingSeconds = sleepTimerRemainingSeconds,
                onDismiss = { showSleepTimerSheet = false },
                onSelectMinutes = { minutes, label ->
                    sleepTimerMinutes = minutes
                    sleepTimerRemainingSeconds = if (minutes > 0) minutes * 60 else 0
                    showSleepTimerSheet = false
                    snackbarMessage = if (minutes > 0) {
                        if (isArabic) "تم ضبط مؤقت النوم على $label" else "Sleep timer set for $label"
                    } else if (minutes == -1) {
                        if (isArabic) "سيتوقف التشغيل تلقائياً بنهاية هذا المقطع" else "Playback will stop at track end"
                    } else {
                        if (isArabic) "تم إلغاء مؤقت النوم" else "Sleep timer disabled"
                    }
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
                },
                onToggleSpatialAudio = { isSpatialAudioEnabled = it },
                onBassBoostChange = { bassBoostPercent = it }
            )
        }
    }
}
