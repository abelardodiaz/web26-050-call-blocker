# Roadmap Debate: DeepSeek vs OpenAI

**Fecha**: 2026-01-17
**Sistema**: interactions-pro (Server003)
**Rondas**: 10 turnos de debate
**Proyecto**: Call Blocker v0.2.8.1

---

## Contexto Presentado

- App Android nativa (Kotlin + Jetpack Compose + Material 3)
- CallScreeningService para bloqueo de llamadas
- Soporte Android 9-17 (API 28-35+)
- Dual SIM con detección por formato (Bait +52, AT&T sin código)
- Arquitectura Clean + MVVM + Hilt + Room
- Estado actual: v0.2.8.1 funcional en Samsung Galaxy S23

## Preguntas Debatidas

1. Próximos pasos recomendados para la app
2. ¿Conviene hacerla open source para portafolio?
3. Estrategia de monetización (Ads vs F-Droid vs Híbrido)

---

## 1. Próximos Pasos Recomendados

### CONSENSO: Backup/Restore como prioridad #1

**Roadmap acordado:**

| Versión | Feature | Descripción | Estado |
|---------|---------|-------------|--------|
| v0.3.0 | Backup local | Export/import JSON con encriptación opcional | **COMPLETADO** |
| v0.4.0 | Backup cloud | Sincronización Google Drive/Firebase | Pendiente |
| v0.5.0 | Spam database | Sistema P2P/crowdsourcing anónimo | Pendiente |

### Especificaciones Técnicas Acordadas

**Backup/Restore (v0.3.0):**
- Formato JSON/XML estándar para portabilidad
- Metadatos de versión para compatibilidad futura
- Encriptación opcional (AES-256 recomendado)
- Backup incremental para ahorrar espacio
- Restauración parcial (solo blacklist, solo config, etc.)
- Validación de integridad post-restauración
- Verificación de permisos pre-operación
- Notificaciones detalladas de éxito/error

**Spam Database (v0.5.0 - futuro):**
- Enfoque crowdsourcing anónimo (hash de números, no números completos)
- Arquitectura P2P descentralizada (formato Blocklist)
- Evita costos de servidor centralizado

### Features Descartados/Pospuestos

- Regex patterns: Pospuesto, menor valor que backup
- Scheduled blocking: Considerado para v0.4.0+
- Spam database online centralizada: Descartado por complejidad legal

---

## 2. Open Source

### CONSENSO: Sí, hacerla open source

| Aspecto | DeepSeek | OpenAI | Decisión |
|---------|----------|--------|----------|
| Plataforma | GitHub | GitHub | **GitHub** |
| Licencia | GPLv3 | Apache 2.0 | Por definir |
| Timing | Después de v0.3.0 | Después de estabilizar | **Post v0.3.0** |

### Beneficios Identificados

- **Portafolio**: Demuestra Clean Architecture + MVVM + Hilt + Room
- **Colaboración**: Traducciones, testing en más dispositivos
- **Credibilidad**: Revisión de código por pares
- **Networking**: Visibilidad en comunidad Android/FOSS

### Riesgos y Mitigaciones

| Riesgo | Mitigación |
|--------|------------|
| Forking malicioso | Licencia GPLv3 + releases firmadas |
| Exposición de lógica | Mantener config de carriers privada |
| Carga de mantenimiento | Documentar que es proyecto personal |
| Competidores | Licencia copyleft protege contra uso comercial |

### Contenido Recomendado para Repo

- README detallado con screenshots
- Documentación arquitectural
- Roadmap público
- CONTRIBUTING.md con guidelines
- CODEOWNERS para control de merges

---

## 3. Monetización y Distribución

### CONSENSO: Estrategia Híbrida (Opción C)

**Fase 1: F-Droid First**
```
- Versión completa gratis
- Sin ads
- Comunidad técnica como early adopters
- Testeo en más dispositivos
- Genera credibilidad ética
```

**Fase 2: Play Store Freemium**
```
- Misma base de código
- Features premium via IAP
- Donación única ($3-5), no suscripción
- Sin ads nunca
```

### Features Premium Propuestos

| Feature | Justificación |
|---------|---------------|
| Backup en la nube | Conveniencia, requiere servidor |
| Regex avanzado | Power users |
| Scheduling/perfiles horarios | Uso avanzado |
| Estadísticas detalladas | Valor agregado |

