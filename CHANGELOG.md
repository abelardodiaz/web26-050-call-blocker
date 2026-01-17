# Changelog

Todos los cambios notables seran documentados aqui.

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

---

## [Unreleased]

### Agregado
- Bloqueo por prefijo (ej: bloquear todos los numeros que empiecen con 442)
- Notificaciones cuando se bloquea una llamada
- PermissionHandler para solicitar permisos
- Solicitud de ROLE_CALL_SCREENING

### Pendiente
- Gradle wrapper (gradlew, gradlew.bat)
- Launcher icons
- Tests unitarios e instrumentados

---

## [0.1.0] - 2026-01-17

### Agregado

- **Estructura inicial del proyecto**
  - Clean Architecture (domain, data, presentation)
  - Hilt dependency injection
  - Room database con 3 entidades

- **CallScreeningService**
  - Intercepta llamadas entrantes
  - Bloquea segun configuracion
  - Registra llamadas bloqueadas

- **UI con Jetpack Compose**
  - Pantalla de llamadas bloqueadas
  - Pantalla de lista de bloqueados
  - Pantalla de configuracion
  - Bottom navigation

- **Modelos de datos**
  - BlockedNumber (numero, label, fecha)
  - BlockedCall (numero, timestamp, razon)
  - Settings (flags de configuracion)

- **Documentacion 996**
  - Context session file
  - system-architecture.md
  - ui-design.md
  - app-architecture.md

---

## Tipos de Cambios

- **Agregado**: Nuevas funcionalidades
- **Cambiado**: Cambios en funcionalidades existentes
- **Obsoleto**: Funcionalidades que seran removidas
- **Removido**: Funcionalidades eliminadas
- **Arreglado**: Correcciones de bugs
- **Seguridad**: Correcciones de vulnerabilidades
