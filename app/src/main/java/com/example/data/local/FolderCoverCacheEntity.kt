package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "folder_cover_cache")
data class FolderCoverCacheEntity(
    @PrimaryKey val folderKey: String,
    val displayName: String,
    val sourceUrl: String? = null,
    val localFilePath: String? = null,
    val sourceProvider: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val isMusicFolder: Boolean = false,
    val status: String = STATUS_CACHED,
    val fetchedAtMs: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_CACHED = "CACHED"
        const val STATUS_NO_HD_MATCH = "NO_HD_MATCH"
    }
}

@Dao
interface FolderCoverCacheDao {
    @Query("SELECT * FROM folder_cover_cache")
    fun observeAll(): Flow<List<FolderCoverCacheEntity>>

    @Query("SELECT * FROM folder_cover_cache WHERE folderKey = :key LIMIT 1")
    suspend fun getByKey(key: String): FolderCoverCacheEntity?

    @Upsert
    suspend fun upsert(entry: FolderCoverCacheEntity)

    @Query("DELETE FROM folder_cover_cache WHERE folderKey = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM folder_cover_cache")
    suspend fun clearAll()
}
