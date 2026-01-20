package com.example.seesound.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.seesound.data.local.dao.NoiseDao
import com.example.seesound.data.local.entity.NoiseRecord

@Database(
    entities = [NoiseRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noiseDao(): NoiseDao
}
