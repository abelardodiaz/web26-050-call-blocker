package com.callblocker.domain.model

/**
 * Modelo completo de backup incluyendo todos los datos de la app.
 */
data class BackupData(
    val metadata: BackupMetadata,
    val blockedNumbers: List<BlockedNumber>,
    val blockedCalls: List<BlockedCall>,
    val settings: Settings?
)

/**
 * Metadatos del archivo de backup.
 */
data class BackupMetadata(
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    val appVersion: String,
    val createdAt: Long,
    val deviceModel: String,
    val androidVersion: Int,
    val encrypted: Boolean = false,
    val counts: BackupCounts
) {
    companion object {
        const val CURRENT_FORMAT_VERSION = 2
    }
}

/**
 * Conteo de elementos en el backup.
 */
data class BackupCounts(
    val blockedNumbers: Int,
    val blockedCalls: Int,
    val hasSettings: Boolean
)

/**
 * Resultado de una operacion de importacion.
 */
data class ImportResult(
    val numbersImported: Int,
    val numbersSkipped: Int,
    val callsImported: Int,
    val settingsRestored: Boolean
)
