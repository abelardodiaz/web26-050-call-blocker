package com.callblocker.core.service

import android.telecom.Call
import android.telecom.CallScreeningService
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

        serviceScope.launch {
            val settings = settingsRepository.getSettings().first()

            if (!settings.isBlockingEnabled) {
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
