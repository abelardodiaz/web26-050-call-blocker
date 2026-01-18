package com.callblocker.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.callblocker.R
import com.callblocker.core.receiver.PhoneStateReceiver
import com.callblocker.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service for persistent call blocking protection.
 *
 * This service keeps the app alive with a notification to ensure
 * reliable call blocking on all Android versions.
 *
 * On Android 9 (API 28): Also handles call blocking via PhoneStateReceiver
 * On Android 10+: Ensures CallScreeningService is promptly awakened
 */
@AndroidEntryPoint
class CallBlockerForegroundService : Service() {

    private var phoneStateReceiver: PhoneStateReceiver? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "call_blocker_service"

        fun isRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            @Suppress("DEPRECATION")
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (CallBlockerForegroundService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun start(context: Context) {
            val intent = Intent(context, CallBlockerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallBlockerForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Solo registrar PhoneStateReceiver en Android 9 (legacy blocking)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            registerPhoneStateReceiver()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterPhoneStateReceiver()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Proteccion de Llamadas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el bloqueo de llamadas activo"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Proteccion Activa")
            .setContentText("Bloqueando llamadas no deseadas")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun registerPhoneStateReceiver() {
        phoneStateReceiver = PhoneStateReceiver()
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(phoneStateReceiver, filter)
    }

    private fun unregisterPhoneStateReceiver() {
        phoneStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
        phoneStateReceiver = null
    }
}
