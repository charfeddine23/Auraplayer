package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.HomeMimicItem

@Entity(tableName = "home_mimic_state")
data class HomeMimicEntity(
    @PrimaryKey val id: Int = 1,
    val equippedMimicId: String = HomeMimicItem.STREAK_FLAME.id,
    val isMimicEnabled: Boolean = true,
    val updatedAtMs: Long = System.currentTimeMillis()
)
