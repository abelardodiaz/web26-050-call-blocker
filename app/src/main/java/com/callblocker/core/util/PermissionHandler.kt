package com.callblocker.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility class for handling permissions based on Android version.
 *
 * Android 9 (API 28): Uses TelecomManager.endCall() which requires CALL_PHONE
 * Android 10+ (API 29+): Uses CallScreeningService which requires ROLE_CALL_SCREENING
 */
object PermissionHandler {

    /**
     * Returns the list of required runtime permissions based on Android version.
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.ANSWER_PHONE_CALLS
        )

        // Android 9: CALL_PHONE needed for TelecomManager.endCall()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.CALL_PHONE)
        }

        // Android 13+: POST_NOTIFICATIONS needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions
    }

    /**
     * Returns the list of optional permissions for enhanced functionality.
     */
    fun getOptionalPermissions(): List<String> {
        return listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_NUMBERS
        )
    }

    /**
     * Checks if all required permissions are granted.
     */
    fun areRequiredPermissionsGranted(context: Context): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns missing required permissions.
     */
    fun getMissingPermissions(context: Context): List<String> {
        return getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) !=
                PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if this device requires the legacy blocking method (Android 9).
     */
    fun requiresLegacyBlocker(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    /**
     * Checks if this device supports CallScreeningService (Android 10+).
     */
    fun supportsCallScreeningService(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * Returns human-readable description of why a permission is needed.
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_PHONE_STATE ->
                "Detectar llamadas entrantes"
            Manifest.permission.READ_CALL_LOG ->
                "Acceder al historial de llamadas"
            Manifest.permission.ANSWER_PHONE_CALLS ->
                "Bloquear llamadas no deseadas"
            Manifest.permission.CALL_PHONE ->
                "Terminar llamadas bloqueadas (Android 9)"
            Manifest.permission.READ_CONTACTS ->
                "Identificar llamadas de contactos"
            Manifest.permission.POST_NOTIFICATIONS ->
                "Mostrar notificaciones de llamadas bloqueadas"
            Manifest.permission.READ_PHONE_NUMBERS ->
                "Soporte multi-SIM"
            else -> permission
        }
    }
}
