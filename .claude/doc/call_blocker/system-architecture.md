# Call Blocker - System Architecture

**Feature**: Call Blocker App
**Agent**: android-system-architect
**Date**: 2026-01-17
**Session**: context_session_call_blocker

---

## Executive Summary

Este documento define la arquitectura de sistema para la app de bloqueo de llamadas, enfocandose en CallScreeningService, manejo de permisos, y la integracion con APIs del sistema Android.

**Key Decisions:**
- CallScreeningService como mecanismo principal de bloqueo
- Role-based permission para ser app de screening por defecto
- Room database para persistencia local
- Sin necesidad de internet (100% offline)

---

## 1. CallScreeningService Implementation

### 1.1 Service Overview

```kotlin
// core/service/CallBlockerScreeningService.kt
package com.callblocker.core.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.callblocker.domain.repository.BlockedNumberRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallBlockerScreeningService : CallScreeningService() {

    @Inject
    lateinit var blockedNumberRepository: BlockedNumberRepository

    @Inject
    lateinit var callLogRepository: CallLogRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        if (phoneNumber == null) {
            handlePrivateNumber(callDetails)
            return
        }

        serviceScope.launch {
            val response = screenCall(phoneNumber, callDetails)
            respondToCall(callDetails, response)
        }
    }

    private suspend fun screenCall(
        phoneNumber: String,
        callDetails: Call.Details
    ): CallResponse {
        val settings = settingsRepository.getSettings()

        if (!settings.isBlockingEnabled) {
            return allowCall()
        }

        val isBlocked = blockedNumberRepository.isBlocked(phoneNumber)
        val isUnknown = !isInContacts(phoneNumber)
        val shouldBlockUnknown = settings.blockUnknownNumbers && isUnknown

        return if (isBlocked || shouldBlockUnknown) {
            callLogRepository.logBlockedCall(
                phoneNumber = phoneNumber,
                reason = if (isBlocked) BlockReason.BLACKLIST else BlockReason.UNKNOWN,
                timestamp = System.currentTimeMillis()
            )

            if (settings.showBlockedNotifications) {
                notifyBlockedCall(phoneNumber)
            }

            blockCall(settings.blockMode)
        } else {
            allowCall()
        }
    }

    private fun handlePrivateNumber(callDetails: Call.Details) {
        serviceScope.launch {
            val settings = settingsRepository.getSettings()

            val response = if (settings.blockPrivateNumbers) {
                callLogRepository.logBlockedCall(
                    phoneNumber = "Private",
                    reason = BlockReason.PRIVATE,
                    timestamp = System.currentTimeMillis()
                )
                blockCall(settings.blockMode)
            } else {
                allowCall()
            }

            respondToCall(callDetails, response)
        }
    }

    private fun allowCall(): CallResponse {
        return CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .build()
    }

    private fun blockCall(mode: BlockMode): CallResponse {
        return when (mode) {
            BlockMode.REJECT -> CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()

            BlockMode.SILENCE -> CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSilenceCall(true)
                .build()
        }
    }

    private fun isInContacts(phoneNumber: String): Boolean {
        // TODO: Implementar verificacion de contactos
        return false
    }

    private fun notifyBlockedCall(phoneNumber: String) {
        // TODO: Implementar notificacion
    }
}
```

### 1.2 Block Modes

| Mode | Behavior | User Experience |
|------|----------|-----------------|
| **REJECT** | Rechaza la llamada | Llamante escucha tono de ocupado |
| **SILENCE** | Silencia pero no rechaza | Telefono no suena, va a voicemail |

### 1.3 Block Reasons

```kotlin
// domain/model/BlockReason.kt
enum class BlockReason {
    BLACKLIST,      // Numero en lista negra
    UNKNOWN,        // Numero desconocido
    PRIVATE,        // Numero privado/oculto
    PREFIX,         // Coincide con prefijo bloqueado
    SPAM            // Detectado como spam (futuro)
}
```

---

## 2. Permission Strategy

### 2.1 Required Permissions

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
</manifest>
```

### 2.2 Permission Handler

```kotlin
// core/util/PermissionHandler.kt
package com.callblocker.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHandler {

    val REQUIRED_PERMISSIONS = buildList {
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.READ_CALL_LOG)
        add(Manifest.permission.ANSWER_PHONE_CALLS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    fun hasRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    fun getMissingPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(context, it) !=
                PackageManager.PERMISSION_GRANTED
        }
    }
}
```

---

## 3. Role Management

### 3.1 Request Default Call Screening Role

```kotlin
// core/util/CallScreeningRoleManager.kt
package com.callblocker.core.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import androidx.activity.result.ActivityResultLauncher

object CallScreeningRoleManager {

    fun isDefaultCallScreeningApp(context: Context): Boolean {
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        return telecomManager?.defaultCallScreeningApp == context.packageName
    }

    fun requestCallScreeningRole(
        context: Context,
        launcher: ActivityResultLauncher<Intent>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)

            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(
                        RoleManager.ROLE_CALL_SCREENING
                    )
                    launcher.launch(intent)
                }
            }
        }
    }
}
```

---

## 4. AndroidManifest Configuration

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".CallBlockerApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.CallBlocker">

        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.CallBlocker">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".core.service.CallBlockerScreeningService"
            android:permission="android.permission.BIND_SCREENING_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.telecom.CallScreeningService" />
            </intent-filter>
        </service>

        <receiver
            android:name=".core.receiver.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

    </application>
</manifest>
```

---

## 5. Data Models

### 5.1 Blocked Number Entity

```kotlin
@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "is_prefix")
    val isPrefix: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
```

### 5.2 Blocked Call Log Entity

```kotlin
@Entity(tableName = "blocked_calls")
data class BlockedCallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "reason")
    val reason: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "display_name")
    val displayName: String? = null
)
```

### 5.3 Settings Entity

```kotlin
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "is_blocking_enabled")
    val isBlockingEnabled: Boolean = true,

    @ColumnInfo(name = "block_mode")
    val blockMode: String = "REJECT",

    @ColumnInfo(name = "block_unknown_numbers")
    val blockUnknownNumbers: Boolean = false,

    @ColumnInfo(name = "block_private_numbers")
    val blockPrivateNumbers: Boolean = false,

    @ColumnInfo(name = "show_blocked_notifications")
    val showBlockedNotifications: Boolean = true
)
```

---

## Implementation Checklist

- [ ] Create CallBlockerScreeningService
- [ ] Implement permission handling
- [ ] Implement role request flow
- [ ] Create Room entities and DAOs
- [ ] Create BootReceiver
- [ ] Configure AndroidManifest
- [ ] Write unit tests
- [ ] Test on real device

---

*Generated by android-system-architect - Sistema de Agentes 996*
