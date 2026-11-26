package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_bookmarks")
data class VideoBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val videoId: String,
    val uriString: String?,
    val videoTitle: String,
    val timestampMs: Long,
    val durationMs: Long,
    val thumbnailRes: Int = 0,
    val createdAtMs: Long = System.currentTimeMillis()
)
