package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_audio",
    indices = [Index(value = ["sourceUrl"], unique = true)]
)
data class CachedAudio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val sourceUrl: String,
    val localFilePath: String,
    val savedAt: Long = System.currentTimeMillis()
)
