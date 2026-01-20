package com.example.seesound.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.seesound.data.local.entity.NoiseRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface NoiseDao {
    @Insert
    suspend fun insert(record: NoiseRecord)

    @Query("SELECT * FROM noise_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NoiseRecord>>

    @Query("DELETE FROM noise_records")
    suspend fun deleteAll()
}
