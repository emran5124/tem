package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilledResultDao {
    @Query("SELECT * FROM filled_results ORDER BY createdAt DESC")
    fun getAllFilledResults(): Flow<List<FilledResultEntity>>

    @Query("SELECT * FROM filled_results WHERE id = :id")
    fun getFilledResultById(id: Int): Flow<FilledResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilledResult(result: FilledResultEntity): Long

    @Update
    suspend fun updateFilledResult(result: FilledResultEntity)

    @Delete
    suspend fun deleteFilledResult(result: FilledResultEntity)
}
