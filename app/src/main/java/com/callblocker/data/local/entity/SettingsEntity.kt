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
    val enabledSimSlots: String = "" // Comma-separated subscription IDs
) {
    fun toDomain(): Settings = Settings(
        isBlockingEnabled = isBlockingEnabled,
        blockUnknownNumbers = blockUnknownNumbers,
        blockPrivateNumbers = blockPrivateNumbers,
        showNotifications = showNotifications,
        enabledSimSlots = parseSimSlots(enabledSimSlots)
    )

    companion object {
        fun fromDomain(domain: Settings): SettingsEntity = SettingsEntity(
            isBlockingEnabled = domain.isBlockingEnabled,
            blockUnknownNumbers = domain.blockUnknownNumbers,
            blockPrivateNumbers = domain.blockPrivateNumbers,
            showNotifications = domain.showNotifications,
            enabledSimSlots = domain.enabledSimSlots.joinToString(",")
        )

        private fun parseSimSlots(value: String): Set<Int> {
            if (value.isBlank()) return emptySet()
            return value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }
    }
}
