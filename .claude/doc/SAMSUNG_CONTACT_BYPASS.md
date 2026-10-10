# Samsung One UI - Contact Bypass de CallScreeningService

> **TL;DR (actualizado 2026-10-10):** El 2026-05-02 se observó que una llamada de un número guardado en Contactos NO se delegaba al `CallScreeningService` de terceros (filtro `SamsungAutoRejectIncomingCallFiltering` → `Allow ... contact exists`), con el rol asignado. Ese día se probó en **dos equipos** (Galaxy S23 con One UI 8.5 / Android 16, y un Samsung antiguo de gama media/baja con One UI 7.x / Android 15) y **no quedó registrado cuál produjo la evidencia**. Posteriormente NO se reproduce: S23 (2026-07-10) y Galaxy S26 / One UI 9 (2026-10-10) rechazan un contacto bloqueado. Causas posibles, sin distinguir: (a) bypass propio de One UI 7.x, (b) el rol no estaba realmente vinculado en ese momento, (c) una actualización del sistema lo corrigió. No hay fix viable en la app; si ocurre, el workaround es borrar el número de Contactos.

---

## Contexto

Reportado el 2026-05-02 tras instalar v0.3.4 desde F-Droid. Ese día el autor probó el mismo escenario en dos equipos: su Galaxy S23 (One UI 8.5 / Android 16) y un Samsung antiguo de gama media/baja, no de la serie Galaxy S (One UI 7.x / Android 15; modelo no registrado y ya no disponible). El commit original (`3588f2d`) registró un solo equipo con la etiqueta mezclada "Galaxy S23 / One UI 7 / Android 16" (combinación imposible: One UI 7 es Android 15), por lo que **se desconoce qué equipo produjo la evidencia de abajo**. Síntoma: la app no bloqueaba llamadas de un contacto guardado, aunque todo lo demás (permisos, rol, optimización de batería, prefijos en BD) estaba correcto.

## Diagnóstico — evidencia capturada (2026-05-02, equipo no determinado: S23 o Samsung antiguo)

### 1. Verificación del rol — CORRECTO

```bash
adb shell cmd role get-role-holders android.app.role.CALL_SCREENING
# → com.callblocker

adb shell dumpsys role | grep -A 3 CALL_SCREENING
# →   name=android.app.role.CALL_SCREENING
#     fallback_enabled=true
#     holders=com.callblocker
```

### 2. Verificación de telecom — el rol está, pero el screening no se invoca

```bash
adb shell dumpsys telecom | grep -i screen
```

Salida relevante (call event durante una llamada de contacto):

```
DefaultCallScreeningApp: com.callblocker
REQUEST_ADD_CALL_LOG - AddCallParams {
    Number : +52*******567
    CallScreeningAppName : null
    CallScreeningComponentName : null
    ...
}
```

**Clave:** `DefaultCallScreeningApp: com.callblocker` está configurado, pero al registrar la llamada `CallScreeningAppName` y `CallScreeningComponentName` salen `null`. Significa que el sistema NO invocó la app de screening pese a tener el rol.

### 3. Pipeline interno de Samsung — el shortcut

El trace de filtros muestra:

```
INCOMING_FILTERING_INITIATED (SamsungAutoRejectIncomingCallFiltering)
INCOMING_FILTERING_FINISHED (SamsungAutoRejectIncomingCallFiltering, [Allow, logged, notified, contact exists])
FILTERING_COMPLETED ([Allow, logged, notified, contact exists])
```

El filtro `SamsungAutoRejectIncomingCallFiltering` detecta `contact exists` y termina con `Allow`, deteniendo el pipeline antes de delegar al `CallScreeningService` de terceros.

### 4. Logcat de la app — vacío

```bash
adb shell logcat -d -s CallBlockerScreening:D
# (sin output durante la llamada de prueba — onScreenCall() nunca fue invocado)
```

### 5. Confirmación experimental

