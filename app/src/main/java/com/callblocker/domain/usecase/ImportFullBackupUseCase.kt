package com.callblocker.domain.usecase

import android.content.Context
import android.net.Uri
import com.callblocker.data.backup.BackupEncryption
import com.callblocker.data.backup.BackupJsonSerializer
import com.callblocker.domain.model.BackupData
import com.callblocker.domain.model.ImportResult
import com.callblocker.domain.repository.BlockedCallRepository
import com.callblocker.domain.repository.BlockedNumberRepository
import com.callblocker.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case para importar backup completo.
 * Soporta format_version 1 (legacy: solo numeros) y 2 (completo).
 * Realiza merge inteligente: agrega nuevos, mantiene existentes.
 */
class ImportFullBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedNumberRepository: BlockedNumberRepository,
    private val blockedCallRepository: BlockedCallRepository,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Verifica si un archivo de backup esta encriptado.
     *
     * @param uri Uri del archivo a verificar
     * @return true si el backup esta encriptado
     */
    suspend fun isBackupEncrypted(uri: Uri): Boolean {
        return try {
            val bytes = readBytesFromUri(uri)
            BackupEncryption.isEncrypted(bytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lee los metadatos del backup sin importar.
     * Util para preview antes de importar.
     *
     * @param uri Uri del archivo
     * @param password Contrasena si esta encriptado
     * @return BackupData con los metadatos y datos
     */
    suspend fun previewBackup(uri: Uri, password: String? = null): Result<BackupData> {
        return try {
            val bytes = readBytesFromUri(uri)
            val json = decryptIfNeeded(bytes, password)
            val backupData = BackupJsonSerializer.deserialize(json)
            Result.success(backupData)
        } catch (e: javax.crypto.AEADBadTagException) {
            Result.failure(Exception("Contrasena incorrecta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importa backup completo con merge inteligente.
     *
     * @param uri Uri del archivo de backup
     * @param password Contrasena si el backup esta encriptado
     * @return Result con ImportResult o error
     */
    suspend fun execute(uri: Uri, password: String? = null): Result<ImportResult> {
        return try {
            // Leer archivo
            val bytes = readBytesFromUri(uri)

            // Desencriptar si es necesario
            val json = decryptIfNeeded(bytes, password)

            // Parsear
            val backupData = BackupJsonSerializer.deserialize(json)

            // Hacer merge inteligente
            val result = mergeData(backupData)

            Result.success(result)
        } catch (e: javax.crypto.AEADBadTagException) {
            Result.failure(Exception("Contrasena incorrecta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readBytesFromUri(uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: throw Exception("No se pudo leer el archivo")
    }

    private fun decryptIfNeeded(bytes: ByteArray, password: String?): String {
        return if (BackupEncryption.isEncrypted(bytes)) {
            if (password == null) {
                throw Exception("Este backup esta protegido con contrasena")
            }
            BackupEncryption.decrypt(bytes, password)
        } else {
            String(bytes, Charsets.UTF_8)
        }
    }

    private suspend fun mergeData(backupData: BackupData): ImportResult {
        var numbersImported = 0
        var numbersSkipped = 0
        var callsImported = 0
        var settingsRestored = false

        // Merge blocked numbers
        val existingNumbers = blockedNumberRepository.getAllBlockedNumbers().first()
        val existingPhoneNumbers = existingNumbers.map { it.phoneNumber }.toSet()

        for (number in backupData.blockedNumbers) {
            if (number.phoneNumber !in existingPhoneNumbers) {
                blockedNumberRepository.addBlockedNumber(number)
                numbersImported++
            } else {
                numbersSkipped++
            }
        }

        // Import all blocked calls (historial no duplica por timestamp unico)
        for (call in backupData.blockedCalls) {
            blockedCallRepository.addBlockedCall(call)
            callsImported++
        }

        // Restore settings if present
        backupData.settings?.let { settings ->
            settingsRepository.updateSettings(settings)
            settingsRestored = true
        }

        return ImportResult(
            numbersImported = numbersImported,
            numbersSkipped = numbersSkipped,
            callsImported = callsImported,
            settingsRestored = settingsRestored
        )
    }
}
