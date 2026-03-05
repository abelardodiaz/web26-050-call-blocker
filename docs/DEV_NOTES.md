# Notas de Desarrollo - Call Blocker

## Comandos ADB para Debug y Testing

### Conexion y Dispositivos

```bash
# Ver dispositivos conectados
adb devices

# Conectar a dispositivo por IP (WiFi debugging)
adb connect 192.168.1.100:5555

# Desconectar dispositivo
adb disconnect 192.168.1.100:5555

# Reiniciar servidor ADB (si hay problemas de conexion)
adb kill-server
adb start-server
```

### Instalacion de APK

```bash
# Instalar APK (reemplaza si ya existe)
adb install -r app-debug.apk

# Instalar APK con ruta completa
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Desinstalar app
adb uninstall com.redv6.callblocker

# Instalar y reemplazar forzando (si hay problemas de firma)
adb install -r -d app-debug.apk
```

### Monitoreo de Logs (Logcat)

```bash
# Ver logs en tiempo real (todos)
adb logcat

# Limpiar buffer de logs y luego ver
adb logcat -c; adb logcat

# Filtrar por tags especificos de Call Blocker
adb logcat -s CallBlockerApp:D CallBlockerScreening:D CallBlockerFgService:D MainActivity:D

# Limpiar y filtrar (RECOMENDADO para testing)
adb logcat -c; adb logcat -s CallBlockerApp:D CallBlockerScreening:D CallBlockerFgService:D MainActivity:D

# Filtrar solo errores
adb logcat *:E

# Filtrar por tag con nivel de debug
adb logcat -s CallBlockerScreening:D

# Guardar logs a archivo
adb logcat -s CallBlockerScreening:D > logs.txt

# Ver logs con timestamp
adb logcat -v time -s CallBlockerScreening:D
```

### Tags de Log en Call Blocker

| Tag | Clase | Descripcion |
|-----|-------|-------------|
| `CallBlockerApp` | CallBlockerApplication | Inicio de aplicacion |
| `CallBlockerScreening` | CallBlockerScreeningService | Screening de llamadas |
| `CallBlockerFgService` | CallBlockerForegroundService | Servicio foreground |
| `MainActivity` | MainActivity | Actividad principal, permisos |
| `BlockedNumberRepo` | BlockedNumberRepositoryImpl | Verificacion de bloqueo |

### Comandos Utiles de Shell

```bash
# Abrir shell en dispositivo
adb shell

# Ver info del dispositivo
adb shell getprop ro.build.version.sdk    # API level
adb shell getprop ro.product.model        # Modelo
adb shell getprop ro.product.manufacturer # Fabricante

# Ver permisos de la app
adb shell dumpsys package com.redv6.callblocker | grep permission

# Ver servicios activos de la app
adb shell dumpsys activity services com.redv6.callblocker

# Forzar cierre de la app
adb shell am force-stop com.redv6.callblocker

# Iniciar actividad principal
adb shell am start -n com.redv6.callblocker/.presentation.MainActivity

# Limpiar datos de la app (reset completo)
adb shell pm clear com.redv6.callblocker
```

### Flujo de Testing Recomendado

```bash
# 1. Compilar APK
./gradlew assembleDebug --no-daemon

# 2. Instalar en dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Limpiar logs y monitorear
adb logcat -c; adb logcat -s CallBlockerApp:D CallBlockerScreening:D CallBlockerFgService:D MainActivity:D

# 4. Abrir app manualmente o via ADB
adb shell am start -n com.redv6.callblocker/.presentation.MainActivity

# 5. Realizar llamada de prueba y observar logs
```

### Copiar APK a Windows (desde Server005)

```bash
# Obtener version y copiar via SCP
VERSION=$(grep 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
scp app/build/outputs/apk/debug/app-debug.apk wrr@10.254.0.133:/mnt/c/Users/abela/Downloads/CallBlocker-v${VERSION}-debug.apk
```

### Troubleshooting

```bash
# Si adb no reconoce el dispositivo:
adb kill-server
adb start-server
adb devices

# Si la instalacion falla por firma diferente:
adb uninstall com.redv6.callblocker
adb install app-debug.apk

# Si los logs no aparecen:
adb logcat -c              # Limpiar buffer
adb logcat -G 16M          # Aumentar buffer si es necesario

# Ver todos los procesos de la app
adb shell ps | grep callblocker
```

