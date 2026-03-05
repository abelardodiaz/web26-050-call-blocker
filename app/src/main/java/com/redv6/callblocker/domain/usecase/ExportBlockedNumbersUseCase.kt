package com.redv6.callblocker.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.redv6.callblocker.domain.model.BlockedNumber
import com.redv6.callblocker.domain.repository.BlockedNumberRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use case para exportar la lista de numeros bloqueados a un archivo JSON.
 *
 * El archivo se guarda en la carpeta Downloads del dispositivo.
 */
class ExportBlockedNumbersUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedNumberRepository: BlockedNumberRepository
) {

    /**
     * Exporta la lista de numeros bloqueados a JSON.
     *
     * @return Result con la ruta del archivo o error
     */
    suspend fun execute(): Result<String> {
        return try {
            val blockedNumbers = blockedNumberRepository.getAllBlockedNumbers().first()

            if (blockedNumbers.isEmpty()) {
                return Result.failure(Exception("No hay numeros bloqueados para exportar"))
            }

            val json = createJsonExport(blockedNumbers)
            val filePath = saveToDownloads(json)

            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createJsonExport(blockedNumbers: List<BlockedNumber>): String {
        val jsonObject = JSONObject().apply {
            put("version", EXPORT_VERSION)
            put("exported_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
            put("app_version", "0.2.3")
            put("total_count", blockedNumbers.size)

            val numbersArray = JSONArray()
            blockedNumbers.forEach { number ->
                numbersArray.put(JSONObject().apply {
                    put("phoneNumber", number.phoneNumber)
                    put("label", number.label ?: "")
                    put("isPrefix", number.isPrefix)
                    put("createdAt", number.createdAt)
                })
            }
            put("blocked_numbers", numbersArray)
        }

        return jsonObject.toString(2) // Formatted with 2-space indentation
    }

    private fun saveToDownloads(jsonContent: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "CallBlocker_backup_$timestamp.json"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ usar MediaStore
            saveWithMediaStore(fileName, jsonContent)
        } else {
            // Android 9 usar acceso directo
            saveToDownloadsLegacy(fileName, jsonContent)
        }
    }

    private fun saveWithMediaStore(fileName: String, content: String): String {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("No se pudo crear el archivo")

        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        return "Downloads/$fileName"
    }

    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(fileName: String, content: String): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }

        return file.absolutePath
    }

    companion object {
        const val EXPORT_VERSION = 1
    }
}
