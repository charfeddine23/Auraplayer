package com.example.data.local

import com.example.data.model.StoreAvatarItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class CyberAvatarConfig(
    val equippedAvatar: StoreAvatarItem? = null,
    val unlockedAvatarIds: Set<String> = emptySet(),
    val isAuraFrameEnabled: Boolean = true
) {
    fun isPurchased(avatar: StoreAvatarItem): Boolean = unlockedAvatarIds.contains(avatar.id)
}

class CyberAvatarRepository(private val dao: CyberAvatarDao) {

    val configFlow: Flow<CyberAvatarConfig> = dao.observeState().map { entity ->
        if (entity == null) {
            CyberAvatarConfig()
        } else {
            val unlocked = parseIds(entity.unlockedAvatarIds)
            CyberAvatarConfig(
                equippedAvatar = StoreAvatarItem.fromId(entity.equippedAvatarId),
                unlockedAvatarIds = unlocked,
                isAuraFrameEnabled = entity.isAuraFrameEnabled
            )
        }
    }

    suspend fun seedIfEmpty(initialUnlockedIds: Set<String> = emptySet(), initialEquippedId: String? = null) {
        if (dao.getStateOnce() == null) {
            dao.upsertState(
                CyberAvatarStateEntity(
                    id = 1,
                    equippedAvatarId = initialEquippedId,
                    unlockedAvatarIds = initialUnlockedIds.joinToString(","),
                    isAuraFrameEnabled = true,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun unlockAvatar(avatar: StoreAvatarItem, autoEquip: Boolean = true) {
        val current = dao.getStateOnce()
        val updatedSet = parseIds(current?.unlockedAvatarIds).toMutableSet().apply {
            add(avatar.id)
        }
        val nextEquippedId = if (autoEquip) avatar.id else current?.equippedAvatarId
        dao.upsertState(
            CyberAvatarStateEntity(
                id = 1,
                equippedAvatarId = nextEquippedId,
                unlockedAvatarIds = updatedSet.joinToString(","),
                isAuraFrameEnabled = current?.isAuraFrameEnabled ?: true,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun equipAvatar(avatar: StoreAvatarItem) {
        val current = dao.getStateOnce()
        dao.upsertState(
            CyberAvatarStateEntity(
                id = 1,
                equippedAvatarId = avatar.id,
                unlockedAvatarIds = current?.unlockedAvatarIds ?: "",
                isAuraFrameEnabled = current?.isAuraFrameEnabled ?: true,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun unequipAvatar() {
        val current = dao.getStateOnce()
        dao.upsertState(
            CyberAvatarStateEntity(
                id = 1,
                equippedAvatarId = null,
                unlockedAvatarIds = current?.unlockedAvatarIds ?: "",
                isAuraFrameEnabled = current?.isAuraFrameEnabled ?: true,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun setAuraFrameEnabled(enabled: Boolean) {
        val current = dao.getStateOnce()
        dao.upsertState(
            CyberAvatarStateEntity(
                id = 1,
                equippedAvatarId = current?.equippedAvatarId,
                unlockedAvatarIds = current?.unlockedAvatarIds ?: "",
                isAuraFrameEnabled = enabled,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    private fun parseIds(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
