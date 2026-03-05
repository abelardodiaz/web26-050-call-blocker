package com.redv6.callblocker.data.backup

import com.redv6.callblocker.domain.model.BackupCounts
import com.redv6.callblocker.domain.model.BackupData
import com.redv6.callblocker.domain.model.BackupMetadata
import com.redv6.callblocker.domain.model.BlockedCall
import com.redv6.callblocker.domain.model.BlockedNumber
import com.redv6.callblocker.domain.model.BlockReason
import com.redv6.callblocker.domain.model.Settings
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializador JSON para backup completo de la app.
 * Maneja format_version 1 (legacy) y 2 (completo).
 */
object BackupJsonSerializer {

    /**
     * Serializa BackupData a JSON string.
     */
    fun serialize(backupData: BackupData): String {
        val json = JSONObject().apply {
            // Metadata
            put("metadata", JSONObject().apply {
                put("format_version", backupData.metadata.formatVersion)
                put("app_version", backupData.metadata.appVersion)
                put("created_at", backupData.metadata.createdAt)
                put("device_model", backupData.metadata.deviceModel)
                put("android_version", backupData.metadata.androidVersion)
                put("encrypted", backupData.metadata.encrypted)
                put("counts", JSONObject().apply {
                    put("blocked_numbers", backupData.metadata.counts.blockedNumbers)
                    put("blocked_calls", backupData.metadata.counts.blockedCalls)
                    put("has_settings", backupData.metadata.counts.hasSettings)
                })
            })

            // Blocked numbers
            put("blocked_numbers", JSONArray().apply {
                backupData.blockedNumbers.forEach { number ->
                    put(JSONObject().apply {
                        put("phoneNumber", number.phoneNumber)
                        put("label", number.label ?: "")
                        put("isPrefix", number.isPrefix)
                        put("createdAt", number.createdAt)
                    })
                }
            })

            // Blocked calls
            put("blocked_calls", JSONArray().apply {
                backupData.blockedCalls.forEach { call ->
                    put(JSONObject().apply {
                        put("phoneNumber", call.phoneNumber)
                        put("timestamp", call.timestamp)
                        put("reason", call.reason.name)
                        if (call.simSlot != null) {
                            put("simSlot", call.simSlot)
                        }
                    })
                }
            })

            // Settings
            backupData.settings?.let { settings ->
                put("settings", JSONObject().apply {
                    put("isBlockingEnabled", settings.isBlockingEnabled)
                    put("blockUnknownNumbers", settings.blockUnknownNumbers)
                    put("blockPrivateNumbers", settings.blockPrivateNumbers)
                    put("showNotifications", settings.showNotifications)
                    put("enabledSimSlots", settings.enabledSimSlots.joinToString(","))
                    put("persistentServiceEnabled", settings.persistentServiceEnabled)
                    put("developerModeEnabled", settings.developerModeEnabled)
                    put("devSimDetectionByFormat", settings.devSimDetectionByFormat)
                    put("devBlockSim1", settings.devBlockSim1)
                    put("devBlockSim2", settings.devBlockSim2)
                })
            }
        }

        return json.toString(2) // Pretty print with 2-space indentation
    }

    /**
     * Deserializa JSON string a BackupData.
     * Soporta format_version 1 (legacy) y 2 (completo).
     */
    fun deserialize(jsonString: String): BackupData {
        val json = JSONObject(jsonString)

        // Detectar version del formato
        val formatVersion = detectFormatVersion(json)

        return when (formatVersion) {
            1 -> deserializeV1(json)
            2 -> deserializeV2(json)
            else -> throw IllegalArgumentException("Formato de backup no soportado: v$formatVersion")
        }
    }

    /**
     * Detecta la version del formato del backup.
     */
    fun detectFormatVersion(json: JSONObject): Int {
        // V2 tiene metadata.format_version
        if (json.has("metadata")) {
            val metadata = json.getJSONObject("metadata")
            return metadata.optInt("format_version", 2)
        }
        // V1 tiene "version" en el root
        if (json.has("version")) {
            return json.getInt("version")
        }
        // Default para archivos muy viejos
        return 1
    }

    /**
     * Detecta si el backup esta encriptado.
     */
    fun isEncrypted(json: JSONObject): Boolean {
        if (json.has("metadata")) {
            return json.getJSONObject("metadata").optBoolean("encrypted", false)
        }
        return false
    }

