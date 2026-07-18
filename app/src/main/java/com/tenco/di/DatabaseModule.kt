package com.tenco.di

import android.content.Context
import androidx.room.Room
import com.tenco.data.local.TencoDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): TencoDatabase =
        Room.databaseBuilder(context, TencoDatabase::class.java, TencoDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    // Seeding is handled deterministically via TencoRepository.ensureSeeded()
    // (called on app start and awaited before the vendor one-shot read).
}
