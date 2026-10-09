# Reporte de usuaria (F-Droid, GrapheneOS): no bloquea nada y la pestaña "llamadas" queda vacia

**De:** prweb (hub, sesion de correo/contactos)
**Para:** web26-050
**Fecha:** 2026-10-09
**Tipo:** bug (reportado por usuaria, no reproducido aun)
**Urgencia:** conviene saber

---

## Resumen

Una usuaria de la version de F-Droid escribio a atc@redv6.com (autor "redv6.com" en la ficha).
En un Pixel con GrapheneOS, ni un numero completo ni prefijos bloquean, y la pestaña "llamadas"
de la app queda vacia: parece que `onScreenCall` ni siquiera se invoca.

> Este repo es publico: este memo no incluye nombre ni correo de la usuaria a proposito. Estan en
> el hub, `prweb/_tareas-claude/seguimiento-callblocker-bugs-usuarios/README.md`.

## Que reporto (texto de la usuaria, resumido)

- Dispositivo: Google Pixel con **GrapheneOS**, app instalada en el perfil principal.
- Le dio el **rol de filtrado de llamadas** (call screening) y permisos de Telefono, Registro de
  llamadas y Contactos.
- Bateria en "sin restricciones"; **servicio persistente activado**.
- Agrego un numero completo `+33…` (sin espacios, sin opcion de prefijo) con "bloqueo activo":
  **el telefono suena normal y la pestaña "llamadas" queda vacia**.
- Prefijos `0162` y `+33162` con "bloquear como prefijo": **tampoco bloquean**.
- Pregunta si hay incompatibilidad conocida con GrapheneOS o algun ajuste que falte.

No dijo version de la app, version de GrapheneOS, ni si el numero esta en sus contactos.

## Que esta verificado y que no

| Afirmacion | Estado |
|-----------|--------|
| El correo existe y dice lo de arriba | verificado (leido en Zoho redv6, 2026-10-09) |
| Se le respondio pidiendo issue en GitHub + si el numero esta en contactos | verificado (enviado desde atc@redv6.com 2026-10-09) |
| `onScreenCall` no se invoca para su llamada | **supuesto**: se infiere de la pestaña vacia, sin ver el codigo |
| Causa: numero guardado en contactos y la app no es el dialer por defecto | **supuesto, sin verificar** ni en docs de `CallScreeningService` ni en el codigo |
| Prefijos con codigo de pais fallan por normalizacion | **supuesto**; coincide con el issue abierto #9 |
| Algo especifico de GrapheneOS | **supuesto**, el menos probable (GrapheneOS usa la app de Telefono AOSP) |
| El usuario (dueño) ha visto sintomas parecidos en su Samsung, que se arreglaron con un ajuste que no recuerda | dicho por el usuario, sin detalle |

Nada de esto se reprodujo todavia. No se reviso el codigo de 050.

## Hipotesis, en orden

1. **El servicio no recibe la llamada.** En Android, el `CallScreeningService` del rol de screening
   podria no ser invocado para numeros que estan en los contactos si la app no es el dialer por
   defecto. Verificar en la documentacion oficial antes de darlo por hecho.
2. **Issue #9** "Prefix matching may mismatch with country-code normalization": explica lo de los
   prefijos `0162` / `+33162`, pero no la pestaña vacia.
3. Rol concedido pero revocado/no persistido, o comportamiento distinto en GrapheneOS.

## Como reproducirlo (propuesta, no ejecutada)

- GrapheneOS no tiene emulador precompilado (solo build desde fuente, ~300 GB / 32 GB RAM): no
  viable. Un **AVD de Android "puro"** se parece mas a GrapheneOS que el Samsung.
- `adb emu gsm call +33162XXXXXX` para simular la llamada entrante.
- Casos: (a) numero NO en contactos, (b) mismo numero en contactos, (c) prefijo `+33162` y `0162`.
- Ver si `onScreenCall` se dispara en cada caso (log) y si la pestaña "llamadas" registra algo.

## Sugerencia

Confirmar primero la hipotesis 1 (es la que explica todos los sintomas). Si es asi, la app podria
avisar en la UI cuando un numero bloqueado esta en contactos, o documentarlo en la ficha de F-Droid.

## Que se espera de ustedes

- Revisar cuando retomen el proyecto; no hay fecha comprometida con la usuaria.
- Si ella abre un issue en GitHub, enlazarlo aqui o al #9.
- Al incorporarlo, mover este memo a `docs/procesados/`. **No commitear datos personales** de
  usuarios en este repo publico.

---

**Contexto de origen:** `prweb/_tareas-claude/seguimiento-callblocker-bugs-usuarios/README.md` (Windows).
