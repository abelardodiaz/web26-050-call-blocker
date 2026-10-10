# Call Screening Role Reminder Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show a dismiss-free warning card in Settings that detects when Call Blocker does not hold `ROLE_CALL_SCREENING` (Android 10+) and lets the user activate it with one tap, since without this role Android never invokes the app's `CallScreeningService` even when all runtime permissions are granted.

**Architecture:** A new `PermissionHandler.isCallScreeningRoleHeld()` helper wraps `RoleManager.isRoleHeld()` with fail-safe defaults. `SettingsScreen` holds a `callScreeningRoleHeld` Compose state, re-evaluated on first composition, after the system role-request dialog returns, and on every `ON_RESUME`. A new private `CallScreeningRoleCard` composable renders only when the role is missing and launches `RoleManager.createRequestRoleIntent()` via `ActivityResultContracts.StartActivityForResult`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, `android.app.role.RoleManager`, `androidx.activity.compose.rememberLauncherForActivityResult`, `androidx.lifecycle.LifecycleEventObserver`.

## Global Constraints

- Design spec: `docs/superpowers/specs/2026-07-08-call-screening-role-reminder-design.md` — implement exactly what it describes, nothing more.
- Only applies to Android 10+ (API 29+, `PermissionHandler.supportsCallScreeningService() == true`). Android 9 must never show the card.
- Fail-safe: if `RoleManager` is unavailable or throws, treat the role as held (never falsely block/nag the user due to an OEM quirk).
- Spanish string resources in this project are plain ASCII — no accented characters (verified: zero accented chars across all of `values/strings.xml`). New Spanish strings must follow this.
- This project has **no existing automated test suite** (zero test files despite JUnit5/MockK/Compose-Testing dependencies being configured in `app/build.gradle.kts`). Each task's verification step is a Gradle build check (`compileDebugKotlin` / `assembleDebug`), not a unit test. Full behavioral verification happens manually on-device in Task 4, matching the design spec's own "Testing" section.
- Post-build, copy the debug APK to the user's Windows Downloads per this project's `CLAUDE.md` ("Post-Build: Copiar APK a Windows").
- Do not push or merge — stop after local commits, per this project's `CLAUDE.md` ("no hagas merge con produccion... lo hare o pedire yo manualmente").

---

### Task 1: Branch + `PermissionHandler.isCallScreeningRoleHeld()`

**Files:**
- Modify: `app/src/main/java/com/callblocker/core/util/PermissionHandler.kt`

**Interfaces:**
- Produces: `PermissionHandler.isCallScreeningRoleHeld(context: Context): Boolean` — `true` if the role is held, not required (Android 9), or undeterminable (fail-safe). `false` only when Android 10+ and the role is confirmed NOT held. Consumed by Task 3.

- [ ] **Step 1: Create the feature branch**

```bash
git checkout -b feature/call-screening-role-reminder
```

- [ ] **Step 2: Add the `RoleManager` import**

In `app/src/main/java/com/callblocker/core/util/PermissionHandler.kt`, the file currently starts:

```kotlin
package com.callblocker.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
```

Change to:

```kotlin
package com.callblocker.core.util

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
```

- [ ] **Step 3: Add `isCallScreeningRoleHeld()`**

In the same file, this existing function:

```kotlin
    /**
     * Checks if this device supports CallScreeningService (Android 10+).
     */
    fun supportsCallScreeningService(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
```

becomes:

```kotlin
    /**
     * Checks if this device supports CallScreeningService (Android 10+).
     */
    fun supportsCallScreeningService(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * Checks if the app currently holds ROLE_CALL_SCREENING.
     * Without this role, Android never invokes the app's CallScreeningService
     * even when all runtime permissions are granted.
     *
     * Fail-safe: returns true (role considered held) on Android 9 where the
     * role does not apply, and if RoleManager is unavailable or throws.
     */
    fun isCallScreeningRoleHeld(context: Context): Boolean {
        if (!supportsCallScreeningService()) return true
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return true
        return try {
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } catch (e: Exception) {
            true
        }
    }
```

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/callblocker/core/util/PermissionHandler.kt
git commit -m "feat: add PermissionHandler.isCallScreeningRoleHeld()"
```

---

### Task 2: New string resources

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Produces: `R.string.settings_role_reminder_title`, `R.string.settings_role_reminder_description`, `R.string.settings_role_reminder_button`. Consumed by Task 3.

- [ ] **Step 1: Add Spanish strings**

In `app/src/main/res/values/strings.xml`, this block:

```xml
    <string name="settings_blocking_enabled_desc">Bloquear llamadas de numeros en tu lista</string>
    <string name="settings_block_unknown">Bloquear Desconocidos</string>
```

becomes:

```xml
    <string name="settings_blocking_enabled_desc">Bloquear llamadas de numeros en tu lista</string>
    <string name="settings_role_reminder_title">Call Blocker no esta bloqueando llamadas</string>
    <string name="settings_role_reminder_description">Activala como app predeterminada de identificador de llamadas y spam para que pueda funcionar</string>
    <string name="settings_role_reminder_button">Activar ahora</string>
    <string name="settings_block_unknown">Bloquear Desconocidos</string>
```

- [ ] **Step 2: Add English strings**

In `app/src/main/res/values-en/strings.xml`, this block:

```xml
    <string name="settings_blocking_enabled_desc">Block calls from numbers in your list</string>
    <string name="settings_block_unknown">Block Unknown</string>
```

becomes:

```xml
    <string name="settings_blocking_enabled_desc">Block calls from numbers in your list</string>
    <string name="settings_role_reminder_title">Call Blocker is not blocking calls</string>
    <string name="settings_role_reminder_description">Set it as the default caller ID and spam protection app so it can work</string>
    <string name="settings_role_reminder_button">Activate now</string>
    <string name="settings_block_unknown">Block Unknown</string>
