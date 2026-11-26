package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.local.AmbientGlowRepository
import com.example.data.local.AppDatabase
import com.example.data.local.PlayerAmbientConfig
import com.example.data.local.SeekbarGlowIntensity
import com.example.data.local.SeekbarThickness
import com.example.data.local.VideoBookmarkEntity
import com.example.data.local.WatchDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerAmbientViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AmbientGlowRepository(db.playerAmbientSettingsDao())
    private val bookmarkRepository = WatchDataRepository(
        watchSettingsDao = db.watchSettingsDao(),
        bookmarkDao = db.videoBookmarkDao()
    )

    val config: StateFlow<PlayerAmbientConfig> = repository.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PlayerAmbientConfig()
        )

    fun observeBookmarksForVideo(videoId: String, uriString: String?): Flow<List<VideoBookmarkEntity>> =
        bookmarkRepository.observeBookmarksForVideo(videoId, uriString)

    fun addBookmark(
        videoId: String,
        uriString: String?,
        videoTitle: String,
        timestampMs: Long,
        durationMs: Long,
        thumbnailRes: Int,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val inserted = bookmarkRepository.addBookmarkIfNotDuplicate(
                videoId = videoId,
                uriString = uriString,
                videoTitle = videoTitle,
                timestampMs = timestampMs,
                durationMs = durationMs,
                thumbnailRes = thumbnailRes
            )
            onResult(inserted)
        }
    }

    fun deleteBookmarkById(id: Long) {
        viewModelScope.launch { bookmarkRepository.deleteBookmarkById(id) }
    }

    fun setAmbientGlowEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setAmbientGlowEnabled(enabled) }
    }

    fun selectBlackBlurMode(mode: AmbientBlackBlurMode) {
        viewModelScope.launch { repository.setBlackBlurMode(mode) }
    }

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch { repository.setPlaybackSpeed(speed) }
    }

    fun setResizeModeName(resizeModeName: String) {
        viewModelScope.launch { repository.setResizeModeName(resizeModeName) }
    }

    fun setAutoCropBlackBars(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoCropBlackBars(enabled) }
    }

    fun setSelectedAudioTrack(track: String) {
        viewModelScope.launch { repository.setSelectedAudioTrack(track) }
    }

    fun setSelectedSubtitle(subtitle: String) {
        viewModelScope.launch { repository.setSelectedSubtitle(subtitle) }
    }

    fun setAudioBoostMultiplier(boost: Float) {
        viewModelScope.launch { repository.setAudioBoostMultiplier(boost) }
    }

    fun setSubtitleDelayOffsetMs(offsetMs: Long) {
        viewModelScope.launch { repository.setSubtitleDelayOffsetMs(offsetMs) }
    }

    fun setToolbarCollapsed(collapsed: Boolean) {
        viewModelScope.launch { repository.setToolbarCollapsed(collapsed) }
    }

    fun setDoubleTapSeekSeconds(seconds: Int) {
        viewModelScope.launch { repository.setDoubleTapSeekSeconds(seconds) }
    }

    fun setControlsAutoHideSeconds(seconds: Int) {
        viewModelScope.launch { repository.setControlsAutoHideSeconds(seconds) }
    }

    fun setEnableSwipeGestures(enabled: Boolean) {
        viewModelScope.launch { repository.setEnableSwipeGestures(enabled) }
    }

    fun setSeekbarThickness(thickness: SeekbarThickness) {
        viewModelScope.launch { repository.setSeekbarThickness(thickness) }
    }

    fun setSeekbarGlowIntensity(intensity: SeekbarGlowIntensity) {
        viewModelScope.launch { repository.setSeekbarGlowIntensity(intensity) }
    }

    fun setShowBookmarkMarkersOnSeekbar(show: Boolean) {
        viewModelScope.launch { repository.setShowBookmarkMarkersOnSeekbar(show) }
    }

    fun setLiveSeekPreview(enabled: Boolean) {
        viewModelScope.launch { repository.setLiveSeekPreview(enabled) }
    }

    fun setSeekbarBelowControls(enabled: Boolean) {
        viewModelScope.launch { repository.setSeekbarBelowControls(enabled) }
    }
}
