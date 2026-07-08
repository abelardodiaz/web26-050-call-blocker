# Diseño: Recordatorio de rol ROLE_CALL_SCREENING en Settings

Fecha: 2026-07-08
Origen: BACKLOG.md item A

## Problema

La app pide permisos runtime (teléfono, contactos, etc.) manualmente vía Ajustes del sistema, pero nunca verifica ni solicita el rol `RoleManager.ROLE_CALL_SCREENING` (app predeterminada de identificador de llamadas y spam). Sin ese rol, Android nunca invoca el `CallScreeningService` de la app aunque todos los permisos estén otorgados — el usuario cree que la app está lista pero no bloquea ninguna llamada.

Confirmado en campo: 2026-07-08, usuario reinstaló desde F-Droid, dio todos los permisos, y no bloqueaba llamadas. Causa raíz: rol no activado.

## Alcance

- Solo aplica a Android 10+ (API 29+, `PermissionHandler.supportsCallScreeningService() == true`).
- Android 9 usa `TelecomManager.endCall()` vía `BroadcastReceiver`, no requiere rol — no se muestra nada en ese caso.

## Diseño

### 1. Detección del estado del rol

Nueva función en `app/src/main/java/com/callblocker/core/util/PermissionHandler.kt`, junto a `supportsCallScreeningService()`:

```kotlin
fun isCallScreeningRoleHeld(context: Context): Boolean {
    if (!supportsCallScreeningService()) return true
    val rm = context.getSystemService(RoleManager::class.java) ?: return true
    return try {
        rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    } catch (e: Exception) {
        true
    }
}
```

Falla segura: si el `RoleManager` no está disponible o lanza excepción, se asume `true` (rol activo) para no bloquear al usuario por un error inesperado del dispositivo/OEM.

### 2. UI: `CallScreeningRoleCard`

Nuevo Composable en `SettingsScreen.kt`, insertado inmediatamente después del `SettingsSwitch` de "Bloqueo activado" (línea ~229), antes de los switches deshabilitados de funciones "coming soon".

- Se renderiza únicamente si `!PermissionHandler.isCallScreeningRoleHeld(context)`.
- Estilo: `Card` con `containerColor = MaterialTheme.colorScheme.errorContainer`, ícono de advertencia, texto explicativo, botón de acción.
- Contenido (nuevos strings en `values/strings.xml` y `values-en/strings.xml`):
  - Título: "Call Blocker no está bloqueando llamadas"
  - Descripción: "Actívala como app predeterminada de identificador de llamadas y spam para que pueda funcionar."
  - Botón: "Activar ahora"

### 3. Acción del botón

```kotlin
val roleLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { roleHeld = PermissionHandler.isCallScreeningRoleHeld(context) }

// onClick del botón:
val rm = context.getSystemService(RoleManager::class.java)
val intent = rm?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
intent?.let { roleLauncher.launch(it) }
```

Abre el diálogo nativo del sistema directamente — el usuario no tiene que navegar manualmente por Ajustes.

### 4. Reactividad al volver de background

El usuario puede activar el rol desde el diálogo nativo (cubierto por el `ActivityResultContracts` de arriba) o manualmente desde Ajustes del sistema sin pasar por el diálogo. Para cubrir ambos casos, se re-evalúa el estado también al volver la Activity a foreground:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            roleHeld = PermissionHandler.isCallScreeningRoleHeld(context)
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

Con esto la tarjeta desaparece sola en cuanto el rol queda activo, sin necesidad de reiniciar la app.

## Fuera de alcance

- No se agrega notificación persistente del sistema (descartado por el usuario).
- No se bloquea el uso del resto de la app mientras falte el rol (descartado por el usuario).
- No se toca la pantalla Home/BlockList — el recordatorio vive solo en Settings.
- No se implementa para Android 9 (no aplica el rol en ese flujo).

## Testing

- Verificar manualmente en emulador/dispositivo Android 10+: tarjeta visible sin rol, botón abre el diálogo nativo, tarjeta desaparece tras conceder el rol.
- Verificar que en Android 9 la tarjeta nunca aparece.
- Verificar que revocar el rol manualmente desde Ajustes y volver a la app (onResume) hace reaparecer la tarjeta.
