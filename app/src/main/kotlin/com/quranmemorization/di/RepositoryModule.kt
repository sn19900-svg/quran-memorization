package com.quranmemorization.di

import com.quranmemorization.data.repository.QuranRepositoryImpl
import com.quranmemorization.domain.repository.QuranRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule — binds domain interfaces to their data-layer implementations.
 *
 * Using @Binds instead of @Provides avoids an unnecessary wrapper function
 * and lets Hilt inline the binding at compile time.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuranRepository(impl: QuranRepositoryImpl): QuranRepository
}