El usuario borró el contacto y volvió a llamar desde el mismo número. Resultado: bloqueo correcto, llamada en la lista de bloqueadas. Esto confirma que el único factor diferenciador es la presencia del número en Contactos.

## Por qué no hay fix viable

### Opción descartada A — extender `PhoneStateReceiver` a Android 10+

La app ya tiene un `PhoneStateReceiver` (path legacy para Android 9 / API 28). En teoría podría usarse como red de seguridad en Android 10+ para atrapar las llamadas que Samsung deja pasar. **No funciona** por dos razones:

1. **`EXTRA_INCOMING_NUMBER` no llega.** Desde Android 10 (API 29), el broadcast `PHONE_STATE` no entrega el número de teléfono a apps que no son el dialer por defecto. Sin número no hay decisión de bloqueo.
2. **`TelecomManager.endCall()` requiere `MODIFY_PHONE_STATE` (system permission) o `ROLE_DIALER`.** Una app con solo `ANSWER_PHONE_CALLS` lanza `SecurityException` en Android 10+.

Estas restricciones existen exactamente para forzar el flujo correcto: las apps que quieran controlar llamadas deben ser `CallScreeningService` (lo que ya somos) o `ROLE_DIALER`.

### Opción descartada B — ser `ROLE_DIALER`

Hacer Call Blocker el dialer por defecto del sistema. Le daría control total sobre llamadas entrantes ANTES de que Samsung las toque. Pero:

- Reemplaza la app de Teléfono nativa de Samsung.
- Requiere implementar `InCallService` completo: pantalla de llamada entrante, marcado, mute, speaker, hold, llamadas en espera, integración con contactos, historial, etc.
- Multiplica el scope de la app por 5x.
- Rompe la filosofía actual de Call Blocker ("herramienta minimalista de bloqueo").
- Los usuarios de F-Droid no esperan ese tipo de invasividad.

**Decisión 2026-05-02:** no se implementa. Si en el futuro hay demanda fuerte y el equipo decide pivotar el scope, este sería el path técnico.

### Opción descartada C — workarounds frágiles

`NotificationListenerService` para detectar la notificación de llamada y matar el call vía algún truco no documentado: rota frecuentemente entre versiones de Android, no es robusto, no aporta valor.

## Triaje — qué hacer cuando un usuario reporte "no me bloquea X"

Antes de tocar código, hacer este checklist con el usuario:

1. **¿Qué dispositivo es?** Si es Samsung (o Xiaomi, Oppo, Vivo posiblemente), seguir abajo. Si es Pixel/AOSP, investigar como bug real.
2. **¿El rol `CALL_SCREENING` está activo para Call Blocker?** (causa más probable en reinstalaciones) y **¿hay una regla de prefijo muy amplia** que atrape al contacto, o al revés, falta la regla? Revisar Ajustes > Lista de bloqueados.
   Si 1-2 están bien y el número está en Contactos: verificar con `dumpsys telecom`. Si `SCREENING_COMPLETED` sale con `mCallScreeningAppName = null` y `SamsungAutoRejectIncomingCallFiltering` con `contact exists`, Samsung no delegó la llamada (visto en mayo 2026, posiblemente One UI 7.x). Workaround: borrar de Contactos. En One UI 8.5/9 se verificó que un contacto bloqueado sí se rechaza.
3. **Si no está en Contactos** y aún así no bloquea → ahí sí investigar. Capturar logcat:
   ```bash
   adb logcat -c
   adb logcat -s CallBlockerScreening:D
   # luego hacer la llamada de prueba
   ```
   Si no aparece `=== LLAMADA ENTRANTE ===` el problema es de delivery del sistema (rol mal asignado, OEM diferente bypaseando, etc.). Si aparece pero termina en `PERMITIR`, es bug de matching nuestro.

## Comandos de diagnóstico de referencia

