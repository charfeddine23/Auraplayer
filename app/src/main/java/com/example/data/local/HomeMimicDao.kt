package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeMimicDao {
    @Query("SELECT * FROM home_mimic_state WHERE id = 1")
    fun observeState(): Flow<HomeMimicEntity?>

    @Query("SELECT * FROM home_mimic_state WHERE id = 1")
    suspend fun getStateOnce(): HomeMimicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: HomeMimicEntity)
}
