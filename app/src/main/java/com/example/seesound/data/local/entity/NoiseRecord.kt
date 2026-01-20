package com.example.seesound.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "noise_records")
data class NoiseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val dbValue: Double
)
