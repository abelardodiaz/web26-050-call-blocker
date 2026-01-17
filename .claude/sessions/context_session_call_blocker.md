# Context Session: Call Blocker Android App

## Estado Actual
- **Fase**: Implementacion inicial completada por 996
- **Branch**: master
- **Archivos**: 53+ archivos

---

## Features Prioritarias

### 1. Bloqueo por Prefijo (ALTA PRIORIDAD)
**Requisito del usuario**: Bloquear llamadas que comiencen con un prefijo especifico (ej: 442)

**Implementacion sugerida**:

1. **Agregar campo en BlockedNumberEntity**:
   ```kotlin
   @Entity(tableName = "blocked_numbers")
   data class BlockedNumberEntity(
       @PrimaryKey(autoGenerate = true)
       val id: Long = 0,
       val phoneNumber: String,
       val label: String? = null,
       val isPrefix: Boolean = false,  // NUEVO: true si es prefijo
       val createdAt: Long = System.currentTimeMillis()
   )
   ```

2. **Actualizar BlockedNumberDao**:
   ```kotlin
   @Query("""
       SELECT EXISTS(
           SELECT 1 FROM blocked_numbers 
           WHERE (isPrefix = 0 AND phoneNumber = :phoneNumber)
              OR (isPrefix = 1 AND :phoneNumber LIKE phoneNumber || '%')
       )
   """)
   suspend fun isNumberBlocked(phoneNumber: String): Boolean
   ```

3. **Actualizar UI (AddNumberDialog)**:
   - Agregar Switch/Checkbox: "Bloquear como prefijo"
   - Si es prefijo, mostrar hint: "Ej: 442 bloqueara 4421234567"

4. **Actualizar BlockedNumberCard**:
   - Mostrar icono/badge si es prefijo
   - Texto: "Prefijo: 442*" en lugar de solo "442"

---

## Pendiente para 050

### Alta Prioridad
1. **Bloqueo por prefijo** (feature del usuario)
2. Crear gradle wrapper (gradlew, gradlew.bat)
3. Agregar launcher icons (mipmap)

### Media Prioridad
4. Implementar PermissionHandler para solicitar permisos
5. Implementar solicitud de ROLE_CALL_SCREENING
6. Notificaciones cuando se bloquea una llamada

### Baja Prioridad
7. Tests unitarios e instrumentados
8. Backup/Restore de lista de bloqueados

---

## Arquitectura Actual

### Domain Layer
- models/: BlockedNumber, BlockedCall, Settings, BlockReason
- repository/: Interfaces de repositorios
- usecase/: (pendiente)

### Data Layer
- local/entity/: Room entities
- local/dao/: Room DAOs
- local/AppDatabase.kt: Room database
- repository/: Implementations

### Presentation Layer
- theme/: Color.kt, Theme.kt
- navigation/: Screen, AppNavigation
- screens/: 3 pantallas con ViewModels
- components/: Cards, Dialog, Switch
- MainActivity.kt

### Core Layer
- di/: AppModule, RepositoryModule
- service/: CallBlockerScreeningService
- receiver/: BootReceiver

---

## Documentacion de Agentes
- .claude/doc/call_blocker/system-architecture.md
- .claude/doc/call_blocker/ui-design.md
- .claude/doc/call_blocker/app-architecture.md

---

## Comandos Utiles
```bash
# Construir APK
./gradlew assembleDebug

# Instalar
adb install app/build/outputs/apk/debug/app-debug.apk

# Logs
adb logcat -s CallBlocker

# Limpiar
./gradlew clean
```

---

## Notas Importantes
- minSdk = 29 (Android 10) requerido para CallScreeningService
- La app debe solicitar ROLE_CALL_SCREENING para ser el call screener default
- El servicio se activa automaticamente cuando la app tiene el rol
