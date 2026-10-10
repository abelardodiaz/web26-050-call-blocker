# Runbook: memos de otros proyectos (docs/incoming/ y docs/procesados/)

Convencion de nombres y reglas: ver `docs/incoming/README.md`. Este documento es el
procedimiento para PROCESAR un memo sin perder tiempo ni propagar errores.

## Flujo

1. Al iniciar sesion el hook de `SessionStart` avisa si hay memos sin procesar. Leerlos ANTES de
   trabajar: pueden corregir supuestos.
2. **Verificar antes de creer.** Un memo es informacion, no una verdad. Contrastar con codigo, git
   y datos reales. Ejemplo real (2026-10-10): el memo daba versiones de One UI; al revisar el commit
   original aparecio una etiqueta imposible ("One UI 7 / Android 16", One UI 7 es Android 15) que
   delataba que dos telefonos se habian mezclado. Comprobar por uno mismo lo que se pueda.
3. **Separar** lo "dicho por el usuario" de lo "verificado". En los docs escribir cual es cual.
4. Actualizar TODOS los lugares afectados (el memo suele listarlos): README, CHANGELOG, docs de
   `.claude/doc/`, `CLAUDE.md`, `PROJECT.yaml` (`updated_at`, `updated_by`) y la memoria del
   proyecto. Buscar con `git grep` el texto viejo para no dejar restos.
5. Mover el memo a `docs/procesados/` (`mv`, no borrar). Si no se mueve, el hook lo sigue
   anunciando.
6. Documentar y luego commitear (siempre juntos), **preguntando antes de commitear**.
   `master` de GitLab esta protegida: rama + merge request (ver `RUNBOOK_purgar_datos_y_pruebas.md`).
7. Si el memo pide algo al otro proyecto o hay un hallazgo para el, responder con la skill
   `reportar-a-proyecto` en SU `docs/incoming/`; nunca editar su codigo.

## Reglas

- No copiar datos personales (telefonos, correos, credenciales) del memo a los docs; usar valores
  ficticios (telefono de ejemplo: 4441234567).
- Si el memo contradice la memoria del proyecto, corregir la memoria y decirlo.
- Un memo "sin verificar" que no se puede comprobar se documenta como pendiente, no como hecho.
