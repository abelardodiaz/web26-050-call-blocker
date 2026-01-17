# Call Blocker App - Context & Planning

## Feature Overview

Aplicacion Android nativa para bloqueo de llamadas no deseadas. Utiliza CallScreeningService (Android 10+) para interceptar y filtrar llamadas entrantes antes de que suenen.

## User Requirements

### Funcionalidades Principales
1. **Bloqueo de llamadas**
   - Bloquear numeros especificos (lista negra)
   - Bloquear numeros desconocidos/privados
   - Bloquear por prefijo (ej: todos los 800-)
   - Bloquear numeros no en contactos

2. **Lista de bloqueo**
   - Agregar numeros manualmente
   - Agregar desde historial de llamadas
   - Agregar desde contactos
   - Importar/exportar lista

3. **Historial de llamadas bloqueadas**
   - Ver llamadas bloqueadas con fecha/hora
   - Desbloquear desde historial
   - Estadisticas de bloqueo

4. **Configuracion**
   - Activar/desactivar bloqueo global
   - Modo silencioso vs rechazo directo
   - Notificaciones de llamadas bloqueadas
   - Backup/restore de configuracion

### Requisitos No Funcionales
- Consumo minimo de bateria
- Sin acceso a internet requerido (funciona offline)
- Datos almacenados localmente (privacidad)
- Material Design 3

## Technical Stack

- **Platform**: Android (minSdk 29, targetSdk 34)
- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room
- **Background**: CallScreeningService
- **Testing**: JUnit 5, MockK, Turbine, Compose Testing

## System APIs Required

### CallScreeningService (API 29+)
- Intercepta llamadas antes de que suenen
- Puede rechazar, silenciar o permitir
- Requiere ser app de screening por defecto

### Permissions
- READ_PHONE_STATE - Estado del telefono
- READ_CALL_LOG - Historial de llamadas
- ANSWER_PHONE_CALLS - Para rechazar llamadas
- POST_NOTIFICATIONS - Notificaciones (Android 13+)

### Roles
- ROLE_CALL_SCREENING - Rol de sistema para screening

## Plan Status

**Status**: Research Phase
**Last Updated**: 2026-01-17

## Phase 1: Research & Design (In Progress)

### Agentes a Consultar:
1. [ ] android-system-architect - CallScreeningService implementation
2. [ ] compose-ui-architect - UI/UX design
3. [ ] android-architecture-expert - Clean Architecture setup
4. [ ] android-test-engineer - Testing strategy

### Deliverables Esperados:
- system-architecture.md (CallScreeningService, permissions, roles)
- ui-design.md (Screens, components, navigation)
- app-architecture.md (Domain, data, presentation layers)
- testing-strategy.md (Unit, UI, integration tests)

## Phase 2: Implementation (Pending)

### Backend/Core:
- [ ] CallScreeningService implementation
- [ ] Room database (BlockedNumber, CallLog entities)
- [ ] Repository implementations
- [ ] Use cases

### UI:
- [ ] Main screen (blocked calls list)
- [ ] Add number screen
- [ ] Settings screen
- [ ] Call history screen

### Testing:
- [ ] Unit tests for use cases
- [ ] Repository tests
- [ ] ViewModel tests
- [ ] UI tests

## Phase 3: Validation (Pending)

- [ ] Manual testing on real device
- [ ] Permission flow validation
- [ ] Battery consumption check

## Notes & Decisions

1. **CallScreeningService vs BroadcastReceiver**: 
   - Decision: CallScreeningService (API moderno, mas control)
   - BroadcastReceiver esta deprecado para call blocking

2. **Almacenamiento**:
   - Decision: Room database local
   - No necesita backend/servidor

3. **UI Framework**:
   - Decision: Jetpack Compose
   - Material 3 para look moderno

## Open Questions

- [ ] Soporte para dual SIM?
- [ ] Integracion con bases de datos de spam (opcional, fase 2)?
- [ ] Widget para activar/desactivar rapidamente?
