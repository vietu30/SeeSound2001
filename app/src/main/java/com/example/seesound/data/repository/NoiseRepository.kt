package com.example.seesound.data.repository

import com.example.seesound.data.local.dao.NoiseDao
import com.example.seesound.data.local.entity.NoiseRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoiseRepository @Inject constructor(
    private val noiseDao: NoiseDao
) {
    val allRecords: Flow<List<NoiseRecord>> = noiseDao.getAll()

    suspend fun insertRecord(record: NoiseRecord) {
        noiseDao.insert(record)
    }

    suspend fun saveReading(dbValue: Double) {
        val record = NoiseRecord(
            timestamp = System.currentTimeMillis(),
            dbValue = dbValue
        )
        noiseDao.insert(record)
    }

    suspend fun deleteAllRecords() {
        noiseDao.deleteAll()
    }
}
