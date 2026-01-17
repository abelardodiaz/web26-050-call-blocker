package com.callblocker.core.di

import com.callblocker.data.repository.BlockedCallRepositoryImpl
import com.callblocker.data.repository.BlockedNumberRepositoryImpl
import com.callblocker.data.repository.SettingsRepositoryImpl
import com.callblocker.domain.repository.BlockedCallRepository
import com.callblocker.domain.repository.BlockedNumberRepository
import com.callblocker.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBlockedNumberRepository(
        impl: BlockedNumberRepositoryImpl
    ): BlockedNumberRepository

    @Binds
    @Singleton
    abstract fun bindBlockedCallRepository(
        impl: BlockedCallRepositoryImpl
    ): BlockedCallRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
