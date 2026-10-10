# BACKLOG - pendientes del proyecto

Lista unica de pendientes. Aqui van los pendientes operativos (revisiones, esperas, limpiezas);
NO en la memoria de Claude. Al cerrar un item, moverlo a "Hecho" con fecha.
(Issues de codigo detectados en revision: `.claude/doc/BACKLOG.md`.)

## Pendiente

### Purga del numero personal (abierto 2026-10-10)
Contexto y metodo: `docs/RUNBOOK_purgar_datos_y_pruebas.md`.
- [ ] Revisar si el commit viejo `09e85a9f8568fc80ab956d616969cf4df2a1752a` ya dio 404 en GitLab (`/-/commit/<hash>` y `.diff`) y GitHub. Prune de GitLab pulsado ~10:40 pm del 2026-10-10; seguia en 200.
- [ ] Ticket a soporte de GitLab ABIERTO 2026-10-10 (#759398, https://support.gitlab.com/hc/en-us/requests/759398, usuario gratuito, categoria 'user accounts and login issues' / Other / Sev 4). Esperar respuesta por correo y luego verificar que el commit viejo da 404.
- [ ] Revisar respuesta de soporte de GitHub (gmail-personal).
- [ ] El usuario rota el numero con la compania (la purga no lo sustituye).
- [ ] Al confirmar todo: borrar `backup.git` (tiene el numero real) del scratchpad de server005 y `~/Android` (3.8 GB) en server602b.

### F-Droid (fdroiddata)
- [ ] MR !51892 (hashes nuevos de 0.3.3/0.3.4 + datos de autor): revisar si fue mergeado.
- [ ] Revisar si f-droid.org/packages/com.callblocker/ publica 0.3.5 / 0.3.6 (a 2026-10-10 solo estaba 0.3.4). Si no aparecen en 2-3 dias, algo se trabo.
- [ ] Opcional: probar el APK oficial 0.3.6 en el emulador de 602b.

### S26
- [ ] Actualizar el S26 (tiene 0.3.4 de F-Droid) cuando F-Droid publique 0.3.5/0.3.6, desde la app de F-Droid. NO instalar la APK debug encima: firma distinta (INSTALL_FAILED_UPDATE_INCOMPATIBLE), obligaria a desinstalar y perder datos. Decision del usuario 2026-10-10: esperar a F-Droid.

## Hecho
- 2026-10-10: historia git reescrita y force-push a GitLab y GitHub; runbooks mergeados; MR F-Droid abierto.
