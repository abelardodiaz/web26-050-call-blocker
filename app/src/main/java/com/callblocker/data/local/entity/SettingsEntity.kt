package com.callblocker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callblocker.domain.model.Settings

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val isBlockingEnabled: Boolean = true,
    val blockUnknownNumbers: Boolean = false,
    val blockPrivateNumbers: Boolean = false,
    val showNotifications: Boolean = false,
    @ColumnInfo(name = "enabled_sim_slots", defaultValue = "")
    val enabledSimSlots: String = "", // Comma-separated subscription IDs
    @ColumnInfo(name = "persistent_service_enabled", defaultValue = "0")
    val persistentServiceEnabled: Boolean = false,
    @ColumnInfo(name = "app_language", defaultValue = "system")
    val appLanguage: String = "system",
    // Developer mode columns
    @ColumnInfo(name = "developer_mode_enabled", defaultValue = "0")
    val developerModeEnabled: Boolean = false,
    @ColumnInfo(name = "dev_sim_detection_by_format", defaultValue = "0")
    val devSimDetectionByFormat: Boolean = false,
    @ColumnInfo(name = "dev_block_sim1", defaultValue = "1")
    val devBlockSim1: Boolean = true,
    @ColumnInfo(name = "dev_block_sim2", defaultValue = "1")
    val devBlockSim2: Boolean = true
) {
    fun toDomain(): Settings = Settings(
        isBlockingEnabled = isBlockingEnabled,
        blockUnknownNumbers = blockUnknownNumbers,
        blockPrivateNumbers = blockPrivateNumbers,
        showNotifications = showNotifications,
        enabledSimSlots = parseSimSlots(enabledSimSlots),
        persistentServiceEnabled = persistentServiceEnabled,
        appLanguage = appLanguage,
        developerModeEnabled = developerModeEnabled,
        devSimDetectionByFormat = devSimDetectionByFormat,
        devBlockSim1 = devBlockSim1,
        devBlockSim2 = devBlockSim2
    )

    companion object {
        fun fromDomain(domain: Settings): SettingsEntity = SettingsEntity(
            isBlockingEnabled = domain.isBlockingEnabled,
            blockUnknownNumbers = domain.blockUnknownNumbers,
            blockPrivateNumbers = domain.blockPrivateNumbers,
            showNotifications = domain.showNotifications,
            enabledSimSlots = domain.enabledSimSlots.joinToString(","),
            persistentServiceEnabled = domain.persistentServiceEnabled,
            appLanguage = domain.appLanguage,
            developerModeEnabled = domain.developerModeEnabled,
            devSimDetectionByFormat = domain.devSimDetectionByFormat,
            devBlockSim1 = domain.devBlockSim1,
            devBlockSim2 = domain.devBlockSim2
        )

        private fun parseSimSlots(value: String): Set<Int> {
            if (value.isBlank()) return emptySet()
            return value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }
    }
}
