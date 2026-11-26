package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.VideoBookmarkGroup
import com.example.data.local.WatchDataRepository
import com.example.data.local.WatchSettingsConfig
import com.example.data.local.WatchSortBy
import com.example.data.local.WatchSortDirection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WatchDataRepository = run {
        val db = AppDatabase.getInstance(application)
        WatchDataRepository(
            watchSettingsDao = db.watchSettingsDao(),
            bookmarkDao = db.videoBookmarkDao()
        )
    }

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
