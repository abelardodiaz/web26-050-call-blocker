# Changelog

Todos los cambios notables seran documentados aqui.

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

---

## [Unreleased]

### Pendiente
- Tests unitarios e instrumentados
- Iconos de launcher personalizados (reemplazar placeholders)
- Implementar funcionalidad de notificaciones
- Implementar bloqueo de numeros desconocidos
- Implementar bloqueo de numeros privados (ID oculto)

---

## [0.2.8.1] - 2026-01-17

### Agregado

- **Modo desarrollador oculto con activación de 2 pasos**
  - Paso 1: Tocar "Información del Sistema" 7 veces rápido
  - Paso 2: Tocar "Acerca de" 7 veces (dentro de 10 segundos)
  - Snackbar confirma activación/desactivación
  - Sección de desarrollador solo visible cuando está activado

- **Detección de SIM por formato de número**
  - Nuevo toggle "Detectar SIM por formato" en sección desarrollador
  - Números con +52 = SIM 1 (Bait)
  - Números sin +52 = SIM 2 (AT&T)
  - Toggles individuales para habilitar/deshabilitar bloqueo por SIM

### Técnico

- Nuevos campos en Settings: `developerModeEnabled`, `devSimDetectionByFormat`, `devBlockSim1`, `devBlockSim2`
- Migración de base de datos v5 → v6
- Lógica de detección en `CallBlockerScreeningService.determineBlockReason()`
- Estados de tap detector con `mutableIntStateOf` y `mutableLongStateOf`

### Advertencia

- La detección de SIM por formato es **frágil y específica del dispositivo**
- Si cambian las SIMs de slot o de operador, la lógica dejará de funcionar
- Esta función es experimental y está oculta por defecto

---

## [0.2.8] - 2026-01-17

### Corregido

- **Llamadas con codigo de pais no se bloqueaban**
  - SIM 1 enviaba numeros con prefijo `+52` (ej: `+524441390343`)
  - SIM 2 enviaba numeros sin prefijo (ej: `4441390343`)
  - El prefijo `444` no hacia match con `+524441390343`
  - Nueva funcion `normalizePhoneNumber()` quita codigo de pais antes de verificar
  - Soporta codigos de Mexico (+52) y USA/Canada (+1)

### Tecnico

- `CallBlockerScreeningService.normalizePhoneNumber()` normaliza numeros entrantes
- Remueve `+52` y `+1` de numeros con mas de 10 digitos
- Logs agregados para diagnostico de normalizacion

---

## [0.2.7] - 2026-01-17

### Cambiado

- **Simplificacion Dual SIM: Toggles removidos**
  - `PhoneAccountHandle` es `null` en Samsung Android 16 durante screening
  - No es posible determinar SIM durante el screening de llamada
  - Toggles por SIM removidos - el bloqueo aplica a todas las SIMs
  - Settings ahora muestra SIMs detectadas como informacion visual solamente

### Agregado

- **Enriquecimiento del historial: SIM leida post-bloqueo**
  - Nuevo campo `simSlot` en modelo `BlockedCall` y `BlockedCallEntity`
  - Despues de bloquear, se consulta el Call Log del sistema
  - El campo `subscription_id` del Call Log permite identificar la SIM
  - El historial de llamadas bloqueadas ahora muestra "SIM 1" o "SIM 2"
  - Migracion de base de datos v4 → v5

### Tecnico

- Nueva funcion `updateBlockedCallWithSimInfo()` en `CallBlockerScreeningService`
- Usa delay de 1.5s para dar tiempo al sistema de registrar la llamada
- Compara numeros normalizados (ultimos 10 digitos)
- Mapea `subscriptionId` a `simSlotIndex` via `SubscriptionManager`

### Aprendizajes

- `CallScreeningService` no recibe `PhoneAccountHandle` confiable en todos los dispositivos
- El Call Log del sistema SI tiene el `subscription_id` correcto despues del hecho
- La estrategia "enriquecer despues" es mas robusta que "detectar durante"

---

## [0.2.6] - 2026-01-17

### Corregido

- **SIMs no detectadas en Android 12+ (API 31+)**
  - `getActiveSubscriptionInfoList()` requiere **ambos** permisos: READ_PHONE_STATE y READ_PHONE_NUMBERS
  - `READ_PHONE_NUMBERS` movido de permisos opcionales a requeridos (solo en API 31+)
  - PermissionHandler.kt: Agrega READ_PHONE_NUMBERS en `getRequiredPermissions()` para API 31+
  - SimManager.kt: Verifica READ_PHONE_NUMBERS antes de consultar SIMs en API 31+

