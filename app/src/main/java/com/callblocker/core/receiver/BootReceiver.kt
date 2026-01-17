package com.callblocker.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.callblocker.core.service.LegacyCallBlockerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Android 9 (API 28): Iniciar servicio legacy
                LegacyCallBlockerService.start(context)
            }
            // Android 10+: CallScreeningService es manejado por el sistema
        }
    }
}
