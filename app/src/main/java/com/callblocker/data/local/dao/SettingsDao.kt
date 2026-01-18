package com.callblocker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callblocker.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SettingsEntity)

    @Query("UPDATE settings SET isBlockingEnabled = :enabled WHERE id = 1")
    suspend fun setBlockingEnabled(enabled: Boolean)

    @Query("UPDATE settings SET blockUnknownNumbers = :enabled WHERE id = 1")
    suspend fun setBlockUnknownNumbers(enabled: Boolean)

    @Query("UPDATE settings SET blockPrivateNumbers = :enabled WHERE id = 1")
    suspend fun setBlockPrivateNumbers(enabled: Boolean)

    @Query("UPDATE settings SET showNotifications = :enabled WHERE id = 1")
    suspend fun setShowNotifications(enabled: Boolean)

    @Query("UPDATE settings SET enabled_sim_slots = :simSlots WHERE id = 1")
    suspend fun setEnabledSimSlots(simSlots: String)

    @Query("UPDATE settings SET persistent_service_enabled = :enabled WHERE id = 1")
    suspend fun setPersistentServiceEnabled(enabled: Boolean)

    @Query("SELECT persistent_service_enabled FROM settings WHERE id = 1")
    suspend fun isPersistentServiceEnabled(): Boolean?

    @Query("UPDATE settings SET app_language = :language WHERE id = 1")
    suspend fun setAppLanguage(language: String)

    @Query("SELECT app_language FROM settings WHERE id = 1")
    suspend fun getAppLanguage(): String?

    // Developer mode queries
    @Query("UPDATE settings SET developer_mode_enabled = :enabled WHERE id = 1")
    suspend fun setDeveloperModeEnabled(enabled: Boolean)

    @Query("UPDATE settings SET dev_sim_detection_by_format = :enabled WHERE id = 1")
    suspend fun setDevSimDetectionByFormat(enabled: Boolean)

    @Query("UPDATE settings SET dev_block_sim1 = :enabled WHERE id = 1")
    suspend fun setDevBlockSim1(enabled: Boolean)

    @Query("UPDATE settings SET dev_block_sim2 = :enabled WHERE id = 1")
    suspend fun setDevBlockSim2(enabled: Boolean)
}