```bash
# Quien tiene el rol
adb shell cmd role get-role-holders android.app.role.CALL_SCREENING

# Ver si Samsung shortcuteo la última llamada
adb shell dumpsys telecom | grep -i "screen\|filter" | tail -50

# Logs en vivo del screening service
adb shell logcat -s CallBlockerScreening:D

# Versión y firma instaladas
adb shell dumpsys package com.callblocker | grep -E "versionName|versionCode|signatures"
```

## Documentación expuesta al usuario

- `README.md` → sección "Compatibility Notes → Samsung devices (One UI)" — explicación corta + workaround.
- `CHANGELOG.md` → bajo `[Unreleased]` → `### Documented`.
- `fastlane/metadata/.../full_description.txt` → **NO actualizado todavía** (evita refresh en F-Droid). Pendiente para el siguiente release real.

## Pendientes (cuando salga próximo release)

- [ ] Sincronizar la nota de Samsung en `fastlane/metadata/android/en-US/full_description.txt` y `es-MX/full_description.txt`.
- [ ] (Opcional) Mostrar nota in-app en Settings cuando `Build.MANUFACTURER == "samsung"`, algo como un info icon explicando la limitación.
- [ ] Verificar comportamiento en Xiaomi MIUI / Oppo ColorOS / Vivo FunTouch si aparece algún tester con esos dispositivos. Documentar resultados en este mismo archivo.

## Referencias

- Android docs sobre `RoleManager.ROLE_CALL_SCREENING`: https://developer.android.com/reference/android/app/role/RoleManager#ROLE_CALL_SCREENING
- Samsung One UI no expone públicamente cómo opera su pipeline de filtros de llamadas. La evidencia se obtiene vía `dumpsys telecom`.

---

## Verificación en Galaxy S26 (2026-10-10)

Equipo: Galaxy S26 (SM-S948B), Android 17, One UI 9, parche de seguridad 2026-10-05. App instalada: **v0.3.4** (versionCode 17). Rol `CALL_SCREENING` en manos de `com.callblocker`.

`dumpsys telecom` (vía adb de depuración inalámbrica del propio teléfono) muestra, para una llamada entrante de un número **en Contactos**:

- `SamsungAutoRejectIncomingCallFiltering` → `[Allow, ..., contact exists]` (igual que en mayo).
- Pero luego `SCREENING_COMPLETED ... mCallScreeningAppName = Call Blocker, mCallScreeningComponentName = com.callblocker/.../CallBlockerScreeningService` → **el servicio de Call Blocker SÍ fue invocado** pese a `contact exists`.

Conclusión: en One UI 9 el contacto NO impide que se delegue la llamada a `CallScreeningService`. La respuesta fue `shouldRejectCall=false` porque ese número no estaba en la lista de bloqueados (no se probó un contacto bloqueado). Coincide con lo observado en el S23 (One UI 8.5) el 2026-07-10, donde un contacto también fue bloqueado.

### Prueba con contacto bloqueado (2026-10-10 20:56)

Llamada de un número en Contactos Y en la lista de bloqueados (`4441234567`, API 37). Logcat de la app:

```
=== LLAMADA ENTRANTE === Numero: 4441234567
isNumberBlocked('4441234567') = true -> BlockReason: BLOCK_LIST
RESULTADO: Numero bloqueado por BLOCK_LIST -> BLOQUEAR
```

`dumpsys telecom`: `SCREENING_COMPLETED ([Reject, logged, notified, ..., contact exists, mCallBlockReason = 1, mCallScreeningAppName = Call Blocker, ...])` y `FILTERING_COMPLETED ([Reject ...])`.

**Resultado: en Galaxy S26 / Android 17 / One UI 9 (app v0.3.4) un contacto bloqueado SÍ se rechaza.** No hay bypass de Contactos en One UI 9. `contact exists` aparece solo como metadata.

### Qué falta para cerrar el tema

Si hay otro Samsung con One UI 7.x a mano, probar un contacto bloqueado: si lo deja pasar, el bypass es propio de One UI 7.x; si lo rechaza, la causa de mayo fue el rol no vinculado.