---

## 2026-01-17: Version 0.3.0 - Backup/Restore Completo con Encriptacion

### El Concepto

Sistema de backup/restore completo que incluye todos los datos de la app (numeros bloqueados, historial de llamadas, configuracion) con encriptacion opcional AES-256-GCM.

### Formatos de Backup

| Extension | Contenido | Uso |
|-----------|-----------|-----|
| `.json` | Backup sin encriptar | Legible, compatible con v1 |
| `.cbbk` | Backup encriptado | Protegido con contrasena |

### Estructura del Archivo Backup v2

```json
{
  "metadata": {
    "format_version": 2,
    "app_version": "0.3.0",
    "created_at": 1705500000000,
    "device_model": "SM-S911B",
    "android_version": 36,
    "encrypted": false,
    "counts": {
      "blocked_numbers": 25,
      "blocked_calls": 150,
      "has_settings": true
    }
  },
  "blocked_numbers": [...],
  "blocked_calls": [...],
  "settings": {...}
}
```

### Encriptacion AES-256-GCM

**Formato del archivo .cbbk:**
```
[4 bytes: "CBBK"]     <- Magic header
[16 bytes: salt]      <- Para PBKDF2
[12 bytes: IV]        <- Nonce para GCM
[N bytes: ciphertext] <- Datos + authentication tag
```

**Derivacion de clave:**
- Algoritmo: PBKDF2WithHmacSHA256
- Iteraciones: 100,000
- Longitud de clave: 256 bits
- Salt: 16 bytes aleatorios por backup

### Compatibilidad con Backups Anteriores

| Format Version | Contenido | Soporte |
|----------------|-----------|---------|
| v1 | Solo `blocked_numbers` | Importa solo numeros |
| v2 | Todo (numeros, llamadas, settings) | Importa todo |

La deteccion de version es automatica al importar.

### Merge Inteligente al Restaurar

| Tipo de Dato | Estrategia |
|--------------|------------|
| Numeros bloqueados | Agregar nuevos, omitir duplicados (por phoneNumber) |
| Llamadas bloqueadas | Agregar todas (historial no verifica duplicados) |
| Settings | Sobrescribir si existen en backup |

### Flujo de Usuario

**Export:**
```
[Backup Completo] -> "Con contrasena?" -> [Si/No]
                                              |
                     [Ingresar contrasena] <--+
                              |
                     [Guardar .cbbk/.json en Downloads]
```

**Import:**
```
[Restaurar Backup] -> [Seleccionar archivo]
                              |
                     [Detectar encriptacion]
                              |
        [Solicitar contrasena] (si encriptado)
                              |
                     [Merge inteligente]
                              |
                     [Snackbar con estadisticas]
```

### Archivos Nuevos

| Archivo | Proposito |
|---------|-----------|
| `domain/model/BackupData.kt` | Modelos: BackupData, BackupMetadata, BackupCounts, ImportResult |
| `data/backup/BackupJsonSerializer.kt` | Serializacion JSON con soporte v1 y v2 |
| `data/backup/BackupEncryption.kt` | Encriptacion AES-256-GCM |
| `domain/usecase/ExportFullBackupUseCase.kt` | Exporta backup completo |
| `domain/usecase/ImportFullBackupUseCase.kt` | Importa con merge inteligente |
| `presentation/components/PasswordDialog.kt` | Dialogos de contrasena |

### Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `SettingsViewModel.kt` | +startFullBackup(), +executeFullBackup(), +startFullRestore(), +executeFullRestore() |
| `SettingsScreen.kt` | Nuevos botones y dialogos de backup |
| `build.gradle.kts` | versionCode=13, versionName="0.3.0" |

### Estados del ViewModel para Backup

```kotlin
sealed class PasswordDialogState {
    data object Hidden : PasswordDialogState()
    data object PromptEncrypt : PasswordDialogState()       // Pregunta si encriptar
    data object EnterEncryptPassword : PasswordDialogState() // Ingresar contrasena
    data class RequestDecrypt(val uri: Uri) : PasswordDialogState() // Pedir contrasena
}
```

### Lecciones Aprendidas

