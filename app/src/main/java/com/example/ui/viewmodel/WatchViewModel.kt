package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FolderCoverCacheEntity
import com.example.data.local.VideoBookmarkGroup
import com.example.data.local.WatchDataRepository
import com.example.data.local.WatchSettingsConfig
import com.example.data.local.WatchSortBy
import com.example.data.local.WatchSortDirection
import com.example.data.util.OnlineFolderCoverFetcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val folderCoverCacheDao = db.folderCoverCacheDao()

    private val repository: WatchDataRepository = WatchDataRepository(
        watchSettingsDao = db.watchSettingsDao(),
        bookmarkDao = db.videoBookmarkDao(),
        folderCoverCacheDao = folderCoverCacheDao
    )

    val watchSettings: StateFlow<WatchSettingsConfig> = repository.watchSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = WatchSettingsConfig()
        )

    val bookmarkGroups: StateFlow<List<VideoBookmarkGroup>> = repository.bookmarkGroupsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val cachedFolderCovers: StateFlow<Map<String, FolderCoverCacheEntity>> =
        repository.folderCoverCacheFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyMap()
            )

    fun setSortBy(sortBy: WatchSortBy) {
        viewModelScope.launch { repository.setSortBy(sortBy) }
    }

    fun setSortDirection(direction: WatchSortDirection) {
        viewModelScope.launch { repository.setSortDirection(direction) }
    }

    fun setShowContinueWatching(show: Boolean) {
        viewModelScope.launch { repository.setShowContinueWatching(show) }
    }

    fun setShowSearchBar(show: Boolean) {
        viewModelScope.launch { repository.setShowSearchBar(show) }
    }

    fun setHideShortClips(hide: Boolean) {
        viewModelScope.launch { repository.setHideShortClips(hide) }
    }

    fun setShowNewVideoBadge(show: Boolean) {
        viewModelScope.launch { repository.setShowNewVideoBadge(show) }
    }

    fun setNewVideoBadgeDays(days: Int) {
        viewModelScope.launch { repository.setNewVideoBadgeDays(days) }
    }

    fun setAutoFetchOnlineFolderCovers(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoFetchOnlineFolderCovers(enabled) }
    }

    fun syncFolderCovers(
        folderNames: List<String>,
        isMusicFolder: Boolean,
        forceRefresh: Boolean = false
    ) {
        if (!forceRefresh && !watchSettings.value.autoFetchOnlineFolderCovers) return
        val appContext = getApplication<Application>()
        val uniqueNames = folderNames
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        uniqueNames.forEach { name ->
            viewModelScope.launch {
                OnlineFolderCoverFetcher.resolveAndCacheFolderCover(
                    context = appContext,
                    dao = folderCoverCacheDao,
                    folderName = name,
                    isMusicFolder = isMusicFolder,
                    forceRefresh = forceRefresh
                )
            }
        }
    }

    fun clearAndRefreshFolderCovers(folderNames: List<String>, isMusicFolder: Boolean) {
        val appContext = getApplication<Application>()
        viewModelScope.launch {
            OnlineFolderCoverFetcher.clearAllCachedCovers(appContext, folderCoverCacheDao)
            if (watchSettings.value.autoFetchOnlineFolderCovers) {
                folderNames
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .forEach { name ->
                        launch {
                            OnlineFolderCoverFetcher.resolveAndCacheFolderCover(
                                context = appContext,
                                dao = folderCoverCacheDao,
                                folderName = name,
                                isMusicFolder = isMusicFolder,
                                forceRefresh = true
                            )
                        }
                    }
            }
        }
    }

    fun deleteBookmarkById(id: Long) {
        viewModelScope.launch { repository.deleteBookmarkById(id) }
    }

    fun deleteBookmarksForVideo(videoId: String, uriString: String?) {
        viewModelScope.launch { repository.deleteBookmarksForVideo(videoId, uriString) }
    }

    fun deleteAllBookmarks() {
        viewModelScope.launch { repository.deleteAllBookmarks() }
    }
}
