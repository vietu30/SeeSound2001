package com.example.seesound.di

import android.content.Context
import androidx.room.Room
import com.example.seesound.data.local.dao.NoiseDao
import com.example.seesound.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "seesound_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoiseDao(database: AppDatabase): NoiseDao {
        return database.noiseDao()
    }
}
