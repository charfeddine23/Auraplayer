package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchSettingsDao {
    @Query("SELECT * FROM watch_settings WHERE id = 1")
    fun observeWatchSettings(): Flow<WatchSettingsEntity?>

    @Query("SELECT * FROM watch_settings WHERE id = 1")
    suspend fun getWatchSettingsOnce(): WatchSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchSettings(settings: WatchSettingsEntity)
}
