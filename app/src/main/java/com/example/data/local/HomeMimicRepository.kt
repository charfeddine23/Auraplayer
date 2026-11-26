package com.example.data.local

import com.example.data.model.HomeMimicItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class HomeMimicConfig(
    val equippedMimic: HomeMimicItem = HomeMimicItem.STREAK_FLAME,
    val isMimicEnabled: Boolean = true
)

class HomeMimicRepository(private val dao: HomeMimicDao) {

    val configFlow: Flow<HomeMimicConfig> = dao.observeState().map { entity ->
        if (entity == null) {
            HomeMimicConfig()
        } else {
            HomeMimicConfig(
                equippedMimic = HomeMimicItem.fromId(entity.equippedMimicId) ?: HomeMimicItem.STREAK_FLAME,
                isMimicEnabled = entity.isMimicEnabled
            )
        }
    }

    suspend fun seedIfEmpty(initialItem: HomeMimicItem, isEnabled: Boolean) {
        if (dao.getStateOnce() == null) {
            dao.upsertState(
                HomeMimicEntity(
                    id = 1,
                    equippedMimicId = initialItem.id,
                    isMimicEnabled = isEnabled,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun equipMimic(item: HomeMimicItem) {
        dao.upsertState(
            HomeMimicEntity(
                id = 1,
                equippedMimicId = item.id,
                isMimicEnabled = true,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun setMimicEnabled(enabled: Boolean, fallbackItem: HomeMimicItem? = null) {
        val current = dao.getStateOnce()
        val resolvedId = when {
            fallbackItem != null && enabled -> fallbackItem.id
            current != null -> current.equippedMimicId
            else -> HomeMimicItem.STREAK_FLAME.id
        }
        dao.upsertState(
            HomeMimicEntity(
                id = 1,
                equippedMimicId = resolvedId,
                isMimicEnabled = enabled,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }
}
