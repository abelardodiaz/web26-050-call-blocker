# F-Droid Submission - Call Blocker

**Fecha de envío:** 2026-01-18
**Estado:** Pendiente de revisión

---

## Información del Merge Request

| Campo | Valor |
|-------|-------|
| **MR Number** | #32092 |
| **URL** | https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092 |
| **Branch** | `add-call-blocker` |
| **Target** | `fdroid/fdroiddata:master` |
| **Autor** | abelardodiaz |

---

## Repositorios Involucrados

### Fork (tu copia)
- **URL:** https://gitlab.com/abelardodiaz/fdroiddata
- **Branch:** add-call-blocker
- **Archivo creado:** `metadata/com.callblocker.yml`

### Original (F-Droid)
- **URL:** https://gitlab.com/fdroid/fdroiddata
- **Project ID:** 36528

### App Source
- **URL:** https://gitlab.com/abelardodiaz/web26-050-call-blocker
- **Tag:** v0.3.0
- **License:** GPL-3.0-only

---

## Contenido del Archivo Metadata

**Archivo:** `metadata/com.callblocker.yml`

```yaml
Categories:
  - Phone & SMS
License: GPL-3.0-only
AuthorName: redv6.com
AuthorWebSite: https://redv6.com
SourceCode: https://gitlab.com/abelardodiaz/web26-050-call-blocker
IssueTracker: https://gitlab.com/abelardodiaz/web26-050-call-blocker/-/issues

AutoName: Call Blocker

RepoType: git
Repo: https://gitlab.com/abelardodiaz/web26-050-call-blocker.git

Builds:
  - versionName: 0.3.0
    versionCode: 13
    commit: bf21a787776a8206426aac3c0b3068c77946189e
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.3.0
CurrentVersionCode: 13
```

---

## Descripción del MR

```markdown
## App Info

**Name:** Call Blocker
**Package:** com.callblocker
**License:** GPL-3.0-only
**Source:** https://gitlab.com/abelardodiaz/web26-050-call-blocker

## Description

Android app to block unwanted calls using CallScreeningService.

**Features:**
- Block specific numbers
- Block by prefix (e.g., 800 blocks all 800xxxxxxx)
- Blocked calls history
- Dual SIM support
- Backup/restore with optional AES-256 encryption
- No ads, no tracking
- 100% open source

**Compatibility:** Android 9-17 (API 28+)

## Checklist

- [x] Open source license (GPL-3.0)
- [x] No proprietary dependencies
- [x] Fastlane metadata included
- [x] Version tag exists (v0.3.0)
- [x] Builds successfully
```

---

## Requisitos Cumplidos

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Código fuente público | ✅ | GitLab público |
| Licencia open source | ✅ | GPL-3.0-only |
| Sin Google Play Services | ✅ | No hay dependencias propietarias |
| Sin Firebase | ✅ | - |
| Sin Ads (AdMob) | ✅ | - |
| Fastlane metadata | ✅ | es-MX y en-US |
| Tag de versión | ✅ | v0.3.0 |
| Screenshots | ✅ | 5 capturas |

---

## Proceso de Revisión

### Qué esperar

1. **Pipeline automático** - F-Droid ejecutará checks automáticos
2. **Revisión manual** - Un maintainer revisará el código y metadata
3. **Posibles comentarios** - Pueden pedir cambios o aclaraciones
4. **Aprobación** - Una vez aprobado, se merge a master
5. **Build cycle** - La app aparece en F-Droid en el siguiente ciclo (~1 semana después del merge)

### Tiempo estimado

- Revisión inicial: 1-2 semanas
- Si hay cambios solicitados: +1 semana por iteración
- Publicación post-aprobación: ~1 semana

---

## Próximos Pasos

1. **Monitorear MR** - Revisar notificaciones de GitLab
2. **Responder comentarios** - Si el equipo F-Droid solicita cambios
3. **Actualizar metadata** - Para futuras versiones, actualizar el yml

---

## Comandos Útiles

### Ver estado del MR
```bash
curl -s "https://gitlab.com/api/v4/projects/36528/merge_requests/32092" | jq '.state, .merge_status'
```

### Actualizar metadata para nueva versión
1. Editar `fdroid-metadata.yml` en el repo de la app
2. Crear nuevo tag (ej: `v0.4.0`)
3. F-Droid detectará automáticamente la nueva versión (AutoUpdateMode: Version)

---

## Historial

| Fecha | Evento |
|-------|--------|
| 2026-01-18 06:12 | Fork de fdroiddata creado |
| 2026-01-18 06:14 | Branch `add-call-blocker` creado |
| 2026-01-18 06:15 | Archivo `com.callblocker.yml` agregado |
| 2026-01-18 06:16 | MR #32092 enviado |
| 2026-01-18 06:45 | Reviewer (linsui) solicita cambios: usar commit hash |
| 2026-01-18 20:43 | Metadata actualizada con commit hash |

## Revisiones Solicitadas

### 2026-01-18 - linsui

**Comentario 1:** "Use the commit hash"
- **Problema:** Usamos `commit: v0.3.0` (tag name)
- **Solución:** Cambiado a `commit: bf21a787776a8206426aac3c0b3068c77946189e`

**Comentario 2:** "Follow the MR template"
- **Problema:** Descripción del MR no seguía el template de F-Droid
- **Solución:** Comentario agregado explicando los cambios

---

*Documento generado automáticamente - Call Blocker v0.3.0*