| Aspecto | Aprendizaje |
|---------|-------------|
| Extension personalizada | `.cbbk` ayuda a identificar backups encriptados |
| Magic header | "CBBK" permite detectar encriptacion sin extension |
| PBKDF2 | 100k iteraciones es balance entre seguridad y UX |
| GCM vs CBC | GCM incluye autenticacion, detecta contrasena incorrecta |
| Format version | Permite evolucion del formato sin romper backups antiguos |

---

## 2026-01-17: Version 0.2.8.1 - Settings Ocultas de Desarrollador

### El Concepto

Modo desarrollador oculto con activación secreta de 2 pasos para configuración avanzada de detección de SIM por formato de número.

### Activación de 2 Pasos

| Paso | Acción | Tiempo |
|------|--------|--------|
| 1 | Tocar "Información del Sistema" 7 veces | < 2s entre taps |
| 2 | Tocar "Acerca de" 7 veces | < 10s después del paso 1 |

Solo quien conoce la secuencia exacta puede activar el modo desarrollador.

### Lógica de Detección de SIM por Formato

Observación empírica en Samsung Galaxy S23 con Dual SIM:

| Formato de número | Operador | SIM |
|-------------------|----------|-----|
| `+524441234567` (con +52) | Bait | SIM 1 |
| `4441234567` (sin +52) | AT&T MX | SIM 2 |

**Advertencia**: Esta configuración es frágil y específica del dispositivo. Si cambian las SIMs de slot o de operador, dejará de funcionar.

### Nuevos Settings de Desarrollador

| Setting | Descripción | Default |
|---------|-------------|---------|
| `developerModeEnabled` | Muestra/oculta sección dev | `false` |
| `devSimDetectionByFormat` | Detectar SIM por formato +52 | `false` |
| `devBlockSim1` | Bloquear llamadas de SIM 1 (Bait) | `true` |
| `devBlockSim2` | Bloquear llamadas de SIM 2 (AT&T) | `true` |

### Flujo de Filtrado por SIM

```
Llamada entrante: +524441390343
                      ↓
¿devSimDetectionByFormat habilitado?
         Sí ↓                    No → flujo normal
         ↓
¿Tiene código de país +52?
    Sí → SIM 1 (Bait)      No → SIM 2 (AT&T)
         ↓                        ↓
¿devBlockSim1 = true?     ¿devBlockSim2 = true?
    Sí → continuar            Sí → continuar
    No → PERMITIR (return null)
```

### Implementación: Tap Detector de 2 Pasos

```kotlin
// Estados para la activación secreta
var infoSectionTaps by remember { mutableIntStateOf(0) }
var step1Completed by remember { mutableStateOf(false) }
var step1CompletedTime by remember { mutableLongStateOf(0L) }
var aboutSectionTaps by remember { mutableIntStateOf(0) }

// Paso 1: Tocar InfoCard 7 veces
InfoCard(onTap = {
    val now = System.currentTimeMillis()
    if (now - infoSectionLastTap < 2000) {
        infoSectionTaps++
        if (infoSectionTaps >= 7) {
            step1Completed = true
            step1CompletedTime = now
        }
    } else {
        infoSectionTaps = 1
    }
})

// Paso 2: Tocar AboutCard 7 veces (dentro de 10s)
AboutCard(onTap = {
    if (!step1Completed || now - step1CompletedTime > 10000) {
        step1Completed = false
        return@AboutCard
    }
    if (aboutSectionTaps >= 7) {
        viewModel.setDeveloperModeEnabled(true)
        // Snackbar: "Modo desarrollador activado"
    }
})
```

### Migración de Base de Datos v5 → v6

```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE settings ADD COLUMN developer_mode_enabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE settings ADD COLUMN dev_sim_detection_by_format INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE settings ADD COLUMN dev_block_sim1 INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE settings ADD COLUMN dev_block_sim2 INTEGER NOT NULL DEFAULT 1")
    }
}
```

### Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `domain/model/Settings.kt` | +4 campos dev |
| `data/local/entity/SettingsEntity.kt` | +4 columnas |
| `data/local/dao/SettingsDao.kt` | +4 queries UPDATE |
| `data/local/migration/Migrations.kt` | +MIGRATION_5_6 |
| `data/local/AppDatabase.kt` | version = 6 |
| `core/di/AppModule.kt` | +MIGRATION_5_6 |
| `domain/repository/SettingsRepository.kt` | +4 métodos |
| `data/repository/SettingsRepositoryImpl.kt` | +4 implementaciones |
| `presentation/screens/settings/SettingsViewModel.kt` | +4 funciones |
| `presentation/screens/settings/SettingsScreen.kt` | Tap detector + sección oculta |
| `core/service/CallBlockerScreeningService.kt` | Lógica detección SIM por formato |
| `app/build.gradle.kts` | versionCode=12, versionName="0.2.8.1" |
| `PROJECT.yaml` | version: 0.2.8.1 |

### Lecciones Aprendidas

| Aspecto | Aprendizaje |
|---------|-------------|
| Easter Eggs | 2 pasos es más seguro que 1 (evita activación accidental) |
| Formato de número | Es indicador de operador, no de SIM (frágil) |
| Settings avanzados | Ocultar por defecto, solo para quien sabe |
| Configuración por SIM | Útil para usuarios con SIMs de diferente propósito |

---

## 2026-01-17: Version 0.2.8 - Normalizacion de Numeros por Operador

### El Problema

Durante pruebas en Samsung Galaxy S23 (Android 16) con Dual SIM, descubrimos que **cada operador envia los numeros en formato diferente**:

```
=== LLAMADA ENTRANTE ===
Numero: 4441390343          <-- SIM 2 (AT&T Mexico)
isNumberBlocked('4441390343') = true
RESULTADO: BLOQUEAR ✅

=== LLAMADA ENTRANTE ===
Numero: +524441390343       <-- SIM 1 (Bait)
isNumberBlocked('+524441390343') = false
RESULTADO: PERMITIR ❌
```

### Configuracion de Pruebas

| Slot | Operador | Formato de numero entrante |
|------|----------|---------------------------|
| SIM 1 | **Bait** (OMV de AT&T) | `+524441390343` (con codigo de pais) |
| SIM 2 | **AT&T Mexico** | `4441390343` (sin codigo de pais) |

**Dispositivo:** Samsung Galaxy S23
**Android:** 16 (API 36)
**One UI:** 7

### Por que Falla el Bloqueo por Prefijo

El usuario tenia bloqueado el prefijo `444`. La query SQL para prefijos es:

```sql
-- Para prefijos: el numero entrante debe EMPEZAR con el prefijo
SELECT EXISTS(
  SELECT 1 FROM blocked_numbers
  WHERE is_prefix = 1 AND :phoneNumber LIKE phoneNumber || '%'
)
```

**Evaluacion:**
- `'4441390343' LIKE '444%'` → **TRUE** ✅
- `'+524441390343' LIKE '444%'` → **FALSE** ❌ (empieza con +52, no con 444)

### Solucion: Normalizar Antes de Verificar

Nueva funcion `normalizePhoneNumber()` en `CallBlockerScreeningService`:

```kotlin
private fun normalizePhoneNumber(phoneNumber: String): String {
    // Quitar todo excepto digitos
    val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")

    // Mexico: quitar codigo 52 si tiene mas de 10 digitos
    if (digitsOnly.length > 10 && digitsOnly.startsWith("52")) {
        return digitsOnly.substring(2)  // 524441390343 -> 4441390343
    }

    // USA/Canada: quitar codigo 1 si tiene mas de 10 digitos
    if (digitsOnly.length > 10 && digitsOnly.startsWith("1")) {
        return digitsOnly.substring(1)  // 14155551234 -> 4155551234
    }

    return digitsOnly
}
```

### Flujo Corregido

```
Llamada entrante: +524441390343 (Bait)
                      ↓
normalizePhoneNumber("+524441390343")
                      ↓
                 "4441390343"
                      ↓
isNumberBlocked("4441390343")
                      ↓
       '4441390343' LIKE '444%' → TRUE ✅
                      ↓
              BLOQUEAR
```

### Lecciones Aprendidas

| Aspecto | Aprendizaje |
|---------|-------------|
| Formato de numeros | **Cada operador puede enviar formato diferente** |
| Bait (OMV) | Envia numeros con codigo de pais +52 |
| AT&T Mexico | Envia numeros sin codigo de pais |
| Normalizacion | **SIEMPRE normalizar antes de comparar** |
| Prefijos | Solo funcionan si el numero esta normalizado |
| Codigos de pais | Mexico = 52, USA/Canada = 1 |

