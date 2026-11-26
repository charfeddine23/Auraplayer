package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_settings")
data class WatchSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val sortBy: String = WatchSortBy.DEFAULT.name,
    val sortDirection: String = WatchSortDirection.DESCENDING.name,
    val showContinueWatching: Boolean = true,
    val showSearchBar: Boolean = true,
    val hideShortClips: Boolean = false,
    val showNewVideoBadge: Boolean = true,
    val newVideoBadgeDays: Int = 7,
    val autoFetchOnlineFolderCovers: Boolean = true,
    val updatedAtMs: Long = System.currentTimeMillis()
)

enum class WatchSortBy(val titleEn: String, val titleAr: String) {
    DEFAULT("Default", "الافتراضي"),
    NAME("Name", "الاسم"),
    DURATION("Duration", "المدة"),
    SIZE("Size", "الحجم");

    companion object {
        fun fromName(name: String?): WatchSortBy = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

enum class WatchSortDirection(val titleEn: String, val titleAr: String) {
    ASCENDING("Ascending", "تصاعدي"),
    DESCENDING("Descending", "تنازلي");

    companion object {
        fun fromName(name: String?): WatchSortDirection = entries.firstOrNull { it.name == name } ?: DESCENDING
    }
}
