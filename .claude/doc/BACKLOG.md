# BACKLOG - Call Blocker (web26-050)

Issues pendientes detectados en code review (2026-02-21).
Ninguno es critico ni bloquea F-Droid, pero deben resolverse en futuras versiones.

---

## Features propuestas

### A. Recordatorio de "app predeterminada" (ROLE_CALL_SCREENING)
- **Contexto**: La app solo pide permisos runtime (telefono, contactos, etc.) pero NO solicita ni verifica el rol `ROLE_CALL_SCREENING`. Sin ese rol, Android nunca invoca el CallScreeningService aunque los permisos esten en verde — el usuario cree que todo esta bien pero no se bloquea nada.
- **Confirmado**: 2026-07-08, usuario reinstalo desde F-Droid, dio todos los permisos, y no bloqueaba llamadas. Causa: falto activar la app como predeterminada en Ajustes > Apps predeterminadas > Identificador de llamadas y spam.
- **Propuesta**: Agregar tarjeta de estado en SettingsScreen (mismo patron que permisos) usando `RoleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)`. Si falta, boton que lance `roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)` para abrir el dialogo nativo del sistema directamente. Revalidar en cada `onResume`.
- **Prioridad**: Alta — es la causa mas probable de "no bloquea nada" en instalaciones nuevas.

---

## Moderados

### 1. Normalizar telefono: edge case con string vacio
- **Archivo**: `app/src/main/java/com/callblocker/core/service/CallBlockerScreeningService.kt` (lineas 128-147)
- **Problema**: `normalizePhoneNumber()` no maneja el caso donde el numero queda vacio despues de limpiar (ej: input "+" -> digitsOnly = ""). Tambien si el input es solo codigo de pais "52", el substring lo deja vacio.
- **Riesgo**: Podria hacer match incorrecto con numeros bloqueados si se compara string vacio.
- **Fix**: Agregar validacion de longitud minima antes de retornar el numero normalizado.

### 2. BlockReason.valueOf() sin proteccion
- **Archivo**: `app/src/main/java/com/callblocker/data/local/entity/BlockedCallEntity.kt` (linea 21)
- **Problema**: `BlockReason.valueOf(reason)` lanza `IllegalArgumentException` si el string no coincide con ningun enum. Si la DB se corrompe, la app crashea.
- **Fix**: Envolver en try-catch con fallback a un valor default (ej: `BlockReason.MANUAL`).

### 3. getRunningServices() deprecated
- **Archivo**: `app/src/main/java/com/callblocker/core/service/CallBlockerForegroundService.kt` (lineas 40-50)
- **Problema**: `getRunningServices()` esta deprecated desde Android 5.1 y puede dar resultados inconsistentes.
- **Fix**: Usar variable estatica `isRunning` dentro del servicio o `ServiceCompat`.

### 4. SIM detection ausente en PhoneStateReceiver (Android 9)
- **Archivo**: `app/src/main/java/com/callblocker/core/receiver/PhoneStateReceiver.kt`
- **Problema**: No tiene logica de deteccion de SIM como `CallBlockerScreeningService` (devSimDetectionByFormat). En Android 9, el filtrado de SIM en dev mode no funciona.
- **Riesgo**: Solo afecta testing en Android 9 con modo desarrollador.

### 5. Condicion redundante en PhoneStateReceiver
- **Archivo**: `app/src/main/java/com/callblocker/core/receiver/PhoneStateReceiver.kt` (linea 93)
- **Problema**: Checa `Build.VERSION.SDK_INT >= Build.VERSION_CODES.P` pero el receiver ya filtra API 29+. La condicion siempre es true cuando se ejecuta (solo corre en API 28).
- **Fix**: Simplificar la condicion o documentar por que existe.

---

## Bajos

### 6. Gradle wrapper sin SHA-256 checksum
- **Archivo**: `gradle/wrapper/gradle-wrapper.properties`
- **Problema**: Tiene `validateDistributionUrl=true` pero no define `distributionSha256Sum`. Funciona via HTTPS pero el checksum agrega seguridad extra.

### 7. Idioma default no se persiste en fresh install
- **Archivo**: `app/src/main/java/com/callblocker/data/repository/SettingsRepositoryImpl.kt` (linea 65)
- **Problema**: `getAppLanguage()` retorna "system" como default pero nunca lo guarda en DB. Funciona correctamente, solo es inconsistencia de persistencia.

### 8. Receiver unregister silencia errores
- **Archivo**: `app/src/main/java/com/callblocker/core/service/CallBlockerForegroundService.kt` (lineas 151-158)
- **Problema**: Catch de `IllegalArgumentException` al hacer unregister no loggea el error. Dificulta debugging.
- **Fix**: Agregar `Log.w()` en el catch.

### 9. Prefix matching y normalizacion
- **Archivo**: `app/src/main/java/com/callblocker/data/local/dao/BlockedNumberDao.kt` (linea 18)
- **Problema**: El SQL usa LIKE para prefix matching pero podria haber discrepancia entre el numero normalizado y el prefix almacenado si el prefix incluye codigo de pais.
- **Riesgo**: Bajo, el CallScreeningService normaliza antes de consultar.

---

## Notas
- Detectados en revision de codigo del 2026-02-21
- Ninguno bloquea la publicacion en F-Droid
- Priorizar items 1-3 para v0.4.0