### Operadores Mexicanos - Comportamiento Conocido

| Operador | Tipo | Formato Observado |
|----------|------|-------------------|
| AT&T Mexico | Principal | `4441234567` (10 digitos) |
| Bait | OMV (AT&T) | `+524441234567` (con +52) |
| Telcel | Principal | Pendiente de pruebas |
| Movistar | Principal | Pendiente de pruebas |

**Nota:** Los OMVs (Operadores Moviles Virtuales) pueden tener comportamiento diferente a la red que usan.

### Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `core/service/CallBlockerScreeningService.kt` | +normalizePhoneNumber() |

---

## 2026-01-17: Version 0.2.7 - Simplificar Dual SIM + Enriquecer Historial

### El Problema

Durante las pruebas en Samsung Android 16, descubrimos que `PhoneAccountHandle` es `null` durante el screening de llamadas:

```
D/CallBlockerScreening: getSubscriptionId: PhoneAccountHandle is null
```

Esto significa que **no podemos saber qué SIM recibe la llamada DURANTE el screening**.

### Investigacion

1. **PhoneAccountHandle.id**: Puede ser null, un entero, o un ICC ID dependiendo del dispositivo
2. **TelecomManager.getPhoneAccount()**: Requiere permisos adicionales y puede fallar
3. **Call.Details.getExtras()**: No contiene informacion de SIM de manera confiable
4. **Sistema de toggles por SIM**: Inutil si no podemos detectar la SIM

### Solucion: Cambio de Estrategia

En lugar de intentar detectar la SIM durante el screening, adoptamos un enfoque diferente:

1. **Bloquear siempre** (sin importar la SIM)
2. **Enriquecer despues** consultando el Call Log del sistema

### Implementacion

**Paso 1: Leer el Call Log despues de bloquear**

```kotlin
// Despues de bloquear, delay para que el sistema registre
delay(1500)

val cursor = contentResolver.query(
    CallLog.Calls.CONTENT_URI,
    arrayOf(CallLog.Calls.NUMBER, "subscription_id"),
    "${CallLog.Calls.DATE} > ?",
    arrayOf(thirtySecondsAgo.toString()),
    "${CallLog.Calls.DATE} DESC"
)
```

**Paso 2: Mapear subscription_id a simSlot**

```kotlin
val subInfo = subscriptionManager.activeSubscriptionInfoList?.find {
    it.subscriptionId == subscriptionId
}
return subInfo?.simSlotIndex  // 0 = SIM 1, 1 = SIM 2
```

### Por que Funciona

- El Call Log del sistema **SI** tiene el `subscription_id` correcto
- Android lo registra DESPUES de que la llamada es procesada
- El delay de 1.5s es suficiente para que el sistema registre la entrada

### Migracion de Base de Datos v4 → v5

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE blocked_calls ADD COLUMN simSlot INTEGER DEFAULT NULL")
    }
}
```

### UI Simplificada

**Antes (Settings)**:
```
[x] SIM 1 - Telcel
[x] SIM 2 - AT&T
```

**Despues (Settings)**:
```
Tarjetas SIM Detectadas
-----------------------
SIM 1: Telcel
SIM 2: AT&T
El bloqueo aplica a todas las SIMs
```

**Historial de llamadas bloqueadas**:
```
+52 55 1234 5678
17 Ene 2026 15:30 • SIM 1
Bloqueado (en lista)
```

### Lecciones Aprendidas

| Aspecto | Aprendizaje |
|---------|-------------|
| CallScreeningService | No garantiza PhoneAccountHandle en todos los dispositivos |
| Samsung Android 16 | PhoneAccountHandle es null - no es un bug, es comportamiento |
| Call Log | Fuente confiable de subscription_id post-facto |
| Estrategia | "Enriquecer despues" > "Detectar durante" |
| Simplicidad | Mejor UX con menos opciones que no funcionan |

### Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `domain/model/BlockedCall.kt` | +simSlot: Int? |
| `data/local/entity/BlockedCallEntity.kt` | +simSlot columna |
| `data/local/dao/BlockedCallDao.kt` | +updateSimSlot() |
| `domain/repository/BlockedCallRepository.kt` | +updateSimSlot(), addBlockedCall retorna Long |
| `data/repository/BlockedCallRepositoryImpl.kt` | Implementaciones |
| `data/local/migration/Migrations.kt` | MIGRATION_4_5 |
| `data/local/AppDatabase.kt` | version = 5 |
| `core/di/AppModule.kt` | +MIGRATION_4_5 |
| `core/service/CallBlockerScreeningService.kt` | Simplificado + updateBlockedCallWithSimInfo() |
| `presentation/screens/settings/SettingsScreen.kt` | SimInfoCard (solo visual) |
| `presentation/screens/settings/SettingsViewModel.kt` | -setSimBlockingEnabled() |
| `presentation/components/BlockedCallCard.kt` | Muestra SIM en timestamp |

---

## 2026-01-17: Version 0.2.2 - Bloqueo Dual SIM Funcional

### Problema

El bloqueo solo funcionaba en una SIM. El `CallBlockerScreeningService` no verificaba de qué SIM venía la llamada entrante.

### Solucion

Agregado en `CallBlockerScreeningService.kt`:

```kotlin
private fun getSubscriptionIdFromCall(callDetails: Call.Details): Int {
    val phoneAccountHandle = callDetails.accountHandle
    return phoneAccountHandle?.id?.toIntOrNull() ?: -1
}

