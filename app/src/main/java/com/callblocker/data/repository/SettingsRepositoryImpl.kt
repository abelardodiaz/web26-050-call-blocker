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

    override suspend fun setEnabledSimSlots(simSlots: Set<Int>) {
        ensureSettingsExist()
        settingsDao.setEnabledSimSlots(simSlots.joinToString(","))
    }

    override suspend fun setPersistentServiceEnabled(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setPersistentServiceEnabled(enabled)
    }

    override suspend fun isPersistentServiceEnabled(): Boolean {
        return settingsDao.isPersistentServiceEnabled() ?: false
    }

    override suspend fun setAppLanguage(language: String) {
        ensureSettingsExist()
        settingsDao.setAppLanguage(language)
    }

    override suspend fun getAppLanguage(): String {
        return settingsDao.getAppLanguage() ?: "system"
    }

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setDeveloperModeEnabled(enabled)
    }

    override suspend fun setDevSimDetectionByFormat(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setDevSimDetectionByFormat(enabled)
    }

    override suspend fun setDevBlockSim1(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setDevBlockSim1(enabled)
    }

    override suspend fun setDevBlockSim2(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.setDevBlockSim2(enabled)
    }

    private suspend fun ensureSettingsExist() {
        settingsDao.insert(SettingsEntity())
    }
}
