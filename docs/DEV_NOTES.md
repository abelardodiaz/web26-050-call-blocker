# Notas de Desarrollo - Call Blocker

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
