# CLAUDE.md - Call Blocker App (web26-050)

## PROJECT.yaml - IMPORTANTE

Este proyecto tiene un archivo PROJECT.yaml en la raiz. Es leido por el Project Manager (99999) para monitorear el estado.

**Cuando actualizar:**
- Cambio de version -> actualiza project.version
- Siempre actualiza updated_at y updated_by: claude-996

---

## Descripcion

Aplicacion Android nativa para bloqueo de llamadas no deseadas usando CallScreeningService.

## Stack Tecnico

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Arquitectura**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Testing**: JUnit 5 + MockK + Compose Testing

## Sistema de Agentes 996

Este proyecto usa el sistema de agentes colaborativos 996:
- Context files en .claude/sessions/
- Documentacion tecnica en .claude/doc/
- Filosofia: Research First, Implement Later

## Comandos Utiles

```bash
# Build debug
./gradlew assembleDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Post-Build: Copiar APK a Windows

**IMPORTANTE**: Despues de cada build exitoso, copiar el APK a Downloads del usuario:

```bash
# Obtener version del build.gradle.kts
VERSION=$(grep 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')

# Copiar via SCP a WSL local (laptop-g3)
scp app/build/outputs/apk/debug/app-debug.apk wrr@10.254.0.133:/mnt/c/Users/abela/Downloads/CallBlocker-v${VERSION}-debug.apk
```

Destino: `/mnt/c/Users/abela/Downloads/CallBlocker-vX.X.X-debug.apk`

## Dispositivo de Pruebas Principal

- **Modelo**: Samsung Galaxy S23 (SM-S911B)
- **Android**: 16 (API 36)
- **One UI**: 7
- **Configuracion**: Dual SIM (Bait + AT&T Mexico)
- **Nota**: Bait envia numeros con +52, AT&T sin codigo de pais

## Permisos Requeridos

- READ_PHONE_STATE
- READ_CALL_LOG
- ANSWER_PHONE_CALLS
- POST_NOTIFICATIONS (Android 13+)
- Role: ROLE_CALL_SCREENING
