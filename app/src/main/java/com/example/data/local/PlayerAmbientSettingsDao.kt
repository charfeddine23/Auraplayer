package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerAmbientSettingsDao {
    @Query("SELECT * FROM player_ambient_settings WHERE id = 1")
    fun observeSettings(): Flow<PlayerAmbientSettingsEntity?>

    @Query("SELECT * FROM player_ambient_settings WHERE id = 1")
    suspend fun getSettingsOnce(): PlayerAmbientSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: PlayerAmbientSettingsEntity)
}
