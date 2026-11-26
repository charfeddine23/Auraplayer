package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cyber_avatar_state")
data class CyberAvatarStateEntity(
    @PrimaryKey val id: Int = 1,
    val equippedAvatarId: String? = null,
    val unlockedAvatarIds: String = "",
    val isAuraFrameEnabled: Boolean = true,
    val updatedAtMs: Long = System.currentTimeMillis()
)
