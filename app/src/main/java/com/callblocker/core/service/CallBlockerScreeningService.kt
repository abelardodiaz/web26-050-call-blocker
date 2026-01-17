package com.callblocker.core.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
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
     */
    private fun getSubscriptionIdFromCall(callDetails: Call.Details): Int {
        return try {
            val phoneAccountHandle: PhoneAccountHandle? = callDetails.accountHandle
            if (phoneAccountHandle != null) {
                val telecomManager = getSystemService(TELECOM_SERVICE) as? TelecomManager
                val phoneAccount = telecomManager?.getPhoneAccount(phoneAccountHandle)
                // El ID del componente suele contener el slot o subscription ID
                phoneAccountHandle.id.toIntOrNull() ?: -1
            } else {
                -1
            }
        } catch (e: Exception) {
            -1
        }
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
