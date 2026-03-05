package com.redv6.callblocker.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.redv6.callblocker.core.service.CallBlockerForegroundService
import com.redv6.callblocker.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isPersistentEnabled = settingsRepository.isPersistentServiceEnabled()

                    // Iniciar servicio si:
                    // 1. El servicio persistente está habilitado en settings, O
                    // 2. Es Android 9 (siempre necesita el servicio para bloqueo legacy)
                    if (isPersistentEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        CallBlockerForegroundService.start(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