    private fun deserializeV1(json: JSONObject): BackupData {
        // V1 solo tiene blocked_numbers
        val numbersArray = json.getJSONArray("blocked_numbers")
        val blockedNumbers = mutableListOf<BlockedNumber>()

        for (i in 0 until numbersArray.length()) {
            val obj = numbersArray.getJSONObject(i)
            blockedNumbers.add(
                BlockedNumber(
                    phoneNumber = obj.getString("phoneNumber"),
                    label = obj.optString("label", "").takeIf { it.isNotEmpty() },
                    isPrefix = obj.optBoolean("isPrefix", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        return BackupData(
            metadata = BackupMetadata(
                formatVersion = 1,
                appVersion = json.optString("app_version", "unknown"),
                createdAt = System.currentTimeMillis(),
                deviceModel = "unknown",
                androidVersion = 0,
                encrypted = false,
                counts = BackupCounts(
                    blockedNumbers = blockedNumbers.size,
                    blockedCalls = 0,
                    hasSettings = false
                )
            ),
            blockedNumbers = blockedNumbers,
            blockedCalls = emptyList(),
            settings = null
        )
    }

    private fun deserializeV2(json: JSONObject): BackupData {
        val metadataJson = json.getJSONObject("metadata")
        val countsJson = metadataJson.getJSONObject("counts")

        val metadata = BackupMetadata(
            formatVersion = metadataJson.getInt("format_version"),
            appVersion = metadataJson.getString("app_version"),
            createdAt = metadataJson.getLong("created_at"),
            deviceModel = metadataJson.getString("device_model"),
            androidVersion = metadataJson.getInt("android_version"),
            encrypted = metadataJson.optBoolean("encrypted", false),
            counts = BackupCounts(
                blockedNumbers = countsJson.getInt("blocked_numbers"),
                blockedCalls = countsJson.getInt("blocked_calls"),
                hasSettings = countsJson.getBoolean("has_settings")
            )
        )

        // Blocked numbers
        val numbersArray = json.getJSONArray("blocked_numbers")
        val blockedNumbers = mutableListOf<BlockedNumber>()
        for (i in 0 until numbersArray.length()) {
            val obj = numbersArray.getJSONObject(i)
            blockedNumbers.add(
                BlockedNumber(
                    phoneNumber = obj.getString("phoneNumber"),
                    label = obj.optString("label", "").takeIf { it.isNotEmpty() },
                    isPrefix = obj.optBoolean("isPrefix", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Blocked calls
        val callsArray = json.getJSONArray("blocked_calls")
        val blockedCalls = mutableListOf<BlockedCall>()
        for (i in 0 until callsArray.length()) {
            val obj = callsArray.getJSONObject(i)
            blockedCalls.add(
                BlockedCall(
                    phoneNumber = obj.getString("phoneNumber"),
                    timestamp = obj.getLong("timestamp"),
                    reason = try {
                        BlockReason.valueOf(obj.getString("reason"))
                    } catch (e: Exception) {
                        BlockReason.BLOCK_LIST
                    },
                    simSlot = if (obj.has("simSlot")) obj.getInt("simSlot") else null
                )
            )
        }

        // Settings
        val settings = if (json.has("settings")) {
            val settingsJson = json.getJSONObject("settings")
            Settings(
                isBlockingEnabled = settingsJson.optBoolean("isBlockingEnabled", true),
                blockUnknownNumbers = settingsJson.optBoolean("blockUnknownNumbers", false),
                blockPrivateNumbers = settingsJson.optBoolean("blockPrivateNumbers", false),
                showNotifications = settingsJson.optBoolean("showNotifications", false),
                enabledSimSlots = settingsJson.optString("enabledSimSlots", "")
                    .split(",")
                    .filter { it.isNotEmpty() }
                    .mapNotNull { it.toIntOrNull() }
                    .toSet(),
                persistentServiceEnabled = settingsJson.optBoolean("persistentServiceEnabled", false),
                developerModeEnabled = settingsJson.optBoolean("developerModeEnabled", false),
                devSimDetectionByFormat = settingsJson.optBoolean("devSimDetectionByFormat", false),
                devBlockSim1 = settingsJson.optBoolean("devBlockSim1", true),
                devBlockSim2 = settingsJson.optBoolean("devBlockSim2", true)
            )
        } else null

        return BackupData(
            metadata = metadata,
            blockedNumbers = blockedNumbers,
            blockedCalls = blockedCalls,
            settings = settings
        )
    }
}
