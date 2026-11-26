package com.example.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PlayerAmbientConfig(
    val isAmbientGlowEnabled: Boolean = true,
    val blackBlurMode: AmbientBlackBlurMode = AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR,
    val playbackSpeed: Float = 1.0f,
    val resizeModeName: String = "FIT",
    val isAutoCropBlackBars: Boolean = false,
    val selectedAudioTrack: String = "Stereo 2.0 (AAC)",
    val selectedSubtitle: String = "Off",
    val audioBoostMultiplier: Float = 1.0f,
    val subtitleDelayOffsetMs: Long = 0L,
    val isToolbarCollapsed: Boolean = false,
    val doubleTapSeekSeconds: Int = 10,
    val controlsAutoHideSeconds: Int = 10,
    val enableSwipeGestures: Boolean = true,
    val seekbarThickness: SeekbarThickness = SeekbarThickness.BALANCED,
    val seekbarGlowIntensity: SeekbarGlowIntensity = SeekbarGlowIntensity.NORMAL,
    val showBookmarkMarkersOnSeekbar: Boolean = true,
    val liveSeekPreview: Boolean = true,
    val seekbarBelowControls: Boolean = false
)

class AmbientGlowRepository(private val dao: PlayerAmbientSettingsDao) {

    val configFlow: Flow<PlayerAmbientConfig> = dao.observeSettings().map { entity ->
        if (entity == null) {
            PlayerAmbientConfig()
        } else {
            PlayerAmbientConfig(
                isAmbientGlowEnabled = entity.isAmbientGlowEnabled,
                blackBlurMode = AmbientBlackBlurMode.fromName(entity.ambientModeName),
                playbackSpeed = entity.playbackSpeed,
                resizeModeName = entity.resizeModeName,
                isAutoCropBlackBars = entity.isAutoCropBlackBars,
                selectedAudioTrack = entity.selectedAudioTrack,
                selectedSubtitle = entity.selectedSubtitle,
                audioBoostMultiplier = entity.audioBoostMultiplier,
                subtitleDelayOffsetMs = entity.subtitleDelayOffsetMs,
                isToolbarCollapsed = entity.isToolbarCollapsed,
                doubleTapSeekSeconds = entity.doubleTapSeekSeconds.coerceIn(5, 30),
                controlsAutoHideSeconds = entity.controlsAutoHideSeconds.coerceIn(3, 15),
                enableSwipeGestures = entity.enableSwipeGestures,
                seekbarThickness = SeekbarThickness.fromName(entity.seekbarThickness),
                seekbarGlowIntensity = SeekbarGlowIntensity.fromName(entity.seekbarGlowIntensity),
                showBookmarkMarkersOnSeekbar = entity.showBookmarkMarkersOnSeekbar,
                liveSeekPreview = entity.liveSeekPreview,
                seekbarBelowControls = entity.seekbarBelowControls
            )
        }
    }

    suspend fun update(transform: (PlayerAmbientSettingsEntity) -> PlayerAmbientSettingsEntity) {
        val current = dao.getSettingsOnce() ?: PlayerAmbientSettingsEntity()
        val updated = transform(current).copy(updatedAtMs = System.currentTimeMillis())
        dao.upsertSettings(updated)
    }

    suspend fun setAmbientGlowEnabled(enabled: Boolean) = update {
        it.copy(isAmbientGlowEnabled = enabled)
    }

    suspend fun setBlackBlurMode(mode: AmbientBlackBlurMode) = update {
        it.copy(
            isAmbientGlowEnabled = true,
            ambientModeName = mode.name
        )
    }

    suspend fun setPlaybackSpeed(speed: Float) = update {
        it.copy(playbackSpeed = speed)
    }

    suspend fun setResizeModeName(resizeModeName: String) = update {
        it.copy(resizeModeName = resizeModeName)
    }

    suspend fun setAutoCropBlackBars(enabled: Boolean) = update {
        it.copy(isAutoCropBlackBars = enabled)
    }

    suspend fun setSelectedAudioTrack(track: String) = update {
        it.copy(selectedAudioTrack = track)
    }

    suspend fun setSelectedSubtitle(subtitle: String) = update {
        it.copy(selectedSubtitle = subtitle)
    }

    suspend fun setAudioBoostMultiplier(boost: Float) = update {
        it.copy(audioBoostMultiplier = boost)
    }

    suspend fun setSubtitleDelayOffsetMs(offsetMs: Long) = update {
        it.copy(subtitleDelayOffsetMs = offsetMs)
    }

    suspend fun setToolbarCollapsed(collapsed: Boolean) = update {
        it.copy(isToolbarCollapsed = collapsed)
    }

    suspend fun setDoubleTapSeekSeconds(seconds: Int) = update {
        it.copy(doubleTapSeekSeconds = seconds.coerceIn(5, 30))
    }

    suspend fun setControlsAutoHideSeconds(seconds: Int) = update {
        it.copy(controlsAutoHideSeconds = seconds.coerceIn(3, 15))
    }

    suspend fun setEnableSwipeGestures(enabled: Boolean) = update {
        it.copy(enableSwipeGestures = enabled)
    }

    suspend fun setSeekbarThickness(thickness: SeekbarThickness) = update {
        it.copy(seekbarThickness = thickness.name)
    }

    suspend fun setSeekbarGlowIntensity(intensity: SeekbarGlowIntensity) = update {
        it.copy(seekbarGlowIntensity = intensity.name)
    }

    suspend fun setShowBookmarkMarkersOnSeekbar(show: Boolean) = update {
        it.copy(showBookmarkMarkersOnSeekbar = show)
    }

    suspend fun setLiveSeekPreview(enabled: Boolean) = update {
        it.copy(liveSeekPreview = enabled)
    }

    suspend fun setSeekbarBelowControls(enabled: Boolean) = update {
        it.copy(seekbarBelowControls = enabled)
    }
}
