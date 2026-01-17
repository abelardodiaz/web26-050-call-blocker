package com.callblocker.data.repository

import com.callblocker.data.local.dao.SettingsDao
import com.callblocker.data.local.entity.SettingsEntity
import com.callblocker.domain.model.Settings
import com.callblocker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<Settings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toDomain() ?: Settings()
        }
    }

    override suspend fun updateSettings(settings: Settings) {
        settingsDao.insert(SettingsEntity.fromDomain(settings))
    }

    override suspend fun setBlockingEnabled(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setBlockingEnabled(enabled)
    }

    override suspend fun setBlockUnknownNumbers(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setBlockUnknownNumbers(enabled)
    }

    override suspend fun setBlockPrivateNumbers(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setBlockPrivateNumbers(enabled)
    }

    override suspend fun setShowNotifications(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setShowNotifications(enabled)
    }

    private suspend fun ensureSettingsExist() {
        settingsDao.insert(SettingsEntity())
    }
}
