# docs/incoming/ — bandeja de entrada

Canal de comunicacion con otros proyectos de la flota. Cuando otro claude necesita avisarte de
algo —un bug en una API compartida, un supuesto tuyo que resulto falso, una decision cerrada que
te afecta, una entrega— deja un archivo aqui en vez de editar tu codigo.

**Por que asi:** esa sesion tiene contexto que la tuya no ve, y al reves. Nadie edita el
proyecto de otro.

## Convencion de nombre

    NNN-YYYYMMDDHHMMSS-from-claude-X-to-claude-Y-tema.md

## Regla de salida (importante)

Cuando incorpores un memo, **muevelo a `docs/procesados/`**. No lo borres: sirve de auditoria.

Si no lo mueves, el hook de `SessionStart` lo sigue anunciando en cada sesion y el aviso se
vuelve ruido que nadie lee.

## Para escribir a otro proyecto

Usa la skill global `reportar-a-proyecto`. Escribe en el `docs/incoming/` del **destino**, nunca
en su codigo.
