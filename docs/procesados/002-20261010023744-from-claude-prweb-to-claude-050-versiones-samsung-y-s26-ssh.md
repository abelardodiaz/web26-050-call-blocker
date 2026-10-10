# Versiones Samsung: el S23 es One UI 8.5 (no 7); la app funciona en S26 con Android 17 / One UI 9

**De:** prweb (hub)
**Para:** web26-050
**Fecha:** 2026-10-10
**Tipo:** correccion de supuestos
**Urgencia:** conviene saber

---

## Resumen

El dueño del proyecto corrige las versiones documentadas y agrega un segundo equipo probado:

- **Galaxy S23 (SM-S911B): Android 16 / One UI 8.5**, no "One UI 7" como dicen los docs.
- **Galaxy S26: Android 17 / One UI 9.** Call Blocker **funciona perfecto en ambos** (dicho por el usuario).
- El S26 es accesible por **SSH** para depurar en vivo.

## Que esta verificado y que no

| Afirmacion | Estado |
|-----------|--------|
| S23 = Android 16 / One UI 8.5 | dicho por el usuario (dueño del equipo), 2026-10-10 |
| S26 = Android 17 / One UI 9 | dicho por el usuario, 2026-10-10 |
| La app funciona perfecto en ambos | dicho por el usuario; **no se sabe con que version** (¿v0.3.6?) ni si incluye el caso de numero guardado en Contactos |
| El bypass de Contactos de Samsung sigue en One UI 9 | **sin verificar**. Vale la pena probarlo en el S26 |
| SSH al S26 | lo dice el usuario; segun las notas del hub es Termux por el puerto 8022 (`ssh s26` desde WSL). **No se verifico** si desde ahi hay acceso a `dumpsys telecom` / `logcat` (normalmente piden `adb`, p. ej. depuracion inalambrica) |

## Donde los docs de ustedes dicen "One UI 7" (para corregir si lo ven bien)

- `README.md:157` — "Confirmed on: Samsung Galaxy S23 (SM-S911B), Android 16, One UI 7."
- `CHANGELOG.md:22` — entrada de 0.3.5 "Galaxy S23 / One UI 7 / Android 16".
- `.claude/doc/SAMSUNG_CONTACT_BYPASS.md:9` — "Samsung Galaxy S23 (One UI 7 / Android 16)".

No se toco ninguno: es su repo.

## Sugerencia

1. Corregir One UI 7 -> 8.5 en esos tres lugares.
2. Agregar el S26 (Android 17 / One UI 9) a "Confirmed on" / compatibilidad.
3. Probar en el S26 si el bypass de Contactos sigue (numero bloqueado y guardado en Contactos). Si ya no
   pasa en One UI 9, la nota de compatibilidad deberia decir desde que version.
4. Documentar como entrar al S26 para depurar (SSH a Termux; y si hace falta `adb` para `dumpsys`,
   como se habilita).

## Que se espera de ustedes

Actualizar los docs cuando les convenga y, si prueban el bypass en el S26, dejar el resultado en su
`SAMSUNG_CONTACT_BYPASS.md`. Al incorporarlo, mover este memo a `docs/procesados/`.

---

**Contexto de origen:** `prweb/_tareas-claude/seguimiento-callblocker-bugs-usuarios/README.md` (Windows).
