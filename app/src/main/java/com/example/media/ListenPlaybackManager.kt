package com.example.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.R
import com.example.data.local.MediaPlaybackActivityBus
import com.example.data.model.LocalAudioTrack
import com.example.data.preferences.StreakPreferencesRepository
import com.example.data.preferences.UserAudioPlaylist
import com.example.data.util.LocalMediaScanner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

/**
 * Application-scoped music playback manager backed by Media3 [MediaController]
 * connected to [MusicPlaybackService]. Keeps playback, queue, sleep timer, and
 * listening telemetry alive across tab switches and backgrounding.
 */
class ListenPlaybackManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefsRepo = StreakPreferencesRepository.getInstance(appContext)

    private val starterTracks = listOf(
        LocalAudioTrack(
            id = "track_1",
            title = "Cyber Pulse Symphony",
            artist = "Charfeddine Kahlessenane",
            album = "Neon Horizon 2026",
            durationSeconds = 24,
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
            durationSeconds = 24,
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
            durationSeconds = 24,
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

    private val _playlist = MutableStateFlow(starterTracks)
    val playlist: StateFlow<List<LocalAudioTrack>> = _playlist.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _hasSelectedTrackForSession = MutableStateFlow(false)
    val hasSelectedTrackForSession: StateFlow<Boolean> = _hasSelectedTrackForSession.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0f)
    val currentPositionMs: StateFlow<Float> = _currentPositionMs.asStateFlow()

    private val _currentDurationMs = MutableStateFlow(starterTracks[0].durationSeconds * 1000f)
    val currentDurationMs: StateFlow<Float> = _currentDurationMs.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow(0)
    val sleepTimerRemainingSeconds: StateFlow<Int> = _sleepTimerRemainingSeconds.asStateFlow()

    private val _totalListenSeconds = MutableStateFlow(prefsRepo.getListenTotalSeconds())
    val totalListenSeconds: StateFlow<Long> = _totalListenSeconds.asStateFlow()

    private val _totalPlaysCount = MutableStateFlow(prefsRepo.getListenTotalPlays())
    val totalPlaysCount: StateFlow<Int> = _totalPlaysCount.asStateFlow()

    val memberSince: String = prefsRepo.getListenMemberSince()

    private val _trackPlaysMap = MutableStateFlow(prefsRepo.getListenTrackPlays())
    val trackPlaysMap: StateFlow<Map<String, Int>> = _trackPlaysMap.asStateFlow()

    private val _historyIds = MutableStateFlow(prefsRepo.getListenHistoryIds())
    val historyIds: StateFlow<List<String>> = _historyIds.asStateFlow()

    private val _favoriteIds = MutableStateFlow(prefsRepo.getListenFavoriteIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _downloadedIds = MutableStateFlow(prefsRepo.getListenDownloadedIds())
    val downloadedIds: StateFlow<Set<String>> = _downloadedIds.asStateFlow()

    private val _userPlaylists = MutableStateFlow(prefsRepo.getUserAudioPlaylists())
    val userPlaylists: StateFlow<List<UserAudioPlaylist>> = _userPlaylists.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val artworkBytesCache = object : LruCache<String, ByteArray>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var isDraggingSlider = false
    private var hasAutoScannedOnInit = false
    private var playbackJob: Job? = null
    private var positionTickerJob: Job? = null
    private var listenSecondsJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var snackbarJob: Job? = null
    private var suppressNextAutoTransitionTelemetry = false

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
            MediaPlaybackActivityBus.setAudioPlaying(playing)
            if (playing) {
                startActiveLoopers()
            } else {
                stopActiveLoopers()
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            val ctrl = mediaController ?: return
            if (state == Player.STATE_READY) {
                val dur = ctrl.duration
                if (dur > 0L) {
                    _currentDurationMs.value = dur.toFloat()
                }
            } else if (state == Player.STATE_ENDED && _sleepTimerMinutes.value == -1) {
                ctrl.pause()
                ctrl.volume = 1f
                _isPlaying.value = false
                _sleepTimerMinutes.value = 0
                _sleepTimerRemainingSeconds.value = 0
                showMessage("Track ended. Sleep timer stopped playback")
            }
        }

        override fun onMediaItemTransition(mediaItem: ExoMediaItem?, reason: Int) {
            val ctrl = mediaController ?: return
            val list = _playlist.value
            val matchedIndex = mediaItem?.mediaId?.let { id ->
                list.indexOfFirst { it.id == id }
            }?.takeIf { it >= 0 } ?: ctrl.currentMediaItemIndex.coerceIn(0, (list.size - 1).coerceAtLeast(0))

            if (matchedIndex in list.indices) {
                val previousIndex = _currentTrackIndex.value
                _currentTrackIndex.value = matchedIndex
                val track = list[matchedIndex]
                _currentPositionMs.value = ctrl.currentPosition.coerceAtLeast(0L).toFloat()
                val dur = ctrl.duration
                _currentDurationMs.value = if (dur > 0L) dur.toFloat() else track.durationSeconds * 1000f

                if (_sleepTimerMinutes.value == -1 && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    ctrl.pause()
                    ctrl.seekTo(0L)
                    ctrl.volume = 1f
                    _isPlaying.value = false
                    _sleepTimerMinutes.value = 0
                    _sleepTimerRemainingSeconds.value = 0
                    showMessage("Track ended. Sleep timer stopped playback")
                    return
                }

                if (suppressNextAutoTransitionTelemetry) {
                    suppressNextAutoTransitionTelemetry = false
                } else if (matchedIndex != previousIndex || reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    recordPlayTelemetry(track)
                }

                preloadAdjacentArtwork(matchedIndex)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _isPlaying.value = false
            MediaPlaybackActivityBus.setAudioPlaying(false)
            showMessage("Unable to play selected audio track")
        }
    }

    init {
        connectControllerAsync()
    }

    private fun connectControllerAsync() {
        val existing = controllerFuture
        if (existing != null && !existing.isCancelled && !existing.isDone) return

        val token = SessionToken(appContext, ComponentName(appContext, MusicPlaybackService::class.java))
        val future = MediaController.Builder(appContext, token)
            .setListener(object : MediaController.Listener {
                override fun onDisconnected(controller: MediaController) {
                    mediaController = null
                    controllerFuture = null
                    _isPlaying.value = false
                    MediaPlaybackActivityBus.setAudioPlaying(false)
                }
            })
            .buildAsync()
        controllerFuture = future

        future.addListener(
            {
                runCatching {
                    val controller = future.get()
                    mediaController?.removeListener(playerListener)
                    mediaController = controller
                    controller.addListener(playerListener)
                    syncStateFromController(controller)
                }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    private suspend fun ensureController(): MediaController? {
        mediaController?.let { if (it.isConnected) return it }
        connectControllerAsync()
        val future = controllerFuture ?: return null
        return suspendCancellableCoroutine { cont ->
            future.addListener(
                {
                    val ctrl = runCatching { future.get() }.getOrNull()
                    if (ctrl != null && mediaController !== ctrl) {
                        mediaController?.removeListener(playerListener)
                        mediaController = ctrl
                        ctrl.addListener(playerListener)
                    }
                    if (cont.isActive) cont.resume(ctrl)
                },
                ContextCompat.getMainExecutor(appContext)
            )
        }
    }

    private fun syncStateFromController(controller: MediaController) {
        val list = _playlist.value
        if (controller.mediaItemCount > 0) {
            val activeId = controller.currentMediaItem?.mediaId
            val idx = activeId?.let { id -> list.indexOfFirst { it.id == id } }?.takeIf { it >= 0 }
                ?: controller.currentMediaItemIndex.coerceIn(0, (list.size - 1).coerceAtLeast(0))
            _currentTrackIndex.value = idx
            _hasSelectedTrackForSession.value = true
            _isPlaying.value = controller.isPlaying
            _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L).toFloat()
            val dur = controller.duration
            if (dur > 0L) {
                _currentDurationMs.value = dur.toFloat()
            }
            MediaPlaybackActivityBus.setAudioPlaying(controller.isPlaying)
            if (controller.isPlaying) {
                startActiveLoopers()
            }
        }
    }

    private fun startActiveLoopers() {
        if (positionTickerJob?.isActive != true) {
            positionTickerJob = scope.launch {
                while (isActive && _isPlaying.value) {
                    if (!isDraggingSlider) {
                        val ctrl = mediaController
                        if (ctrl != null && ctrl.isConnected) {
                            _currentPositionMs.value = ctrl.currentPosition.coerceAtLeast(0L).toFloat()
                            val dur = ctrl.duration
                            if (dur > 0L) {
                                _currentDurationMs.value = dur.toFloat()
                            }
                        }
                    }
                    delay(250L)
                }
            }
        }

        if (listenSecondsJob?.isActive != true) {
            listenSecondsJob = scope.launch {
                var pendingSeconds = 0L
                try {
                    while (isActive && _isPlaying.value) {
                        delay(1000L)
                        pendingSeconds += 1L
                        _totalListenSeconds.value += 1L
                        if (pendingSeconds >= 5L) {
                            prefsRepo.addListenSeconds(pendingSeconds)
                            pendingSeconds = 0L
                        }
                    }
                } finally {
                    if (pendingSeconds > 0L) {
                        prefsRepo.addListenSeconds(pendingSeconds)
                    }
                }
            }
        }
    }

    private fun stopActiveLoopers() {
        positionTickerJob?.cancel()
        positionTickerJob = null
        listenSecondsJob?.cancel()
        listenSecondsJob = null
    }

    fun getCurrentTrack(): LocalAudioTrack {
        val list = _playlist.value
        return list.getOrElse(_currentTrackIndex.value) { starterTracks[0] }
    }

    fun autoScanDeviceAudiosIfPermitted() {
        if (hasAutoScannedOnInit) return
        if (!LocalMediaScanner.hasStorageOrAudioPermission(appContext)) return
        hasAutoScannedOnInit = true
        scope.launch {
            val deviceAudios = withContext(Dispatchers.IO) {
                LocalMediaScanner.scanDeviceAudios(appContext)
            }
            if (deviceAudios.isNotEmpty() && !_hasSelectedTrackForSession.value) {
                _playlist.value = deviceAudios
                _currentTrackIndex.value = 0
                _currentDurationMs.value = deviceAudios[0].durationSeconds * 1000f
            }
        }
    }

    fun scanDeviceAudios(isArabic: Boolean) {
        scope.launch {
            val deviceAudios = withContext(Dispatchers.IO) {
                LocalMediaScanner.scanDeviceAudios(appContext)
            }
            if (deviceAudios.isNotEmpty()) {
                val current = _playlist.value
                val existingIds = current.map { it.id }.toSet()
                val newlyFound = deviceAudios.filter { it.id !in existingIds }
                val activeTrack = getCurrentTrack()
                val updatedList = when {
                    !current.any { it.isFromDevice } && !_hasSelectedTrackForSession.value -> deviceAudios
                    newlyFound.isNotEmpty() -> newlyFound + current
                    else -> current
                }
                _playlist.value = updatedList
                val updatedIdx = updatedList.indexOfFirst { it.id == activeTrack.id }
                if (updatedIdx >= 0) {
                    _currentTrackIndex.value = updatedIdx
                }
                showMessage(
                    if (isArabic) "تم العثور على ${deviceAudios.size} ملف صوتي من الهاتف"
                    else "Found ${deviceAudios.size} device audio tracks"
                )
            } else {
                showMessage(
                    if (isArabic) "لم يتم العثور على ملفات صوتية في التخزين"
                    else "No audio files found in storage"
                )
            }
        }
    }

    fun appendAndPlayPickedTracks(uris: List<Uri>, isArabic: Boolean) {
        if (uris.isEmpty()) return
        scope.launch {
            val parsedTracks = withContext(Dispatchers.IO) {
                uris.map { uri -> LocalMediaScanner.parseUriToAudioTrack(appContext, uri) }
            }
            val current = _playlist.value
            val startIndex = current.size
            _playlist.value = current + parsedTracks
            playTrack(startIndex, isArabic)
            showMessage(
                if (isArabic) "تم فتح ${parsedTracks.size} ملف صوتي بنجاح"
                else "Loaded ${parsedTracks.size} audio files"
            )
        }
    }

    fun playTrack(index: Int, isArabic: Boolean = false) {
        val list = _playlist.value
        if (index !in list.indices) return
        val track = list[index]
        _hasSelectedTrackForSession.value = true
        _currentTrackIndex.value = index
        _currentPositionMs.value = 0f
        _currentDurationMs.value = track.durationSeconds * 1000f
        recordPlayTelemetry(track)

        playbackJob?.cancel()
        playbackJob = scope.launch {
            val controller = ensureController()
            if (controller == null) {
                showMessage(
                    if (isArabic) "تعذر تشغيل خدمة الموسيقى في الخلفية"
                    else "Unable to connect to background music service"
                )
                return@launch
            }

            // Ensure volume is restored if previously faded by sleep timer
            if (controller.volume < 0.99f && _sleepTimerRemainingSeconds.value <= 0) {
                controller.volume = 1f
            }

            // Fast path: if the controller queue already matches our playlist, jump cleanly
            val currentList = _playlist.value
            val queueMatches = controller.mediaItemCount == currentList.size &&
                index < controller.mediaItemCount &&
                controller.getMediaItemAt(index).mediaId == track.id

            val targetArtBytes = withContext(Dispatchers.IO) {
                resolveTrackArtworkBytes(track)
            }

            if (queueMatches) {
                suppressNextAutoTransitionTelemetry = true
                if (targetArtBytes != null && index != controller.currentMediaItemIndex) {
                    val uri = resolvePlayableTrackUri(track)
                    controller.replaceMediaItem(index, buildMediaItem(track, uri, targetArtBytes))
                }
                controller.seekToDefaultPosition(index)
                controller.prepare()
                controller.play()
                preloadAdjacentArtwork(index)
                return@launch
            }

            val mediaItems = withContext(Dispatchers.IO) {
                currentList.mapIndexed { idx, item ->
                    val uri = resolvePlayableTrackUri(item)
                    val art = if (idx == index) targetArtBytes else artworkBytesCache.get(item.id)
                    buildMediaItem(item, uri, art)
                }
            }

            suppressNextAutoTransitionTelemetry = true
            controller.setMediaItems(mediaItems, index, 0L)
            controller.prepare()
            controller.play()
            preloadAdjacentArtwork(index)
        }
    }

    fun playTrackObject(track: LocalAudioTrack, isArabic: Boolean = false) {
        val current = _playlist.value
        val existingIdx = current.indexOfFirst { it.id == track.id }
        if (existingIdx >= 0) {
            playTrack(existingIdx, isArabic)
        } else {
            _playlist.value = listOf(track) + current
            playTrack(0, isArabic)
        }
    }

    fun toggleCurrentPlayPause(isArabic: Boolean = false) {
        val controller = mediaController
        if (controller != null && controller.isConnected && controller.mediaItemCount > 0) {
            if (controller.isPlaying) {
                controller.pause()
            } else {
                _hasSelectedTrackForSession.value = true
                if (controller.playbackState == Player.STATE_IDLE) {
                    playTrack(_currentTrackIndex.value, isArabic)
                } else {
                    controller.play()
                }
            }
        } else {
            playTrack(_currentTrackIndex.value, isArabic)
        }
    }

    fun skipNext(isArabic: Boolean = false) {
        val list = _playlist.value
        if (list.isEmpty()) return
        val controller = mediaController
        if (controller != null && controller.isConnected && controller.mediaItemCount == list.size && controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            if (!controller.isPlaying) {
                controller.play()
            }
        } else {
            val nextIndex = (_currentTrackIndex.value + 1) % list.size
            playTrack(nextIndex, isArabic)
        }
    }

    fun skipPrevious(isArabic: Boolean = false) {
        val list = _playlist.value
        if (list.isEmpty()) return
        val controller = mediaController
        if (controller != null && controller.isConnected && controller.mediaItemCount == list.size && controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            if (!controller.isPlaying) {
                controller.play()
            }
        } else {
            val prevIndex = if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1 else list.lastIndex
            playTrack(prevIndex, isArabic)
        }
    }

    fun queueTrackNext(track: LocalAudioTrack, isArabic: Boolean = false) {
        val list = _playlist.value
        if (list.isEmpty()) return
        val currentIdx = _currentTrackIndex.value.coerceIn(0, list.lastIndex)
        val currentTrack = list[currentIdx]

        // If triggered on the currently playing track from Now Playing options, advance immediately to the next track
        if (track.id == currentTrack.id) {
            skipNext(isArabic)
            val nextIdx = if (list.size > 1) (currentIdx + 1) % list.size else currentIdx
            val nextTitle = _playlist.value.getOrNull(nextIdx)?.title ?: track.title
            showMessage(
                if (isArabic) "جاري تشغيل التالي: $nextTitle" else "Playing next: $nextTitle"
            )
            return
        }

        // Otherwise move or insert the chosen track directly after the current track in the queue
        val targetInsertIdx = (currentIdx + 1).coerceAtMost(list.size)
        val existingIdx = list.indexOfFirst { it.id == track.id }
        val mutable = list.toMutableList()
        var adjustedCurrentIdx = currentIdx
        val finalInsertIdx: Int
        if (existingIdx >= 0) {
            if (existingIdx == targetInsertIdx) {
                showMessage(if (isArabic) "التالي: ${track.title}" else "Up next: ${track.title}")
                return
            }
            val removed = mutable.removeAt(existingIdx)
            if (existingIdx < currentIdx) {
                adjustedCurrentIdx = (currentIdx - 1).coerceAtLeast(0)
            }
            finalInsertIdx = (adjustedCurrentIdx + 1).coerceAtMost(mutable.size)
            mutable.add(finalInsertIdx, removed)
        } else {
            finalInsertIdx = targetInsertIdx
            mutable.add(finalInsertIdx, track)
        }
        _playlist.value = mutable
        _currentTrackIndex.value = adjustedCurrentIdx

        val controller = mediaController
        if (controller != null && controller.isConnected && controller.mediaItemCount > 0) {
            if (existingIdx >= 0 && existingIdx < controller.mediaItemCount) {
                controller.moveMediaItem(existingIdx, finalInsertIdx.coerceAtMost(controller.mediaItemCount - 1))
            } else {
                scope.launch {
                    val uri = withContext(Dispatchers.IO) { resolvePlayableTrackUri(track) }
                    val art = withContext(Dispatchers.IO) { resolveTrackArtworkBytes(track) }
                    val item = buildMediaItem(track, uri, art)
                    val safePos = (controller.currentMediaItemIndex + 1).coerceIn(0, controller.mediaItemCount)
                    controller.addMediaItem(safePos, item)
                }
            }
        }

        showMessage(
            if (isArabic) "تمت جدولة التالي: ${track.title}" else "Queued next: ${track.title}"
        )
    }

    fun setDraggingSlider(dragging: Boolean, dragPosMs: Float? = null) {
        isDraggingSlider = dragging
        if (dragPosMs != null) {
            _currentPositionMs.value = dragPosMs
        }
    }

    fun seekTo(positionMs: Long) {
        isDraggingSlider = false
        val clamped = positionMs.coerceAtLeast(0L)
        _currentPositionMs.value = clamped.toFloat()
        val ctrl = mediaController
        if (ctrl != null && ctrl.isConnected && ctrl.mediaItemCount > 0) {
            ctrl.seekTo(clamped)
        }
    }

    fun setSleepTimer(minutes: Int, label: String, isArabic: Boolean) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        _sleepTimerRemainingSeconds.value = if (minutes > 0) minutes * 60 else 0
        if (minutes <= 0) {
            mediaController?.volume = 1f
        }
        if (minutes > 0) {
            sleepTimerJob = scope.launch {
                while (isActive && _sleepTimerRemainingSeconds.value > 0) {
                    delay(1000L)
                    if (_isPlaying.value) {
                        val next = (_sleepTimerRemainingSeconds.value - 1).coerceAtLeast(0)
                        _sleepTimerRemainingSeconds.value = next
                        val ctrl = mediaController
                        if (next in 1..5) {
                            ctrl?.volume = (next.toFloat() / 5f).coerceIn(0f, 1f)
                        } else if (next == 0) {
                            ctrl?.pause()
                            ctrl?.volume = 1f
                            _isPlaying.value = false
                            _sleepTimerMinutes.value = 0
                            showMessage(
                                if (isArabic) "انتهى مؤقت النوم وتم إيقاف التشغيل"
                                else "Sleep timer finished. Playback stopped"
                            )
                            break
                        }
                    }
                }
            }
        }
        showMessage(
            when {
                minutes > 0 -> if (isArabic) "تم ضبط مؤقت النوم على $label" else "Sleep timer set for $label"
                minutes == -1 -> if (isArabic) "سيتوقف التشغيل تلقائياً بنهاية هذا المقطع" else "Playback will stop at track end"
                else -> if (isArabic) "تم إلغاء مؤقت النوم" else "Sleep timer disabled"
            }
        )
    }

    fun updateAudioEffects(eqBands: List<Float>, bassBoostPercent: Int, spatialEnabled: Boolean = true) {
        MusicPlaybackService.updateAudioEffects(eqBands, bassBoostPercent, spatialEnabled)
    }

    fun toggleFavorite(trackId: String, isArabic: Boolean) {
        val updated = prefsRepo.toggleListenFavorite(trackId)
        _favoriteIds.value = updated
        val added = updated.contains(trackId)
        showMessage(
            if (added) {
                if (isArabic) "تمت الإضافة إلى المفضلة" else "Added to Favorites"
            } else {
                if (isArabic) "تمت الإزالة من المفضلة" else "Removed from Favorites"
            }
        )
    }

    fun toggleDownloaded(trackId: String, isArabic: Boolean) {
        val updated = prefsRepo.toggleListenDownloaded(trackId)
        _downloadedIds.value = updated
        showMessage(
            if (updated.contains(trackId)) {
                if (isArabic) "تم الحفظ في التنزيلات" else "Saved to Downloads"
            } else {
                if (isArabic) "تمت الإزالة من التنزيلات" else "Removed from Downloads"
            }
        )
    }

    fun saveUserPlaylists(updated: List<UserAudioPlaylist>) {
        _userPlaylists.value = updated
        prefsRepo.saveUserAudioPlaylists(updated)
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
        snackbarJob?.cancel()
        snackbarJob = scope.launch {
            delay(2500L)
            _snackbarMessage.value = null
        }
    }

    private fun recordPlayTelemetry(track: LocalAudioTrack) {
        _trackPlaysMap.value = prefsRepo.recordListenTrackPlay(track.id)
        _historyIds.value = prefsRepo.getListenHistoryIds()
        _totalPlaysCount.value = prefsRepo.getListenTotalPlays()
    }

    private suspend fun resolvePlayableTrackUri(track: LocalAudioTrack): Uri {
        val raw = track.audioUri
        if (!raw.isNullOrBlank()) {
            return Uri.parse(raw)
        }
        return BuiltInAudioGenerator.ensurePlayableUri(appContext, track.id)
    }

    private fun buildMediaItem(
        track: LocalAudioTrack,
        uri: Uri,
        artworkBytes: ByteArray?
    ): ExoMediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(track.title)
            .setDisplayTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setSubtitle(track.artist)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .setIsPlayable(true)
            .setIsBrowsable(false)

        if (!track.albumArtUri.isNullOrBlank()) {
            metadataBuilder.setArtworkUri(Uri.parse(track.albumArtUri))
        }
        if (artworkBytes != null && artworkBytes.isNotEmpty()) {
            metadataBuilder.setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }

        return ExoMediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    private fun resolveTrackArtworkBytes(track: LocalAudioTrack): ByteArray? {
        artworkBytesCache.get(track.id)?.let { return it }

        val rawBitmap: Bitmap? = if (track.isFromDevice && (!track.audioUri.isNullOrBlank() || !track.albumArtUri.isNullOrBlank())) {
            LocalMediaScanner.extractAudioCoverArt(
                context = appContext,
                audioUriStr = track.audioUri,
                albumArtUriStr = track.albumArtUri
            )
        } else {
            null
        } ?: runCatching {
            BitmapFactory.decodeResource(appContext.resources, track.albumCoverRes)
        }.getOrNull()

        if (rawBitmap == null || rawBitmap.isRecycled) return null

        val maxDim = 512
        val scaledBitmap = if (rawBitmap.width > maxDim || rawBitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(rawBitmap.width, rawBitmap.height).toFloat()
            val w = (rawBitmap.width * scale).toInt().coerceAtLeast(1)
            val h = (rawBitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(rawBitmap, w, h, true)
        } else {
            rawBitmap
        }

        val bytes = ByteArrayOutputStream().use { baos ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            baos.toByteArray()
        }
        if (scaledBitmap !== rawBitmap && !scaledBitmap.isRecycled) {
            scaledBitmap.recycle()
        }

        if (bytes.isNotEmpty()) {
            artworkBytesCache.put(track.id, bytes)
        }
        return bytes.takeIf { it.isNotEmpty() }
    }

    private fun preloadAdjacentArtwork(activeIndex: Int) {
        val list = _playlist.value
        if (list.size <= 1) return
        val candidates = listOf(
            (activeIndex + 1) % list.size,
            if (activeIndex > 0) activeIndex - 1 else list.lastIndex
        ).distinct().filter { it != activeIndex }

        scope.launch {
            for (adjIdx in candidates) {
                val track = list.getOrNull(adjIdx) ?: continue
                if (artworkBytesCache.get(track.id) != null) continue
                val bytes = withContext(Dispatchers.IO) { resolveTrackArtworkBytes(track) } ?: continue
                val uri = withContext(Dispatchers.IO) { resolvePlayableTrackUri(track) }
                val ctrl = mediaController ?: break
                if (ctrl.isConnected && adjIdx < ctrl.mediaItemCount && adjIdx != ctrl.currentMediaItemIndex) {
                    val existingItem = ctrl.getMediaItemAt(adjIdx)
                    if (existingItem.mediaId == track.id) {
                        ctrl.replaceMediaItem(adjIdx, buildMediaItem(track, uri, bytes))
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ListenPlaybackManager? = null

        fun getInstance(context: Context): ListenPlaybackManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ListenPlaybackManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
