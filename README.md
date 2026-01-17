# Call Blocker (web26-050)

App Android para bloquear llamadas no deseadas.

## Stack

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Arquitectura**: Clean Architecture + MVVM
- **DI**: Hilt
- **Base de datos**: Room
- **Min SDK**: 29 (Android 10)

## Requisitos

- Android Studio Hedgehog o superior
- JDK 17
- Dispositivo/Emulador Android 10+

## Instalacion

```bash
# Clonar
git clone git@gitlab.com:usuario/web26-050-call-blocker.git
cd web26-050-call-blocker

# Abrir en Android Studio
# O construir desde CLI:
./gradlew assembleDebug

# Instalar en dispositivo
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
|   |   +-- dao/         # Room DAOs
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
- [x] Ver historial de llamadas bloqueadas
- [x] Configurar bloqueo de numeros privados
- [ ] **Bloquear por prefijo** (ej: 442)
- [ ] Notificaciones de llamadas bloqueadas
- [ ] Backup/Restore de lista de bloqueados

## Permisos

| Permiso | Uso |
|---------|-----|
| `READ_PHONE_STATE` | Identificar llamadas entrantes |
| `READ_CALL_LOG` | Registrar llamadas bloqueadas |
| `ANSWER_PHONE_CALLS` | Rechazar llamadas |
| `POST_NOTIFICATIONS` | Notificar bloqueos |

## Comandos

| Comando | Descripcion |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests |
| `./gradlew clean` | Limpiar build |

## Documentacion Tecnica

Ver `.claude/doc/call_blocker/`:
- `system-architecture.md` - CallScreeningService
- `ui-design.md` - Diseno de UI
- `app-architecture.md` - Clean Architecture

## Licencia

Proyecto privado - Uso interno.
