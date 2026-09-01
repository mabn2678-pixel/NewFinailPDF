package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedAudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cachedAudio: CachedAudio)

    @Query("SELECT * FROM cached_audio WHERE sourceUrl = :url LIMIT 1")
    suspend fun getBySourceUrl(url: String): CachedAudio?

    @Query("SELECT * FROM cached_audio WHERE savedAt < :cutoffTime")
    suspend fun getExpiredEntries(cutoffTime: Long): List<CachedAudio>

    @Query("DELETE FROM cached_audio WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM cached_audio ORDER BY savedAt DESC")
    fun getAll(): Flow<List<CachedAudio>>
}
