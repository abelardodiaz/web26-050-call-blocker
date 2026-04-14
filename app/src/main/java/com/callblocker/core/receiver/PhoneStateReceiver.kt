package com.callblocker.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
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

/**
 * BroadcastReceiver for handling phone state changes on Android 9 (API 28).
 *
 * This receiver uses TelecomManager.endCall() to block calls, which is
 * deprecated on API 29+ but functional on API 28.
 *
 * On Android 10+, CallScreeningService is used instead.
 */
@AndroidEntryPoint
class PhoneStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var blockedNumberRepository: BlockedNumberRepository

    @Inject
    lateinit var blockedCallRepository: BlockedCallRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        // Only handle on Android 9 (API 28)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state != TelephonyManager.EXTRA_STATE_RINGING) return

        @Suppress("DEPRECATION")
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        receiverScope.launch {
            handleIncomingCall(context, phoneNumber)
        }
    }

    private suspend fun handleIncomingCall(context: Context, phoneNumber: String?) {
        val settings = settingsRepository.getSettings().first()

        if (!settings.isBlockingEnabled) return

        val blockReason = determineBlockReason(phoneNumber, settings)

        if (blockReason != null) {
            endCall(context)
            logBlockedCall(phoneNumber ?: "", blockReason)
        }
    }

    private suspend fun determineBlockReason(
        phoneNumber: String?,
        settings: com.callblocker.domain.model.Settings
    ): BlockReason? {
        // Private/hidden number
        if (phoneNumber.isNullOrEmpty() && settings.blockPrivateNumbers) {
            return BlockReason.PRIVATE_NUMBER
        }

        // Number in block list
        if (!phoneNumber.isNullOrEmpty() && blockedNumberRepository.isNumberBlocked(phoneNumber)) {
            return BlockReason.BLOCK_LIST
        }

        return null
    }

    @Suppress("DEPRECATION")
    private fun endCall(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            try {
                // endCall() is deprecated in API 29 but works on API 28
                telecomManager.endCall()
            } catch (e: SecurityException) {
                // ANSWER_PHONE_CALLS permission not granted
            }
        }
    }

    private suspend fun logBlockedCall(phoneNumber: String, reason: BlockReason) {
        blockedCallRepository.addBlockedCall(
            BlockedCall(
                phoneNumber = phoneNumber,
                reason = reason
            )
        )
    }
}