```

- [ ] **Step 3: Verify resources merge cleanly**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL` (new strings are unused until Task 3 — this only confirms well-formed XML)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: add strings for call screening role reminder card"
```

---

### Task 3: `CallScreeningRoleCard` UI + wiring into `SettingsScreen`

**Files:**
- Modify: `app/src/main/java/com/callblocker/presentation/screens/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `PermissionHandler.isCallScreeningRoleHeld(context: Context): Boolean` (Task 1), `R.string.settings_role_reminder_title` / `_description` / `_button` (Task 2).
- Produces: private `CallScreeningRoleCard(onActivateClick: () -> Unit)` composable, used only within this file.

- [ ] **Step 1: Add new imports**

This existing block near the top of `SettingsScreen.kt`:

```kotlin
import com.callblocker.presentation.components.SettingsButton
import com.callblocker.presentation.components.SettingsSwitch

@OptIn(ExperimentalMaterial3Api::class)
```

becomes:

```kotlin
import com.callblocker.presentation.components.SettingsButton
import com.callblocker.presentation.components.SettingsSwitch
import android.app.role.RoleManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)
```

- [ ] **Step 2: Add role state, launcher, and resume listener**

This existing block inside `SettingsScreen`:

```kotlin
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
```

becomes:

```kotlin
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estado del rol ROLE_CALL_SCREENING (Android 10+)
    var callScreeningRoleHeld by remember {
        mutableStateOf(PermissionHandler.isCallScreeningRoleHeld(context))
    }

    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        callScreeningRoleHeld = PermissionHandler.isCallScreeningRoleHeld(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                callScreeningRoleHeld = PermissionHandler.isCallScreeningRoleHeld(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
```

- [ ] **Step 3: Insert the card between the two blocking switches**

This existing block:

```kotlin
            SettingsSwitch(
                title = stringResource(R.string.settings_blocking_enabled),
                description = stringResource(R.string.settings_blocking_enabled_desc),
                checked = settings.isBlockingEnabled,
                onCheckedChange = { viewModel.setBlockingEnabled(it) }
            )

            SettingsSwitch(
                title = stringResource(R.string.settings_block_unknown),
```

becomes:

```kotlin
            SettingsSwitch(
                title = stringResource(R.string.settings_blocking_enabled),
                description = stringResource(R.string.settings_blocking_enabled_desc),
                checked = settings.isBlockingEnabled,
                onCheckedChange = { viewModel.setBlockingEnabled(it) }
            )

            if (!callScreeningRoleHeld) {
                Spacer(modifier = Modifier.height(8.dp))
                CallScreeningRoleCard(
                    onActivateClick = {
                        val roleManager = context.getSystemService(RoleManager::class.java)
                        val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        intent?.let { roleRequestLauncher.launch(it) }
                    }
                )
            }

            SettingsSwitch(
                title = stringResource(R.string.settings_block_unknown),
```

- [ ] **Step 4: Add the `CallScreeningRoleCard` composable**

This existing block near the end of the file:

```kotlin
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun getAndroidVersionName(): String {
```

becomes:

```kotlin
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun CallScreeningRoleCard(
    onActivateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_role_reminder_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_role_reminder_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onActivateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.settings_role_reminder_button))
            }
        }
    }
}

private fun getAndroidVersionName(): String {
```

- [ ] **Step 5: Verify it builds**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/callblocker/presentation/screens/settings/SettingsScreen.kt
git commit -m "feat: show ROLE_CALL_SCREENING reminder card in Settings"
```

---

### Task 4: On-device manual verification + APK delivery

**Files:** none (verification only)

- [ ] **Step 1: Bump version and build**

Follow this project's existing versioning convention: bump `versionCode`/`versionName` in `app/build.gradle.kts` per prior release commits (check `git log --oneline -5 -- app/build.gradle.kts` for the pattern before choosing the next value), then:

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`, APK at `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Copy APK to Windows Downloads**

```bash
VERSION=$(grep 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
scp app/build/outputs/apk/debug/app-debug.apk wrr@10.254.0.133:/mnt/c/Users/abela/Downloads/CallBlocker-v${VERSION}-debug.apk
```

- [ ] **Step 3: Manual verification checklist on the Samsung S23 (Android 16 / One UI 8.5)**

Install the APK and confirm, in order:

1. Before touching Settings: the role is not held (fresh install or after manually revoking it via `Ajustes > Apps > Apps predeterminadas > Identificador de llamadas y spam`) → open Settings screen → the warning card is visible between "Bloqueo Activo" and "Bloquear Desconocidos".
2. Tap "Activar ahora" → the native Android role-request dialog opens directly (not a generic Settings page).
3. Grant the role in that dialog → return to the app → the card disappears immediately, without restarting the app.
4. Manually revoke the role again via `Ajustes > Apps > Apps predeterminadas > Identificador de llamadas y spam` → switch away from Call Blocker and back (triggers `ON_RESUME`) → the card reappears.
5. With the role held, place a test call from a blocked number **not** saved in Contacts → confirm it is actually blocked (this is the original user-reported symptom this feature addresses).

- [ ] **Step 4: Update the backlog**

In `.claude/doc/BACKLOG.md`, remove item "A. Recordatorio de 'app predeterminada' (ROLE_CALL_SCREENING)" from "Features propuestas" now that it's implemented (or mark it done, matching whatever convention the rest of the file uses for resolved items at the time this task runs).

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts .claude/doc/BACKLOG.md
git commit -m "chore: bump version for call screening role reminder release"
```