private fun isBlockingEnabledForSim(subscriptionId: Int, enabledSimSlots: Set<Int>): Boolean {
    if (enabledSimSlots.isEmpty()) return true  // Legacy: bloquea en todas
    if (subscriptionId == -1) return true       // Desconocido: bloquea por seguridad
    return enabledSimSlots.contains(subscriptionId)
}
```

### Logica de Bloqueo por SIM

| Configuracion | Comportamiento |
|---------------|----------------|
| Sin SIMs en Ajustes | Bloquea en todas las SIMs |
| SIM 1 habilitada | Solo bloquea llamadas por SIM 1 |
| SIM 2 habilitada | Solo bloquea llamadas por SIM 2 |
| Ambas habilitadas | Bloquea en ambas SIMs |

---

## 2026-01-17: Version 0.2.1 - Tema Oscuro, Espanol y Dual SIM

### Resumen del Cambio

- Tema oscuro forzado por defecto (sin opcion de tema claro)
- Interfaz completa traducida a espanol
- Soporte dual SIM con configuracion individual por tarjeta
- Funciones "Bloquear Desconocidos" y "Notificaciones" deshabilitadas temporalmente

### Tema Oscuro

| Componente | Antes | Despues |
|------------|-------|---------|
| Theme.kt | Sigue sistema | Forzado oscuro siempre |
| Color.kt | BlockedCallBackground | DarkSurfaceVariant |
| Cards | Fondo claro | surfaceVariant del tema |

**Colores Dark Theme:**
```kotlin
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val OnDarkSurface = Color(0xFFE0E0E0)
```

### Textos en Espanol

| Archivo | Textos Traducidos |
|---------|------------------|
| BlockListScreen.kt | "Lista de Bloqueo", "Sin numeros bloqueados" |
| BlockedCallsScreen.kt | "Llamadas Bloqueadas", "Sin llamadas bloqueadas" |
| AddNumberDialog.kt | "Agregar Numero Bloqueado", "Numero de telefono" |
| BlockedCallCard.kt | "Numero Privado", "Bloqueado (en lista)" |
| BlockedNumberCard.kt | "PREFIJO", "Agregado" |
| Navigation.kt | "Llamadas", "Lista", "Ajustes" |

### Soporte Dual SIM

**Nuevo archivo: `core/util/SimManager.kt`**

- Detecta SIMs activas via `SubscriptionManager`
- Compatible desde API 22 (nuestro minSdk es 28)
- Soporta SIM fisica y eSIM

**Cambios en Settings:**

| Capa | Archivo | Cambio |
|------|---------|--------|
| Domain | Settings.kt | +enabledSimSlots: Set<Int> |
| Data | SettingsEntity.kt | +enabled_sim_slots (String) |
| Data | SettingsDao.kt | +setEnabledSimSlots() |
| Data | SettingsRepository.kt | +setEnabledSimSlots() |
| Data | SettingsRepositoryImpl.kt | Implementacion |
| Presentation | SettingsViewModel.kt | +setSimBlockingEnabled() |
| UI | SettingsScreen.kt | Seccion "Tarjetas SIM" |

**Migracion de Base de Datos:**

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE settings ADD COLUMN enabled_sim_slots TEXT NOT NULL DEFAULT ''")
    }
}
```

