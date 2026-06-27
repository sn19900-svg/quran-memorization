package com.quranmemorization.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.quranmemorization.data.local.QuranDatabase
import com.quranmemorization.data.local.dao.MistakeLogDao
import com.quranmemorization.data.local.dao.QuranScheduleDao
import com.quranmemorization.data.local.dao.UserProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataStore extension property — one instance per application process
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quran_prefs")

/**
 * DatabaseModule — Hilt module that provides Room DB + DAOs + DataStore.
 *
 * All bindings are @Singleton: Room and DataStore are expensive to construct
 * and must be shared across the whole app.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuranDatabase =
        Room.databaseBuilder(
            context,
            QuranDatabase::class.java,
            QuranDatabase.DATABASE_NAME,
        )
        .fallbackToDestructiveMigration()   // replace with proper Migration objects before v2
        .build()

    @Provides
    fun provideScheduleDao(db: QuranDatabase): QuranScheduleDao = db.quranScheduleDao()

    @Provides
    fun provideProgressDao(db: QuranDatabase): UserProgressDao = db.userProgressDao()

    @Provides
    fun provideMistakeDao(db: QuranDatabase): MistakeLogDao = db.mistakeLogDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
