package com.example.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * Lightweight singleton bus tracking active offline video/audio player state
 * so interactive Home Mimics react smoothly to real playback activity.
 */
object MediaPlaybackActivityBus {
    private val _isVideoPlaying = MutableStateFlow(false)
    private val _isAudioPlaying = MutableStateFlow(false)
    private val _lastActiveTimestampMs = MutableStateFlow(0L)

    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()
    val lastActiveTimestampMs: StateFlow<Long> = _lastActiveTimestampMs.asStateFlow()

    val isAnyPlayerActive = combine(_isVideoPlaying, _isAudioPlaying) { video, audio ->
        video || audio
    }

    fun setVideoPlaying(playing: Boolean) {
        _isVideoPlaying.value = playing
        if (playing) {
            _lastActiveTimestampMs.value = System.currentTimeMillis()
        }
    }

    fun setAudioPlaying(playing: Boolean) {
        _isAudioPlaying.value = playing
        if (playing) {
            _lastActiveTimestampMs.value = System.currentTimeMillis()
        }
    }

    fun pulseActivity() {
        _lastActiveTimestampMs.update { System.currentTimeMillis() }
    }
}
