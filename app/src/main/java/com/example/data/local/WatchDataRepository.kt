package com.example.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class WatchSettingsConfig(
    val sortBy: WatchSortBy = WatchSortBy.DEFAULT,
    val sortDirection: WatchSortDirection = WatchSortDirection.DESCENDING,
    val showContinueWatching: Boolean = true,
    val showSearchBar: Boolean = true,
    val hideShortClips: Boolean = false,
    val showNewVideoBadge: Boolean = true,
    val newVideoBadgeDays: Int = 7
)

data class VideoBookmarkGroup(
    val videoKey: String,
    val videoId: String,
    val uriString: String?,
    val videoTitle: String,
    val durationMs: Long,
    val thumbnailRes: Int,
    val bookmarks: List<VideoBookmarkEntity>
)

class WatchDataRepository(
    private val watchSettingsDao: WatchSettingsDao,
    private val bookmarkDao: VideoBookmarkDao
) {
    val watchSettingsFlow: Flow<WatchSettingsConfig> =
        watchSettingsDao.observeWatchSettings().map { entity ->
            if (entity == null) {
                WatchSettingsConfig()
            } else {
                WatchSettingsConfig(
                    sortBy = WatchSortBy.fromName(entity.sortBy),
                    sortDirection = WatchSortDirection.fromName(entity.sortDirection),
                    showContinueWatching = entity.showContinueWatching,
                    showSearchBar = entity.showSearchBar,
                    hideShortClips = entity.hideShortClips,
                    showNewVideoBadge = entity.showNewVideoBadge,
                    newVideoBadgeDays = entity.newVideoBadgeDays.coerceIn(1, 365)
                )
            }
        }

    val bookmarkGroupsFlow: Flow<List<VideoBookmarkGroup>> =
        bookmarkDao.observeAllBookmarks().map { allBookmarks ->
            allBookmarks
                .groupBy { bm -> bm.uriString?.takeIf { it.isNotBlank() } ?: bm.videoId }
                .map { (key, list) ->
                    val latest = list.maxByOrNull { it.createdAtMs } ?: list.first()
                    val bestDuration = list.maxOfOrNull { it.durationMs } ?: 0L
                    VideoBookmarkGroup(
                        videoKey = key,
                        videoId = latest.videoId,
                        uriString = latest.uriString,
                        videoTitle = latest.videoTitle,
                        durationMs = bestDuration,
                        thumbnailRes = latest.thumbnailRes,
                        bookmarks = list.sortedBy { it.timestampMs }
                    )
                }
                .sortedByDescending { group ->
                    group.bookmarks.maxOfOrNull { it.createdAtMs } ?: 0L
                }
        }

    fun observeBookmarksForVideo(videoId: String, uriString: String?): Flow<List<VideoBookmarkEntity>> =
        bookmarkDao.observeBookmarksForVideo(videoId, uriString)

    suspend fun addBookmarkIfNotDuplicate(
        videoId: String,
        uriString: String?,
        videoTitle: String,
        timestampMs: Long,
        durationMs: Long,
        thumbnailRes: Int
    ): Boolean {
        val safePos = timestampMs.coerceAtLeast(0L)
        val nearby = bookmarkDao.countNearbyBookmarks(videoId, uriString, safePos)
        if (nearby > 0) return false
        bookmarkDao.insertBookmark(
            VideoBookmarkEntity(
                videoId = videoId,
                uriString = uriString,
                videoTitle = videoTitle,
                timestampMs = safePos,
                durationMs = durationMs.coerceAtLeast(0L),
                thumbnailRes = thumbnailRes
            )
        )
        return true
    }

    suspend fun deleteBookmarkById(id: Long) = bookmarkDao.deleteBookmarkById(id)

    suspend fun deleteBookmarksForVideo(videoId: String, uriString: String?) =
        bookmarkDao.deleteBookmarksForVideo(videoId, uriString)

    suspend fun deleteAllBookmarks() = bookmarkDao.deleteAllBookmarks()

    private suspend fun updateSettings(transform: (WatchSettingsEntity) -> WatchSettingsEntity) {
        val current = watchSettingsDao.getWatchSettingsOnce() ?: WatchSettingsEntity()
        watchSettingsDao.upsertWatchSettings(
            transform(current).copy(updatedAtMs = System.currentTimeMillis())
        )
    }

    suspend fun setSortBy(sortBy: WatchSortBy) = updateSettings {
        it.copy(sortBy = sortBy.name)
    }

    suspend fun setSortDirection(direction: WatchSortDirection) = updateSettings {
        it.copy(sortDirection = direction.name)
    }

    suspend fun setShowContinueWatching(show: Boolean) = updateSettings {
        it.copy(showContinueWatching = show)
    }

    suspend fun setShowSearchBar(show: Boolean) = updateSettings {
        it.copy(showSearchBar = show)
    }

    suspend fun setHideShortClips(hide: Boolean) = updateSettings {
        it.copy(hideShortClips = hide)
    }

    suspend fun setShowNewVideoBadge(show: Boolean) = updateSettings {
        it.copy(showNewVideoBadge = show)
    }

    suspend fun setNewVideoBadgeDays(days: Int) = updateSettings {
        it.copy(newVideoBadgeDays = days.coerceIn(1, 365))
    }
}
