# Changelog

Todos los cambios notables seran documentados aqui.

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

---

## [Unreleased]

### Pendiente
- Tests unitarios e instrumentados
- Iconos de launcher personalizados (reemplazar placeholders)
- Notificaciones de llamadas bloqueadas (UI)

---

## [0.2.0] - 2026-01-17

### Agregado

- **Bloqueo por prefijo**
  - Nuevo campo `isPrefix` en modelo BlockedNumber
  - Switch "Block as prefix" en dialogo de agregar numero
  - Badge "PREFIX" en tarjetas de numeros bloqueados
  - Migracion de base de datos v1 → v2

- **Entorno de build**
  - Gradle wrapper (gradlew, gradlew.bat)
  - gradle.properties con configuracion AndroidX
  - Iconos de launcher (placeholder)
  - Iconos adaptativos (vector XML)

- **Documentacion**
  - docs/DEV_NOTES.md con notas tecnicas de desarrollo

### Cambiado

- Query `isNumberBlocked()` corregido para matching exacto + prefijo
- AppDatabase version 1 → 2
- AppModule incluye migracion MIGRATION_1_2

### Arreglado

- Query de bloqueo que hacia matching bidireccional incorrecto

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