### Mejorado

- **Detección de números de teléfono por SIM**
  - API 33+: Usa `SubscriptionManager.getPhoneNumber()`
  - API 31-32: Usa `SubscriptionInfo.number` (deprecado pero funcional)
  - Soporta SIMs físicas y eSIM

---

## [0.2.5] - 2026-01-17

### Corregido

- **Bug critico: settings se reseteaban entre si**
  - Cambiar un setting (ej. SIM 1) reseteaba otros (ej. Servicio Persistente)
  - Causa: SettingsDao usaba OnConflictStrategy.REPLACE
  - Solucion: Cambiado a OnConflictStrategy.IGNORE

### Cambiado

- **"Bloquear Privados" deshabilitado**
  - Funcionalidad no implementada aun
  - Mostrado como "Proximamente..." igual que otras opciones pendientes

---

## [0.2.4] - 2026-01-17

### Agregado

- **Servicio Persistente (Foreground Service)**
  - Nueva opcion "Servicio Persistente" en Ajustes
  - Notificacion permanente "Proteccion Activa" cuando esta habilitado
  - Mejora la confiabilidad del bloqueo en modo de bajo consumo
  - Funciona en todas las versiones de Android (no solo Android 9)

- **Auto-inicio al reiniciar**
  - BootReceiver inicia el servicio si esta habilitado en settings
  - En Android 9 siempre se inicia (necesario para bloqueo legacy)

### Cambiado

- Renombrado LegacyCallBlockerService → CallBlockerForegroundService
- Migracion de base de datos v3 → v4 (campo persistentServiceEnabled)

### Conocido

- Desactivar optimizacion de bateria manualmente para mejor funcionamiento

---

## [0.2.3] - 2026-01-17

### Agregado

- **Export/Import de lista de bloqueados**
  - Exportar numeros bloqueados a archivo JSON en Downloads
  - Importar numeros desde archivo JSON
  - Nueva seccion "Respaldo" en Ajustes
  - Use cases: ExportBlockedNumbersUseCase, ImportBlockedNumbersUseCase
  - Componente SettingsButton para botones de accion

### Corregido

- **Crash en Android 9 al abrir Ajustes**
  - SimManager.getPhoneNumber() ahora verifica API level >= 33
  - Metodo getPhoneNumber(subscriptionId) solo disponible en API 33+

- **SIMs no aparecen en Android 16**
  - Cambiado de `remember {}` a estado reactivo con `LaunchedEffect`
  - SIMs se re-evaluan cuando cambia el estado de permisos

- **Mejora en deteccion de SIM para dual SIM**
  - getSubscriptionIdFromCall() ahora usa multiples metodos de deteccion
  - Busqueda por ICC ID para mayor compatibilidad
  - Logging agregado para diagnostico

### Conocido

- **Android 9**: Bloqueo solo funciona con telefono desbloqueado
- **Android 16**: Llamadas bloqueadas se registran como perdidas

---

## [0.2.2] - 2026-01-17

### Corregido

- **Bloqueo dual SIM funcional**
  - CallBlockerScreeningService ahora verifica de que SIM viene la llamada
  - Respeta la configuracion por SIM en Ajustes
  - Si no hay SIMs configuradas, bloquea en todas (comportamiento legacy)

---

## [0.2.1] - 2026-01-17

### Agregado

- **Tema oscuro por defecto**
  - Colores optimizados para modo oscuro
  - Tema forzado (sin opcion de tema claro)
  - Cards con surfaceVariant del tema

- **Interfaz completa en espanol**
  - BlockListScreen: "Lista de Bloqueo", "Agregar numero", etc.
  - BlockedCallsScreen: "Llamadas Bloqueadas", etc.
  - AddNumberDialog: "Agregar Numero Bloqueado", etc.
  - Navigation: "Llamadas Bloqueadas", "Lista de Bloqueo", "Ajustes"

- **Soporte Dual SIM**
  - SimManager para deteccion de SIMs activas
  - Seccion de configuracion por SIM en Ajustes
  - Campo enabledSimSlots en Settings para persistencia
  - Bloqueo individual habilitado/deshabilitado por SIM

### Cambiado

- Opciones "Bloquear Desconocidos" y "Mostrar Notificaciones" deshabilitadas (pendiente implementacion)
- Colores del tema actualizados para mejor legibilidad en modo oscuro

### Corregido

- Correccion de historial de versiones (0.1.0 -> 0.2.0 -> 0.2.1)

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
