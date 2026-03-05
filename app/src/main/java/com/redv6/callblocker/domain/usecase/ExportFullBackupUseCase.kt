package com.redv6.callblocker.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.redv6.callblocker.BuildConfig
import com.redv6.callblocker.data.backup.BackupEncryption
import com.redv6.callblocker.data.backup.BackupJsonSerializer
import com.redv6.callblocker.domain.model.BackupCounts
import com.redv6.callblocker.domain.model.BackupData
import com.redv6.callblocker.domain.model.BackupMetadata
import com.redv6.callblocker.domain.repository.BlockedCallRepository
import com.redv6.callblocker.domain.repository.BlockedNumberRepository
import com.redv6.callblocker.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use case para exportar backup completo de la app.
 * Incluye: blocked_numbers, blocked_calls, settings.
 * Soporta encriptacion opcional con contrasena.
 */
class ExportFullBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedNumberRepository: BlockedNumberRepository,
    private val blockedCallRepository: BlockedCallRepository,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Exporta backup completo.
     *
     * @param password Si no es null, el backup se encripta con AES-256-GCM
     * @return Result con la ruta del archivo o error
     */
    suspend fun execute(password: String? = null): Result<String> {
        return try {
            // Recopilar datos de las 3 tablas
            val blockedNumbers = blockedNumberRepository.getAllBlockedNumbers().first()
            val blockedCalls = blockedCallRepository.getAllBlockedCalls().first()
            val settings = settingsRepository.getSettings().first()

            // Crear backup data con metadatos
            val backupData = BackupData(
                metadata = BackupMetadata(
                    formatVersion = BackupMetadata.CURRENT_FORMAT_VERSION,
                    appVersion = BuildConfig.VERSION_NAME,
                    createdAt = System.currentTimeMillis(),
                    deviceModel = Build.MODEL,
                    androidVersion = Build.VERSION.SDK_INT,
                    encrypted = password != null,
                    counts = BackupCounts(
                        blockedNumbers = blockedNumbers.size,
                        blockedCalls = blockedCalls.size,
                        hasSettings = true
                    )
                ),
                blockedNumbers = blockedNumbers,
                blockedCalls = blockedCalls,
                settings = settings
            )

            // Serializar a JSON
            val json = BackupJsonSerializer.serialize(backupData)

            // Encriptar si hay contrasena
            val content: ByteArray = if (password != null) {
                BackupEncryption.encrypt(json, password)
            } else {
                json.toByteArray(Charsets.UTF_8)
            }

            // Guardar en Downloads
            val filePath = saveToDownloads(content, password != null)

            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveToDownloads(content: ByteArray, encrypted: Boolean): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val extension = if (encrypted) "cbbk" else "json" // Custom extension for encrypted
        val fileName = "CallBlocker_fullbackup_$timestamp.$extension"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(fileName, content, encrypted)
        } else {
            saveToDownloadsLegacy(fileName, content)
        }
    }

    private fun saveWithMediaStore(fileName: String, content: ByteArray, encrypted: Boolean): String {
        val mimeType = if (encrypted) "application/octet-stream" else "application/json"

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("No se pudo crear el archivo")

        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        return "Downloads/$fileName"
    }

    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(fileName: String, content: ByteArray): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            outputStream.write(content)
        }

        return file.absolutePath
    }
}
