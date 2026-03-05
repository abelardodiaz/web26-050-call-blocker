# F-Droid Submission - Call Blocker

**Fecha de envío:** 2026-01-18
**Estado:** Actualizando - applicationId renombrado a com.redv6.callblocker (2026-03-05)

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
- **Archivo creado:** `metadata/com.redv6.callblocker.yml`

### Original (F-Droid)
- **URL:** https://gitlab.com/fdroid/fdroiddata
- **Project ID:** 36528

### App Source
- **URL:** https://gitlab.com/abelardodiaz/web26-050-call-blocker
- **Tag:** v0.3.0
- **License:** GPL-3.0-only

---

## Contenido del Archivo Metadata

**Archivo:** `metadata/com.redv6.callblocker.yml`

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
  - versionName: 0.4.0
    versionCode: 17
    commit: <commit-hash-of-v0.4.0-tag>
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.4.0
CurrentVersionCode: 17
```

---

## Descripción del MR

```markdown
## App Info

**Name:** Call Blocker
**Package:** com.redv6.callblocker
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
- [x] Version tag exists (v0.4.0)
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
| Tag de versión | ✅ | v0.4.0 |
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
| 2026-01-18 21:59 | Comentario añadido sobre v0.3.1 y v0.3.2 disponibles |
| 2026-01-23 07:38 | Rebase sobre upstream/master completado |
| 2026-01-23 07:42 | Comentario notificando rebase al reviewer |
| 2026-03-05 | applicationId renombrado de com.callblocker a com.redv6.callblocker |
| 2026-03-05 | Metadata actualizada a v0.4.0 con nuevo applicationId |

## Revisiones Solicitadas

### 2026-01-18 - linsui

**Comentario 1:** "Use the commit hash"
- **Problema:** Usamos `commit: v0.3.0` (tag name)
- **Solución:** Cambiado a `commit: bf21a787776a8206426aac3c0b3068c77946189e`

**Comentario 2:** "Follow the MR template"
- **Problema:** Descripción del MR no seguía el template de F-Droid
- **Solución:** Comentario agregado explicando los cambios

---

---

## Mantenimiento del MR: Rebase

### Por qué se necesita rebase

El repositorio `fdroid/fdroiddata` recibe cientos de commits diarios (bots de actualización automática). Si tu branch se queda atrás, GitLab muestra `need_rebase` y el label `waiting-for-upstream` aparece en el MR. Los reviewers no procesan MRs que necesitan rebase.

### Cómo detectar que se necesita rebase

```bash
# Verificar estado del MR via API
curl -s "https://gitlab.com/api/v4/projects/36528/merge_requests/32092" | \
  python3 -c "import json,sys; d=json.loads(sys.stdin.read()); print(f'Merge status: {d[\"detailed_merge_status\"]}\nLabels: {d[\"labels\"]}')"
```

Si `detailed_merge_status` es `need_rebase` o `conflict`, hay que rebaser.

### Proceso de rebase paso a paso

```bash
# 1. Clonar el fork (con historial completo)
git clone https://gitlab.com/abelardodiaz/fdroiddata.git /tmp/fdroiddata
cd /tmp/fdroiddata

# 2. Checkout la rama del MR
git checkout add-call-blocker

# 3. Agregar upstream (el repo original de F-Droid)
git remote add upstream https://gitlab.com/fdroid/fdroiddata.git

# 4. Fetch solo master del upstream
git fetch upstream master

# 5. Rebase sobre upstream/master
git rebase upstream/master

# 6. Push forzado (seguro) al fork
git push --force-with-lease origin add-call-blocker

# 7. Limpiar
rm -rf /tmp/fdroiddata
```

### Notas importantes sobre el rebase

- **Pipeline del fork falla** - Es normal. Los forks no tienen los runners de F-Droid. El pipeline real lo ejecutan los maintainers en el proyecto upstream.
- **Project ID de fdroiddata** - Es `36528` (necesario para API calls).
- **Autenticación** - Los endpoints de notes/discussions requieren token. El MR metadata es público.
- **Después del rebase** - Dejar un comentario avisando al reviewer:
  ```bash
  curl -s -X POST \
    -H "PRIVATE-TOKEN: <tu-token>" \
    -H "Content-Type: application/json" \
    -d '{"body": "Rebased on latest master. Ready for review."}' \
    "https://gitlab.com/api/v4/projects/36528/merge_requests/32092/notes"
  ```

### Frecuencia recomendada

Si el MR lleva más de 5 días sin actividad del reviewer, verificar si necesita rebase. El repo fdroiddata se mueve rápido.

---

## Monitoreo via API (referencia rápida)

```bash
# Estado general del MR
curl -s "https://gitlab.com/api/v4/projects/36528/merge_requests/32092" | python3 -m json.tool | head -20

# Leer comentarios (requiere token)
curl -s -H "PRIVATE-TOKEN: <token>" \
  "https://gitlab.com/api/v4/projects/36528/merge_requests/32092/notes?sort=desc&per_page=5" | \
  python3 -c "
import json,sys
for n in json.loads(sys.stdin.read()):
    if not n.get('system'):
        print(f'[{n[\"created_at\"][:16]}] @{n[\"author\"][\"username\"]}: {n[\"body\"][:100]}')
"

# Publicar comentario
curl -s -X POST \
  -H "PRIVATE-TOKEN: <token>" \
  -H "Content-Type: application/json" \
  -d '{"body": "Tu mensaje aquí"}' \
  "https://gitlab.com/api/v4/projects/36528/merge_requests/32092/notes"
```

---

*Documento actualizado: 2026-03-05 - Call Blocker v0.4.0*
