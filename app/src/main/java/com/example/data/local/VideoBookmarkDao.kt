package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoBookmarkDao {
    @Query("SELECT * FROM video_bookmarks ORDER BY createdAtMs DESC, timestampMs ASC")
    fun observeAllBookmarks(): Flow<List<VideoBookmarkEntity>>

    @Query(
        """
        SELECT * FROM video_bookmarks
        WHERE videoId = :videoId OR (:uriString IS NOT NULL AND uriString = :uriString)
        ORDER BY timestampMs ASC
        """
    )
    fun observeBookmarksForVideo(videoId: String, uriString: String?): Flow<List<VideoBookmarkEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM video_bookmarks
        WHERE (videoId = :videoId OR (:uriString IS NOT NULL AND uriString = :uriString))
          AND ABS(timestampMs - :timestampMs) <= 1500
        """
    )
    suspend fun countNearbyBookmarks(videoId: String, uriString: String?, timestampMs: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: VideoBookmarkEntity): Long

    @Query("DELETE FROM video_bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query(
        """
        DELETE FROM video_bookmarks
        WHERE videoId = :videoId OR (:uriString IS NOT NULL AND uriString = :uriString)
        """
    )
    suspend fun deleteBookmarksForVideo(videoId: String, uriString: String?)

    @Query("DELETE FROM video_bookmarks")
    suspend fun deleteAllBookmarks()
}
