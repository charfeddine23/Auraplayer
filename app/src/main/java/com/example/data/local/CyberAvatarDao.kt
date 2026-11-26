package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CyberAvatarDao {
    @Query("SELECT * FROM cyber_avatar_state WHERE id = 1")
    fun observeState(): Flow<CyberAvatarStateEntity?>

    @Query("SELECT * FROM cyber_avatar_state WHERE id = 1")
    suspend fun getStateOnce(): CyberAvatarStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: CyberAvatarStateEntity)
}
