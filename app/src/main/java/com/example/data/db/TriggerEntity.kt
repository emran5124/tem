package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triggers")
data class TriggerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tag: String,
    val displayName: String,
    val colorHex: String = "#6366F1",
    val isDefault: Boolean = false
)
