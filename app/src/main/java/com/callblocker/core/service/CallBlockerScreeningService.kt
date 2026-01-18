package com.callblocker.core.service

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.callblocker.domain.model.BlockReason
import com.callblocker.domain.model.BlockedCall
import com.callblocker.domain.repository.BlockedCallRepository
import com.callblocker.domain.repository.BlockedNumberRepository
import com.callblocker.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallBlockerScreeningService : CallScreeningService() {

    @Inject
    lateinit var blockedNumberRepository: BlockedNumberRepository

    @Inject
    lateinit var blockedCallRepository: BlockedCallRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        Log.d(TAG, "=== LLAMADA ENTRANTE ===")
        Log.d(TAG, "Numero: $phoneNumber")
        Log.d(TAG, "API Level: ${android.os.Build.VERSION.SDK_INT}")

        serviceScope.launch {
            val settings = settingsRepository.getSettings().first()

            Log.d(TAG, "Settings cargados:")
            Log.d(TAG, "  - isBlockingEnabled: ${settings.isBlockingEnabled}")
            Log.d(TAG, "  - persistentServiceEnabled: ${settings.persistentServiceEnabled}")

            if (!settings.isBlockingEnabled) {
                Log.d(TAG, "RESULTADO: Bloqueo deshabilitado globalmente -> PERMITIR")
                allowCall(callDetails)
                return@launch
            }

            val blockReason = determineBlockReason(phoneNumber, settings)
            Log.d(TAG, "BlockReason: $blockReason")

            if (blockReason != null) {
                Log.d(TAG, "RESULTADO: Numero bloqueado por $blockReason -> BLOQUEAR")
                blockCall(callDetails, phoneNumber, blockReason)
            } else {
                Log.d(TAG, "RESULTADO: Numero no esta en lista -> PERMITIR")
                allowCall(callDetails)
            }
        }
    }

    companion object {
        private const val TAG = "CallBlockerScreening"
    }

    private suspend fun determineBlockReason(
        phoneNumber: String,
        settings: com.callblocker.domain.model.Settings
    ): BlockReason? {
        Log.d(TAG, "determineBlockReason: phoneNumber='$phoneNumber'")

        if (phoneNumber.isEmpty() && settings.blockPrivateNumbers) {
            Log.d(TAG, "determineBlockReason: Numero privado bloqueado")
            return BlockReason.PRIVATE_NUMBER
        }

        // Normalizar el numero (quitar codigo de pais +52, +1, etc.)
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        Log.d(TAG, "determineBlockReason: normalizedNumber='$normalizedNumber'")

        val isBlocked = blockedNumberRepository.isNumberBlocked(normalizedNumber)
        Log.d(TAG, "determineBlockReason: isNumberBlocked('$normalizedNumber') = $isBlocked")

        if (isBlocked) {
            return BlockReason.BLOCK_LIST
        }

        return null
    }

    /**
     * Normaliza un numero de telefono quitando el codigo de pais.
     * +524441234567 -> 4441234567
     * 4441234567 -> 4441234567
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Quitar todo excepto digitos
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")

        // Si tiene mas de 10 digitos y empieza con 52 (Mexico), quitar el 52
        if (digitsOnly.length > 10 && digitsOnly.startsWith("52")) {
            val normalized = digitsOnly.substring(2)
            Log.d(TAG, "normalizePhoneNumber: Removido codigo MX 52: $phoneNumber -> $normalized")
            return normalized
        }

        // Si tiene mas de 10 digitos y empieza con 1 (USA/Canada), quitar el 1
        if (digitsOnly.length > 10 && digitsOnly.startsWith("1")) {
            val normalized = digitsOnly.substring(1)
            Log.d(TAG, "normalizePhoneNumber: Removido codigo US/CA 1: $phoneNumber -> $normalized")
            return normalized
        }

        return digitsOnly
    }

    private suspend fun blockCall(
        callDetails: Call.Details,
        phoneNumber: String,
        reason: BlockReason
    ) {
        // Primero guardar la llamada bloqueada sin SIM info
        val blockedCall = BlockedCall(
            phoneNumber = phoneNumber,
            reason = reason
        )
        val blockedCallId = blockedCallRepository.addBlockedCall(blockedCall)

        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)

        // Despues de bloquear, intentar obtener SIM del call log
        // Delay para dar tiempo al sistema de registrar la llamada
        delay(1500)
        updateBlockedCallWithSimInfo(blockedCallId, phoneNumber)
    }

    /**
     * Lee el call log del sistema para obtener que SIM recibio la llamada bloqueada
     * y actualiza el registro en nuestra base de datos.
     */
    private suspend fun updateBlockedCallWithSimInfo(blockedCallId: Long, phoneNumber: String) {
        try {
            val hasCallLogPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasCallLogPermission) {
                Log.w(TAG, "updateBlockedCallWithSimInfo: Sin permiso READ_CALL_LOG")
                return
            }

            // Consultar el call log por el numero bloqueado recientemente
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                "subscription_id"  // Campo no documentado pero disponible
            )

            // Buscar llamadas del mismo numero en los ultimos 30 segundos
            val thirtySecondsAgo = System.currentTimeMillis() - 30000
            val selection = "${CallLog.Calls.DATE} > ?"
            val selectionArgs = arrayOf(thirtySecondsAgo.toString())

            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val subIdIndex = it.getColumnIndex("subscription_id")

                while (it.moveToNext()) {
                    val logNumber = it.getString(numberIndex) ?: continue

                    // Comparar numeros (normalizando)
                    if (numbersMatch(logNumber, phoneNumber)) {
                        val subscriptionId = if (subIdIndex >= 0) it.getInt(subIdIndex) else -1
                        Log.d(TAG, "updateBlockedCallWithSimInfo: Encontrado en call log - subId=$subscriptionId")

                        if (subscriptionId > 0) {
                            // Mapear subscriptionId a simSlot (0 o 1)
                            val simSlot = getSimSlotFromSubscriptionId(subscriptionId)
                            if (simSlot != null) {
                                Log.d(TAG, "updateBlockedCallWithSimInfo: Actualizando registro con simSlot=$simSlot")
                                blockedCallRepository.updateSimSlot(blockedCallId, simSlot)
                            }
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateBlockedCallWithSimInfo: Error", e)
        }
    }

    /**
     * Mapea un subscriptionId al slot fisico de la SIM (0 o 1).
     */
    private fun getSimSlotFromSubscriptionId(subscriptionId: Int): Int? {
        try {
            val hasPhoneState = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPhoneState) {
                Log.w(TAG, "getSimSlotFromSubscriptionId: Sin permiso READ_PHONE_STATE")
                return null
            }

            val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE)
                as? SubscriptionManager ?: return null

            val subInfo = subscriptionManager.activeSubscriptionInfoList?.find {
                it.subscriptionId == subscriptionId
            }

            return subInfo?.simSlotIndex
        } catch (e: Exception) {
            Log.e(TAG, "getSimSlotFromSubscriptionId: Error", e)
            return null
        }
    }

    /**
     * Compara dos numeros de telefono ignorando formato.
     */
    private fun numbersMatch(number1: String, number2: String): Boolean {
        val clean1 = number1.replace(Regex("[^0-9]"), "")
        val clean2 = number2.replace(Regex("[^0-9]"), "")

        // Comparar ultimos 10 digitos (para ignorar codigo de pais)
        val suffix1 = clean1.takeLast(10)
        val suffix2 = clean2.takeLast(10)

        return suffix1 == suffix2 && suffix1.length >= 7
    }

    private fun allowCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .build()

        respondToCall(callDetails, response)
    }
}
