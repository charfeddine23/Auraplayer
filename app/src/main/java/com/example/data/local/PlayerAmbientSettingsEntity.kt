package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SeekbarThickness(
    val titleEn: String,
    val titleAr: String,
    val scaleFactor: Float,
    val containerHeightDp: Int
) {
    COMPACT("Slim", "رفيع", 0.78f, 34),
    BALANCED("Balanced", "متوازن", 1.0f, 40),
    BOLD("Bold", "عريض", 1.24f, 46);

    companion object {
        fun fromName(name: String?): SeekbarThickness =
            entries.firstOrNull { it.name == name } ?: BALANCED
    }
}

enum class SeekbarGlowIntensity(
    val titleEn: String,
    val titleAr: String,
    val alphaMultiplier: Float
) {
    SUBTLE("Subtle", "هادئ", 0.55f),
    NORMAL("Balanced", "متوازن", 1.0f),
    VIVID("Vivid", "ساطع", 1.45f);

    companion object {
        fun fromName(name: String?): SeekbarGlowIntensity =
            entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

@Entity(tableName = "player_ambient_settings")
data class PlayerAmbientSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isAmbientGlowEnabled: Boolean = true,
    val ambientModeName: String = AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR.name,
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
    val seekbarThickness: String = SeekbarThickness.BALANCED.name,
    val seekbarGlowIntensity: String = SeekbarGlowIntensity.NORMAL.name,
    val showBookmarkMarkersOnSeekbar: Boolean = true,
    val liveSeekPreview: Boolean = true,
    val seekbarBelowControls: Boolean = false,
    val updatedAtMs: Long = System.currentTimeMillis()
)
