package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    @Query("SELECT * FROM triggers ORDER BY isDefault DESC, displayName ASC")
    fun getAllTriggers(): Flow<List<TriggerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: TriggerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriggers(triggers: List<TriggerEntity>)

    @Delete
    suspend fun deleteTrigger(trigger: TriggerEntity)

    @Query("SELECT COUNT(*) FROM triggers")
    suspend fun getCount(): Int
}
