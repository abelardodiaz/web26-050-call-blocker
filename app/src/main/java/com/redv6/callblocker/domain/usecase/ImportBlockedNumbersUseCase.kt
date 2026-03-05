package com.redv6.callblocker.domain.usecase

import android.content.Context
import android.net.Uri
import com.redv6.callblocker.domain.model.BlockedNumber
import com.redv6.callblocker.domain.repository.BlockedNumberRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import javax.inject.Inject

/**
 * Use case para importar numeros bloqueados desde un archivo JSON.
 */
class ImportBlockedNumbersUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedNumberRepository: BlockedNumberRepository
) {

    /**
     * Resultado de la importacion.
     *
     * @param imported Cantidad de numeros importados
     * @param skipped Cantidad de numeros omitidos (ya existian)
     * @param errors Cantidad de errores
     */
    data class ImportResult(
        val imported: Int,
        val skipped: Int,
        val errors: Int
    )

    /**
     * Importa numeros bloqueados desde un archivo JSON.
     *
     * @param uri URI del archivo a importar
     * @return Result con el resultado de la importacion o error
     */
    suspend fun execute(uri: Uri): Result<ImportResult> {
        return try {
            val jsonContent = readFileContent(uri)
            val blockedNumbers = parseJson(jsonContent)
            val result = importNumbers(blockedNumbers)

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readFileContent(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().readText()
        } ?: throw Exception("No se pudo leer el archivo")
    }

    private fun parseJson(jsonContent: String): List<BlockedNumber> {
        val jsonObject = JSONObject(jsonContent)

        val version = jsonObject.optInt("version", 1)
        if (version > ExportBlockedNumbersUseCase.EXPORT_VERSION) {
            throw Exception("Formato de archivo no soportado (version $version)")
        }

        val numbersArray = jsonObject.getJSONArray("blocked_numbers")
        val blockedNumbers = mutableListOf<BlockedNumber>()

        for (i in 0 until numbersArray.length()) {
            val numberJson = numbersArray.getJSONObject(i)
            val phoneNumber = numberJson.getString("phoneNumber")

            if (phoneNumber.isNotBlank()) {
                blockedNumbers.add(
                    BlockedNumber(
                        phoneNumber = phoneNumber,
                        label = numberJson.optString("label").takeIf { it.isNotBlank() },
                        isPrefix = numberJson.optBoolean("isPrefix", false),
                        createdAt = numberJson.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        return blockedNumbers
    }

    private suspend fun importNumbers(blockedNumbers: List<BlockedNumber>): ImportResult {
        var imported = 0
        var skipped = 0
        var errors = 0

        // Obtener numeros existentes para evitar duplicados
        val existingNumbers = blockedNumberRepository.getAllBlockedNumbers().first()
            .map { it.phoneNumber }
            .toSet()

        blockedNumbers.forEach { number ->
            try {
                if (existingNumbers.contains(number.phoneNumber)) {
                    skipped++
                } else {
                    blockedNumberRepository.addBlockedNumber(number)
                    imported++
                }
            } catch (e: Exception) {
                errors++
            }
        }

        return ImportResult(imported, skipped, errors)
    }
}
