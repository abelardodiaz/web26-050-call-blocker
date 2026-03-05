package com.redv6.callblocker.core.di

import com.redv6.callblocker.data.repository.BlockedCallRepositoryImpl
import com.redv6.callblocker.data.repository.BlockedNumberRepositoryImpl
import com.redv6.callblocker.data.repository.SettingsRepositoryImpl
import com.redv6.callblocker.domain.repository.BlockedCallRepository
import com.redv6.callblocker.domain.repository.BlockedNumberRepository
import com.redv6.callblocker.domain.repository.SettingsRepository
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