### Modelo de Precios

- **Core**: Siempre gratis (bloqueo básico, backup local)
- **Premium**: $2.99-4.99 donación única
- **Sin fragmentación**: Feature flags, mismo código base

### Razones para NO usar Ads

1. Mala UX en app de privacidad/productividad
2. Audiencia técnica rechaza ads intrusivos
3. Ingresos marginales (<$100/mes) para nicho
4. Contradice ética de app de bloqueo

---

## Plan de Acción Consolidado

### Secuencia Recomendada

1. ~~**v0.3.0**: Implementar backup/restore local~~ **COMPLETADO 2026-01-17**
2. ~~**Post v0.3.0**: Publicar en GitLab (open source)~~ **COMPLETADO 2026-01-17**
3. ~~**Licencia GPLv3**~~ **COMPLETADO 2026-01-17**
4. ~~**Screenshots y README**~~ **COMPLETADO 2026-01-17**
5. ~~**F-Droid metadata (fastlane)**~~ **COMPLETADO 2026-01-18**
6. ~~**F-Droid**: Enviar MR a fdroiddata~~ **ENVIADO 2026-01-18** [MR #32092](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092)
7. **v0.4.0**: Backup cloud + features premium
8. **Play Store**: Lanzar con modelo freemium

### Proceso F-Droid (documentado 2026-01-18)

**Pasos para publicar en F-Droid:**

1. Fork https://gitlab.com/fdroid/fdroiddata
2. Crear archivo `metadata/com.callblocker.yml` (template en repo: `fdroid-metadata.yml`)
3. Crear Merge Request con título: `New app: Call Blocker`
4. Esperar revisión del equipo F-Droid (~1-2 semanas)
5. Una vez aprobado, aparece en F-Droid en el siguiente ciclo de build

**Requisitos cumplidos:**
- [x] Código fuente público (GitLab)
- [x] Licencia open source (GPLv3)
- [x] Sin dependencias propietarias
- [x] Fastlane metadata (es-MX, en-US)
- [x] Tag de versión (v0.3.0)
- [x] Screenshots

### Decisiones Pendientes

- [x] Licencia → **GPLv3** (decisión 2026-01-17)
- [x] Formato exacto de backup → **JSON con format_version 2** (v0.3.0)
- [x] Algoritmo de encriptación → **AES-256-GCM con PBKDF2** (v0.3.0)
- [ ] Proveedor cloud (Firebase vs Google Drive API)

---

## Notas del Debate

- **Duración**: ~3.5 minutos (10 rondas)
- **Providers**: DeepSeek (deepseek-chat) + OpenAI (gpt-4o)
- **Tokens totales**: ~17,000
- **Consenso alto**: Ambas IAs coincidieron en 90% de puntos
- **Diferencias menores**: Licencia (GPL vs Apache), timing exacto

---

## Resumen de Progreso (actualizado 2026-01-18)

### Completado ✅

| Item | Fecha | Detalles |
|------|-------|----------|
| v0.3.0 Backup/Restore | 2026-01-17 | AES-256-GCM, JSON format v2 |
| Repo público GitLab | 2026-01-17 | gitlab.com/abelardodiaz/web26-050-call-blocker |
| Licencia GPLv3 | 2026-01-17 | LICENSE file |
| Screenshots | 2026-01-17 | 5 capturas en docs/screenshots/ |
| README con galería | 2026-01-17 | Badges, screenshots, documentación |
| Fastlane metadata | 2026-01-18 | es-MX y en-US |
| F-Droid MR enviado | 2026-01-18 | MR #32092 |
| Tag v0.3.0 | 2026-01-18 | Para referencia F-Droid |

### Pendiente ⏳

| Item | Prioridad | Notas |
|------|-----------|-------|
| Aprobación F-Droid | Alta | Esperar revisión (~1-2 semanas) |
| v0.4.0 Backup cloud | Media | Decidir Firebase vs Google Drive |
| Play Store | Baja | Después de F-Droid |
| Tests unitarios | Baja | Mejorar cobertura |
| Iconos personalizados | Baja | Reemplazar placeholders |

### Siguiente Paso

**Esperar aprobación de F-Droid MR #32092**

Monitorear: https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092

---

*Generado via interactions-pro ping-pong-papas*
