package com.callblocker.core.service

import android.Manifest
import android.content.pm.PackageManager
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
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
        val subscriptionId = getSubscriptionIdFromCall(callDetails)

        serviceScope.launch {
            val settings = settingsRepository.getSettings().first()

            if (!settings.isBlockingEnabled) {
                allowCall(callDetails)
                return@launch
            }

            // Verificar si el bloqueo esta habilitado para esta SIM
            if (!isBlockingEnabledForSim(subscriptionId, settings.enabledSimSlots)) {
                allowCall(callDetails)
                return@launch
            }

            val blockReason = determineBlockReason(phoneNumber, settings)

            if (blockReason != null) {
                blockCall(callDetails, phoneNumber, blockReason)
            } else {
                allowCall(callDetails)
            }
        }
    }

    /**
     * Obtiene el subscriptionId de la llamada entrante.
     * Retorna -1 si no se puede determinar.
     *
     * La logica busca el subscriptionId en el siguiente orden:
     * 1. Intenta parsear el ID del PhoneAccountHandle como entero
     * 2. Busca en las subscripciones activas por coincidencia de ICC ID
     * 3. Retorna -1 si no se puede determinar
     */
    private fun getSubscriptionIdFromCall(callDetails: Call.Details): Int {
        return try {
            val phoneAccountHandle: PhoneAccountHandle? = callDetails.accountHandle
            if (phoneAccountHandle == null) {
                Log.d(TAG, "PhoneAccountHandle is null")
                return -1
            }

            val handleId = phoneAccountHandle.id
            Log.d(TAG, "PhoneAccountHandle.id: $handleId")

            // Metodo 1: Intentar parsear como entero (funciona en algunos dispositivos)
            val directId = handleId.toIntOrNull()
            if (directId != null && isValidSubscriptionId(directId)) {
                Log.d(TAG, "SubscriptionId from direct parse: $directId")
                return directId
            }

            // Metodo 2: Buscar por ICC ID en las subscripciones activas
            // El ID del PhoneAccountHandle suele ser el ICC ID (SIM card identifier)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE)
                    as? SubscriptionManager

                subscriptionManager?.activeSubscriptionInfoList?.forEach { subInfo ->
                    // Comparar por ICC ID
                    if (subInfo.iccId == handleId || subInfo.iccId?.endsWith(handleId) == true) {
                        Log.d(TAG, "SubscriptionId from ICC ID match: ${subInfo.subscriptionId}")
                        return subInfo.subscriptionId
                    }
                }
            }

            Log.d(TAG, "Could not determine subscriptionId, returning -1")
            -1
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subscriptionId", e)
            -1
        }
    }

    /**
     * Verifica si un subscriptionId es valido consultando SubscriptionManager.
     */
    private fun isValidSubscriptionId(subscriptionId: Int): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return try {
            val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE)
                as? SubscriptionManager ?: return false

            subscriptionManager.activeSubscriptionInfoList?.any {
                it.subscriptionId == subscriptionId
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "CallBlockerScreening"
    }

    /**
     * Verifica si el bloqueo esta habilitado para una SIM especifica.
     * Si enabledSimSlots esta vacio, bloquea en todas las SIMs (comportamiento legacy).
     * Si subscriptionId es -1 (desconocido), bloquea por seguridad.
     */
    private fun isBlockingEnabledForSim(subscriptionId: Int, enabledSimSlots: Set<Int>): Boolean {
        // Si no hay SIMs configuradas, bloquear en todas (comportamiento por defecto)
        if (enabledSimSlots.isEmpty()) {
            return true
        }
        // Si no se pudo determinar la SIM, bloquear por seguridad
        if (subscriptionId == -1) {
            return true
        }
        // Verificar si esta SIM tiene bloqueo habilitado
        return enabledSimSlots.contains(subscriptionId)
    }

    private suspend fun determineBlockReason(
        phoneNumber: String,
        settings: com.callblocker.domain.model.Settings
    ): BlockReason? {
        if (phoneNumber.isEmpty() && settings.blockPrivateNumbers) {
            return BlockReason.PRIVATE_NUMBER
        }

        if (blockedNumberRepository.isNumberBlocked(phoneNumber)) {
            return BlockReason.BLOCK_LIST
        }

        return null
    }

    private suspend fun blockCall(
        callDetails: Call.Details,
        phoneNumber: String,
        reason: BlockReason
    ) {
        blockedCallRepository.addBlockedCall(
            BlockedCall(
                phoneNumber = phoneNumber,
                reason = reason
            )
        )

        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }

    private fun allowCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .build()

        respondToCall(callDetails, response)
    }
}