### Funciones Deshabilitadas

Por ahora, las siguientes funciones estan deshabilitadas en la UI:

1. **Bloquear Desconocidos**: Requiere implementacion de verificacion de contactos
2. **Mostrar Notificaciones**: Requiere implementacion del canal de notificaciones

Se muestran con texto "Proximamente..." y switch deshabilitado.

---

## 2026-01-17: Soporte Android 9-17 (API 28-37)

### Resumen del Cambio

Se implemento soporte para Android 9 (Pie, API 28) ademas del existente para Android 10+. La app ahora es compatible con Android 9 hasta Android 17.

### Metodos de Bloqueo por Version

| Android | API | Metodo | Descripcion |
|---------|-----|--------|-------------|
| 9 (Pie) | 28 | `TelecomManager.endCall()` | Deprecated en API 29 pero funcional en API 28 |
| 10+ (Q+) | 29+ | `CallScreeningService` | Metodo oficial recomendado por Google |

### Diferencias Clave

| Caracteristica | Android 9 | Android 10+ |
|---------------|-----------|-------------|
| App predeterminada | No requerida | Requiere `ROLE_CALL_SCREENING` |
| Servicio | Foreground service manual | Gestionado por el sistema |
| Experiencia | Puede sonar brevemente antes de colgar | Bloqueo silencioso |
| Permisos extra | `CALL_PHONE` | Ninguno |

### Archivos Nuevos

| Archivo | Proposito |
|---------|-----------|
| `core/service/LegacyCallBlockerService.kt` | Foreground service para Android 9 |
| `core/receiver/PhoneStateReceiver.kt` | BroadcastReceiver para detectar llamadas en Android 9 |
| `core/util/PermissionHandler.kt` | Manejo de permisos condicionales por version |
| `domain/model/SimConfig.kt` | Modelo para configuracion multi-SIM |

### Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `app/build.gradle.kts` | `minSdk = 28`, `buildConfig = true` |
| `AndroidManifest.xml` | +CALL_PHONE, +READ_PHONE_NUMBERS, +FOREGROUND_SERVICE, +componentes nuevos |
| `core/receiver/BootReceiver.kt` | Inicia LegacyCallBlockerService en Android 9 |
| `presentation/screens/settings/SettingsScreen.kt` | UI en espanol, info de version, seccion "Acerca de" |
| `res/values/strings.xml` | Traduccion completa a espanol |

### Permisos por Version

**Android 9 (API 28):**
```
READ_PHONE_STATE       - Detectar llamadas entrantes
READ_CALL_LOG          - Historial de llamadas bloqueadas
ANSWER_PHONE_CALLS     - Terminar llamadas
CALL_PHONE             - TelecomManager.endCall()
FOREGROUND_SERVICE     - Mantener servicio activo
```

**Android 10+ (API 29+):**
```
READ_PHONE_STATE       - Detectar llamadas entrantes
READ_CALL_LOG          - Historial de llamadas bloqueadas
ANSWER_PHONE_CALLS     - Requerido por CallScreeningService
ROLE_CALL_SCREENING    - App predeterminada de filtrado
```

### Configurar App como Predeterminada

**Android 10+:** El usuario debe configurar la app como predeterminada en:
- Ajustes > Apps > Apps predeterminadas > Identificador de llamadas y spam

**Android 9:** No requiere ser app predeterminada (usa BroadcastReceiver).

### Soporte Multi-SIM y eSIM

Se agrego el modelo `SimConfig` para futura implementacion de:
- Configuracion de bloqueo por SIM individual
- Soporte para eSIM
- Deteccion de SIMs activas via `SubscriptionManager`

### Limitaciones Android 9

1. **Retraso en bloqueo**: El telefono puede sonar brevemente (~0.5s) antes de colgar
2. **Servicio foreground**: Requiere notificacion permanente para mantenerse activo
3. **Consumo bateria**: Mayor que en Android 10+ debido al servicio foreground

---

## 2026-01-17: Implementacion de Bloqueo por Prefijo

### Resumen del Cambio

Se agrego la funcionalidad para bloquear llamadas por prefijo. Por ejemplo, si se bloquea "442", todas las llamadas que empiecen con "442" seran bloqueadas automaticamente.

