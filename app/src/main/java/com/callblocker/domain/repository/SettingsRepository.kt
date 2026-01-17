package com.callblocker.domain.repository

import com.callblocker.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
    suspend fun setBlockingEnabled(enabled: Boolean)
    suspend fun setBlockUnknownNumbers(enabled: Boolean)
    suspend fun setBlockPrivateNumbers(enabled: Boolean)
    suspend fun setShowNotifications(enabled: Boolean)
    suspend fun setEnabledSimSlots(simSlots: Set<Int>)
}
