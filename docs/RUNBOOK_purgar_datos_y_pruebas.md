# Runbook: purgar datos personales del historial y probar en emulador

Metodo aprendido el 2026-10-10 para no repetir los tropiezos. Contexto: un telefono personal
quedo escrito en docs y llego al historial publico (GitLab, GitHub, F-Droid).

## 0. Regla preventiva (lo mas barato)

Nunca escribir numeros, correos ni credenciales reales en docs, CHANGELOG, tests ni mensajes de
commit. En ejemplos usar el numero ficticio **4441234567**. Antes de commitear docs:
`git diff --cached | grep -E "<tus numeros reales>"`.

## 1. Purgar un dato del historial git

Orden que funciona (y por que):

1. **Respaldo**: `git clone --mirror <repo> backup.git` (fuera del repo; contiene el dato real,
   borrarlo al final).
2. **Reescribir en un clon aparte** (nunca en el repo de trabajo):
   `git clone --no-local <repo> work && cd work`
   `git filter-repo --replace-text repl.txt --replace-message repl.txt --force`
   con `repl.txt` = `VALOR_REAL==>VALOR_FICTICIO`.
3. **Verificar**: `git log --all -S"<fragmento>"` debe dar 0; ramas y tags intactos.
4. **Force-push** a los remotos: `git push --force <remoto> --all` y `--tags` (no `--mirror`:
   GitHub rechaza `refs/pull/*`).
5. **Resincronizar el repo local**: `git fetch --prune --force --tags`, `git reset --hard
   origin/master`, ramas locales `git branch -f`, `git reflog expire --expire=now --all &&
   git gc --prune=now`.

### Trampas conocidas

- **GitLab protege `master`**: el force-push se rechaza. Hay que activar "Allowed to force push"
  en Settings > Repository > Protected branches (solo el dueño, es cambio de seguridad) y volver
  a protegerla despues. Los tags SI se actualizan aunque las ramas no: no dejar el remoto a medias.
- **El force-push NO borra los commits viejos del servidor.** Siguen sirviendose por URL de SHA
  (`/commit/<sha>` y `.diff` dan 200 con el dato).
- **GitLab**: "Prune unreachable objects" solo NO basta (guarda commits referenciados por MRs,
  pipelines, keep-around). Usar Settings > Repository > Repository maintenance > **Remove
  blobs** con los IDs de los blobs afectados, esperar el correo "history rewrite completed",
  luego "Run housekeeping", esperar ~30 min y "Prune unreachable objects".
  IDs de blobs: recorrer `git rev-list --all` en el backup y `git cat-file -p <blob> | grep`.
- **GitHub**: no hay boton. Abrir ticket en help.github.com/support/contact (Repositorios >
  Funciones del repositorio > Datos). Datos que piden: repo, PRs afectados (0 si no hay
  `refs/pull/*`), forks, *First Changed Commit* (el primer commit viejo cuyo hash cambio, sale del
  `commit-map` de filter-repo), sin LFS. Pueden rechazar si no es una credencial.
- **Forks y clones ajenos** conservan el dato; no hay forma de limpiarlos.
- Lo unico que invalida de verdad el dato es **rotarlo** (cambiar el numero/credencial).

### Efecto en F-Droid

`fdroiddata` pinea **hashes de commit** (no tags) en `Builds:`. Tras reescribir, esos hashes
dejan de existir. Hay que abrir un MR a `fdroid/fdroiddata` cambiando cada `commit:` por el hash
nuevo (tabla viejo->nuevo: `git rev-parse <tag>^{commit}` en backup vs repo nuevo). Comprobar que
`app/` es identico (`git rev-parse <tag>:app` igual antes y despues): asi el APK no cambia.
- El fork de `fdroiddata` se queda MUY atrasado (17k commits): antes de empujar una rama usar el
  boton "Update fork"; un clon `--depth 1` empujado a un fork desactualizado da
  `shallow update not allowed`.
- MR por linea de comandos: `git push -o merge_request.create -o merge_request.target_project=fdroid/fdroiddata -o merge_request.target=master`
  (el cuerpo del commit pasa a ser la descripcion).

## 2. Cambios en `master` de GitLab (protegida)

No se hace push directo: rama + `git push -o merge_request.create -o merge_request.target=master`,
merge (terminal o web), luego `git pull` y `git push github master` (el espejo no se actualiza solo).

**Merge desde terminal (sin navegador).** Token en `~/.config/gitlab-callblocker-token`
(permisos 600, NUNCA escribirlo en docs ni en el chat). Es un token "fine-grained" limitado a
este proyecto, permisos Merge Request: Merge + Read. Creado 2026-10-10, caduca 2026-11-09.
```bash
T=$(cat ~/.config/gitlab-callblocker-token); P=abelardodiaz%2Fweb26-050-call-blocker
curl -s -X PUT -H "PRIVATE-TOKEN: $T" \
  "https://gitlab.com/api/v4/projects/$P/merge_requests/<IID>/merge?should_remove_source_branch=true" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('state'),d.get('message',''))"
```
Si da 401/403 el token caduco o fue revocado: volver a la web (Merge) y renovarlo (ver
`docs/BACKLOG.md`). Si `should_remove_source_branch` falla por permisos, quitarlo y borrar la rama a mano.
Crear el token: User Settings > Access tokens > Generate token > Fine-grained; el usuario pulsa
"Generate" y lo guarda con `read -rs` en el prompt (`!`), asi no pasa por el chat.

## 3. Probar CallScreeningService en emulador AOSP

Nada de esto debe montarse en server005 (disco al 91 %). Se uso **server602b** (via laptop-g3).
Detalle de instalacion y arranque con limites en la memoria del proyecto `emulator_602b`; resumen:

- Servidores sin KVM no sirven (085). Verificar `/dev/kvm` y flags `vmx|svm` antes.
- Descargas en el servidor: `curl -4` (IPv6 de 602b da 404 en dl.google.com). Verificar sumas
  (sha1 del indice `repository2-3.xml`, sha256 de Adoptium).
- Usar `cmdline-tools` **10406996** (el 16111833 intenta bajar un "Android CLI" y falla).
- JDK en `~/Android/jdk` sin instalar paquetes; solo hizo falta `libx11-xcb1` del sistema.
- Emulador como servicio `systemd-run` con `MemoryMax`, `CPUQuota` y `Nice` (es un servidor de
  BD en produccion). Parar con `sudo systemctl stop aosp-emu`.
- No mandar APKs grandes por laptop-g3 (~40 KB/s): bajar el de F-Droid directo en el servidor.
- Prueba: `cmd role add-role-holder android.app.role.CALL_SCREENING com.callblocker 0`, crear
  contacto, bloquear su numero (UI via `uiautomator dump` + `input tap`), `adb emu gsm call <n>`,
  leer `logcat -s CallBlockerScreening:D` y `dumpsys telecom` (`Reject` + `mCallBlockReason`).

## 4. Errores de herramienta que cuestan tiempo

- `pkill -f "<patron>"` se mata a si mismo si el patron aparece en su propia linea de comandos
  (usar `tim[e]`).
- Cadenas `ssh` anidadas pierden comillas: escribir un script y copiarlo con `scp`.
- `set -e` + `read X < archivo` sin salto de linea final aborta en silencio.