### Archivos Modificados

| Capa | Archivo | Cambio |
|------|---------|--------|
| Domain | `domain/model/BlockedNumber.kt` | Agregado `isPrefix: Boolean = false` |
| Data | `data/local/entity/BlockedNumberEntity.kt` | Agregado columna `is_prefix` con `@ColumnInfo` |
| Data | `data/local/migration/Migrations.kt` | **Nuevo** - MIGRATION_1_2 |
| Data | `data/local/AppDatabase.kt` | Version 1 → 2 |
| Data | `data/local/dao/BlockedNumberDao.kt` | Query corregido para matching exacto + prefijo |
| DI | `core/di/AppModule.kt` | Agregado `.addMigrations()` |
| UI | `presentation/components/AddNumberDialog.kt` | Agregado Switch "Block as prefix" |
| UI | `presentation/components/BlockedNumberCard.kt` | Agregado badge "PREFIX" |
| ViewModel | `presentation/screens/blocklist/BlockListViewModel.kt` | Parametro `isPrefix` |
| Screen | `presentation/screens/blocklist/BlockListScreen.kt` | Pasa `isPrefix` al dialog |

### Query de Bloqueo (Critico)

**Antes** (problematico - matching bidireccional confuso):
```sql
SELECT EXISTS(
  SELECT 1 FROM blocked_numbers
  WHERE phoneNumber LIKE '%' || :phoneNumber
     OR :phoneNumber LIKE '%' || phoneNumber
)
```

**Despues** (correcto - exacto + prefijo):
```sql
SELECT EXISTS(
  SELECT 1 FROM blocked_numbers
  WHERE (is_prefix = 0 AND phoneNumber = :phoneNumber)
     OR (is_prefix = 1 AND :phoneNumber LIKE phoneNumber || '%')
)
```

### Migracion de Base de Datos

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE blocked_numbers ADD COLUMN is_prefix INTEGER NOT NULL DEFAULT 0")
    }
}
```

- Los numeros existentes migran con `isPrefix = false` (compatibilidad hacia atras)
- No se pierden datos

### UI Agregada

1. **AddNumberDialog**: Switch toggle para marcar como prefijo
2. **BlockedNumberCard**: Badge "PREFIX" en color primaryContainer cuando `isPrefix == true`

---

## 2026-01-17: Configuracion del Entorno de Build

### Android SDK en Server005

Se configuro el entorno de compilacion Android en el servidor:

```bash
# Ubicaciones
ANDROID_HOME=/home/ubuntu/android-sdk
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
GRADLE=/home/ubuntu/gradle-8.7/bin/gradle
```

### Componentes SDK Instalados

- `platform-tools` (adb, fastboot)
- `platforms;android-34`
- `build-tools;34.0.0`
- `cmdline-tools;latest`

### Archivos de Proyecto Agregados

| Archivo | Proposito |
|---------|-----------|
| `gradlew` | Gradle wrapper script (Unix) |
| `gradlew.bat` | Gradle wrapper script (Windows) |
| `gradle.properties` | Configuracion de Gradle (AndroidX, memoria) |
| `app/src/main/res/mipmap-*/` | Iconos de launcher (placeholder) |
| `app/src/main/res/drawable/ic_launcher_*.xml` | Iconos adaptativos (vector) |

### Comando de Build

```bash
# Recomendado (evita problemas de memoria)
./gradlew assembleDebug --no-daemon

# Con daemon (mas rapido pero puede crashear en VPS)
./gradlew assembleDebug
```

### Output

- APK Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Tamano: ~54MB (debug con todas las librerias Compose)

---

## Notas Tecnicas

### Por que no usar Snap para Gradle

- snap tiene problemas con paths en algunos entornos
- apt gradle (4.4.1) es muy viejo para Android moderno
- Solucion: Descarga manual de Gradle 8.7

### Memoria para Compilacion

- Minimo recomendado: 4GB RAM
- Configurado en `gradle.properties`: `-Xmx2048m`
- Usar `--no-daemon` si el daemon crashea

### Iconos Placeholder

Los iconos actuales son placeholders minimos. Para produccion:

1. Usar Android Studio > Image Asset Studio
2. O crear iconos con herramientas como Figma
3. Exportar a todas las densidades (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
