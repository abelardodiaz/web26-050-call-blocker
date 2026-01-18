# Call Blocker (web26-050)

App Android para bloquear llamadas no deseadas.

## Stack

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Arquitectura**: Clean Architecture + MVVM
- **DI**: Hilt
- **Base de datos**: Room
- **Min SDK**: 28 (Android 9+)

## Requisitos

- JDK 17
- Android SDK 34
- Dispositivo/Emulador Android 9+ (API 28+)

## Build

### En servidor (server005)

```bash
cd /home/ubuntu/projects/web26-050-call-blocker

# Build debug APK
./gradlew assembleDebug --no-daemon

# Output
ls app/build/outputs/apk/debug/app-debug.apk
```

### En local (Android Studio)

```bash
# Clonar
git clone <repo-url>
cd web26-050-call-blocker

# Abrir en Android Studio y sincronizar Gradle
# O desde CLI:
./gradlew assembleDebug
```

### Instalar en dispositivo

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Estructura del Proyecto

```
app/src/main/java/com/callblocker/
|-- core/
|   |-- di/              # Hilt modules
|   |-- service/         # CallScreeningService
|   +-- receiver/        # BootReceiver
|
|-- data/
|   |-- local/
|   |   |-- entity/      # Room entities
|   |   |-- dao/         # Room DAOs
|   |   +-- migration/   # Room migrations
|   +-- repository/      # Repository implementations
|
|-- domain/
|   |-- model/           # Domain models
|   |-- repository/      # Repository interfaces
|   +-- usecase/         # Use cases
|
+-- presentation/
    |-- components/      # Composables reutilizables
    |-- navigation/      # Navigation setup
    |-- screens/         # Pantallas (ViewModels + Composables)
    +-- theme/           # Material 3 theme
```

## Funcionalidades

- [x] Bloquear numeros especificos
- [x] **Bloquear por prefijo** (ej: 442 bloquea todos los que empiecen con 442)
- [x] Ver historial de llamadas bloqueadas
- [x] Configurar bloqueo de numeros privados
- [x] **Soporte Dual SIM** - bloqueo individual por tarjeta
- [x] **Tema oscuro** forzado para mejor legibilidad
- [x] **Interfaz en espanol**
- [x] Compatible Android 9 - 17
- [x] **Backup/Restore completo** con encriptacion AES-256-GCM opcional
- [ ] Notificaciones de llamadas bloqueadas

## Bloqueo por Prefijo

Nueva funcionalidad que permite bloquear rangos de numeros:

1. Abrir la app > Block List > Boton (+)
2. Ingresar el prefijo (ej: "442")
3. Activar switch "Block as prefix"
4. Guardar

Todos los numeros que empiecen con ese prefijo seran bloqueados.

## Backup y Restauracion

### Backup Completo
1. Abrir la app > Ajustes > Respaldo
2. Tocar "Backup Completo"
3. Elegir si proteger con contrasena
4. El archivo se guarda en Downloads

### Restaurar Backup
1. Ajustes > Respaldo > "Restaurar Backup"
2. Seleccionar archivo (.json o .cbbk)
3. Si esta encriptado, ingresar contrasena
4. Los datos se agregan sin duplicar existentes

**Formatos:**
- `.json` - Backup sin encriptar (legible)
- `.cbbk` - Backup encriptado con AES-256-GCM

## Permisos

| Permiso | Uso |
|---------|-----|
| `READ_PHONE_STATE` | Identificar llamadas entrantes |
| `READ_CALL_LOG` | Registrar llamadas bloqueadas |
| `ANSWER_PHONE_CALLS` | Rechazar llamadas |
| `POST_NOTIFICATIONS` | Notificar bloqueos |
| `ROLE_CALL_SCREENING` | Actuar como screening service |

## Comandos

| Comando | Descripcion |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests |
| `./gradlew clean` | Limpiar build |

## Documentacion

| Documento | Contenido |
|-----------|-----------|
| `docs/DEV_NOTES.md` | Notas tecnicas de desarrollo |
| `CHANGELOG.md` | Historial de cambios |
| `.claude/doc/` | Documentacion arquitectura 996 |

## Entorno de Desarrollo (Server005)

```bash
# Variables de entorno (ya en ~/.bashrc)
export ANDROID_HOME=/home/ubuntu/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:/home/ubuntu/gradle-8.7/bin
```

## Licencia

Proyecto privado - Uso interno.
