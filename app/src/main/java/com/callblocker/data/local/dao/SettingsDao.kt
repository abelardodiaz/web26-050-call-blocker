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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
}
