package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filled_results")
data class FilledResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val templateId: Int? = null,
    val templateTitle: String,
    val filledText: String,
    val valuesJson: String = "{}",
    val imageUri: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
