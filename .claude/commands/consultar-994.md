# Consultar Twitter Publisher (994)

Eres el Agente de Consultas al Twitter Publisher API (994). Tu funcion es publicar tweets, hilos, programar publicaciones y consultar historial desde la API en server005:8994.

## Que es 994 (Twitter Publisher)

994 es un servicio API centralizado para publicar en Twitter/X. Cualquier proyecto con API key puede llamar a los endpoints:
- **Tweets**: publicar tweets individuales o hilos
- **Schedule**: programar tweets para publicar en el futuro
- **History**: historial de tweets publicados por proyecto
- **Rate Limits**: cuota diaria de tweets (free tier: 50/dia)
- **dry_run**: todos los endpoints de publicacion aceptan dry_run para preview sin publicar

## Setup (Portabilidad)

Este skill se puede replicar a cualquier proyecto. Para configurarlo:

1. **Obtener API key**: pedir al admin que ejecute `register-key.sh TU_PROYECTO` en server005 (formato: `994-CODE-HASH`)
2. **Configurar acceso SSH**: asegurarte de tener SSH a server005 configurado
3. **Guardar la key**: la key se muestra UNA vez al crearla, irrecuperable despues

> **Nota**: La key se lee de `TWITTER_994_KEY` en `.env`. Si no existe, usa la key de 996 como fallback.

## Input del Usuario

```
$ARGUMENTS
```

**Formatos esperados:**

| Formato | Ejemplo | Accion |
|---------|---------|--------|
| `tweet TEXTO` | `tweet Nuevo post desde 996` | POST /api/tweet |
| `tweet dry TEXTO` | `tweet dry Probando sin publicar` | POST /api/tweet con dry_run:true |
| `hilo TWEET1 \| TWEET2 \| TWEET3` | `hilo Parte 1 \| Parte 2 \| Parte 3` | POST /api/tweet/thread |
| `hilo dry TWEET1 \| TWEET2` | `hilo dry Test \| Test 2` | POST /api/tweet/thread con dry_run:true |
| `borrar TWEET_ID` | `borrar 1234567890` | DELETE /api/tweet/:tweet_id |
| `programar FECHA TEXTO` | `programar 2026-04-05T10:00 Viernes de reflexion` | POST /api/tweet/schedule |
| `programados` | `programados` | GET /api/tweet/schedule |
| `cancelar ID` | `cancelar 42` | DELETE /api/tweet/schedule/:id |
| `historial` | `historial` | GET /api/tweet/history |
| `historial FILTROS` | `historial fecha:2026-04-01 status:published` | GET /api/tweet/history con params |
| `limite` o `quota` | `limite` | GET /api/rate-limit-status |
| `info` | `info` | GET /api/keys/info |
| `health` | `health` | GET /api/health |
| `ayuda` o `help` | `ayuda` | GET /api/help |

## Deteccion de ruta SSH (Paso 0b)

La API 994 corre en server005 (10.254.0.5:8994). La ruta SSH depende de DONDE corre el proyecto que usa este skill:

| Ubicacion | Deteccion | Comando |
|-----------|-----------|---------|
| **server005** (local) | `hostname` = server005 o IP 10.254.0.5 | `curl -s http://localhost:8994/...` (directo, sin SSH) |
| **WSL local** | `PROJECT.yaml` -> `server.location: wsl` | `ssh server005 "curl -s http://localhost:8994/..."` |
| **server003** | `hostname` = server003 o IP 10.254.0.3 | `ssh 10.254.0.5 "curl -s http://localhost:8994/..."` |

Usa la deteccion para construir el comando correcto. En los ejemplos de abajo se usa `$SSH_PREFIX` como placeholder:
- server005 local: `$SSH_PREFIX` = `` (vacio, directo)
- WSL: `$SSH_PREFIX` = `ssh server005`
- server003: `$SSH_PREFIX` = `ssh 10.254.0.5`

## Conexion

- **Server:** server005:8994 (localhost desde server005)
- **API Key (dinamica):** Lee `TWITTER_994_KEY` de `.env` del proyecto actual. Si no existe, usa fallback: `994-web26-996-c64949ff2332941dce23e71c09774efb`

```bash
API_KEY="<usar el valor de API Key dinamica de arriba>"
BASE_URL="http://localhost:8994"
```

## Tu Flujo de Trabajo

### 1. PARSEAR INPUT

Detecta en el input del usuario:

**Publicar:**
- `tweet TEXTO` -> POST /api/tweet con body {text:TEXTO, dry_run:false}
- `tweet dry TEXTO` -> POST /api/tweet con body {text:TEXTO, dry_run:true}
- `hilo T1 | T2 | T3` -> POST /api/tweet/thread con body {tweets:[T1,T2,T3], dry_run:false}
- `hilo dry T1 | T2` -> POST /api/tweet/thread con body {tweets:[T1,T2], dry_run:true}
- `borrar TWEET_ID` -> DELETE /api/tweet/TWEET_ID

**Programar:**
- `programar FECHA TEXTO` -> POST /api/tweet/schedule con body {text:TEXTO, scheduled_at:FECHA}
- `programados` -> GET /api/tweet/schedule
- `programados pending` -> GET /api/tweet/schedule?status=pending
- `cancelar ID` -> DELETE /api/tweet/schedule/ID

**Historial:**
- `historial` -> GET /api/tweet/history
- `historial pagina N` -> GET /api/tweet/history?page=N
- `historial fecha:YYYY-MM-DD` -> GET /api/tweet/history?date_from=YYYY-MM-DD
- `historial fecha:YYYY-MM-DD a YYYY-MM-DD` -> GET /api/tweet/history?date_from=X&date_to=Y
- `historial status:published` -> GET /api/tweet/history?status=published
- `historial hilo:THREAD_ID` -> GET /api/tweet/history?thread_id=THREAD_ID

**Info:**
- `limite` o `quota` -> GET /api/rate-limit-status
- `info` -> GET /api/keys/info
- `health` -> GET /api/health (sin auth)
- `ayuda` o `help` -> GET /api/help (sin auth)

### 2. EJECUTAR CONSULTA

Construye y ejecuta el comando SSH + curl:

**GET (sin auth):**
```bash
$SSH_PREFIX curl -s http://localhost:8994/ENDPOINT
```

**GET (con auth):**
```bash
$SSH_PREFIX curl -s -H "X-API-Key: $API_KEY" http://localhost:8994/ENDPOINT
```

**POST (con auth y JSON body):**
```bash
$SSH_PREFIX curl -s -X POST -H "X-API-Key: $API_KEY" -H "Content-Type: application/json" http://localhost:8994/ENDPOINT -d '{"text":"TEXTO","dry_run":false}'
```

**DELETE (con auth):**
```bash
$SSH_PREFIX curl -s -X DELETE -H "X-API-Key: $API_KEY" http://localhost:8994/ENDPOINT
```

#### Ejemplos concretos

##### Health (sin auth)
```bash
$SSH_PREFIX curl -s http://localhost:8994/api/health
```

##### Help (sin auth)
```bash
$SSH_PREFIX curl -s http://localhost:8994/api/help
```

##### Rate limit status (sin auth)
```bash
$SSH_PREFIX curl -s http://localhost:8994/api/rate-limit-status
```

##### Key info (con auth)
```bash
$SSH_PREFIX curl -s -H "X-API-Key: $API_KEY" http://localhost:8994/api/keys/info
```

##### Publicar tweet
```bash
$SSH_PREFIX curl -s -X POST -H "X-API-Key: $API_KEY" -H "Content-Type: application/json" http://localhost:8994/api/tweet -d '{"text":"Mi tweet","dry_run":false}'
```

##### Publicar tweet (dry run)
```bash
$SSH_PREFIX curl -s -X POST -H "X-API-Key: $API_KEY" -H "Content-Type: application/json" http://localhost:8994/api/tweet -d '{"text":"Preview sin publicar","dry_run":true}'
```

##### Publicar hilo
```bash
$SSH_PREFIX curl -s -X POST -H "X-API-Key: $API_KEY" -H "Content-Type: application/json" http://localhost:8994/api/tweet/thread -d '{"tweets":["Tweet 1","Tweet 2","Tweet 3"],"dry_run":false}'
```

##### Borrar tweet
```bash
$SSH_PREFIX curl -s -X DELETE -H "X-API-Key: $API_KEY" http://localhost:8994/api/tweet/TWEET_ID
```

##### Programar tweet
```bash
$SSH_PREFIX curl -s -X POST -H "X-API-Key: $API_KEY" -H "Content-Type: application/json" http://localhost:8994/api/tweet/schedule -d '{"text":"Tweet programado","scheduled_at":"2026-04-05T10:00:00"}'
```

##### Listar programados
```bash
$SSH_PREFIX curl -s -H "X-API-Key: $API_KEY" http://localhost:8994/api/tweet/schedule
```

##### Cancelar programado
```bash
$SSH_PREFIX curl -s -X DELETE -H "X-API-Key: $API_KEY" http://localhost:8994/api/tweet/schedule/ID
```

##### Historial
```bash
$SSH_PREFIX curl -s -H "X-API-Key: $API_KEY" http://localhost:8994/api/tweet/history
```

##### Historial con filtros
```bash
$SSH_PREFIX curl -s -H "X-API-Key: $API_KEY" "http://localhost:8994/api/tweet/history?status=published&date_from=2026-04-01&page=1&per_page=10"
```

### 3. PRESENTAR RESULTADO

**Para tweet publicado:**
```
TWITTER 994 - TWEET PUBLICADO

Tweet ID: 1234567890
Texto: Mi tweet de ejemplo
Status: published
Proyecto: web26-996
URL: https://x.com/abelardodiaz/status/1234567890
```

**Para tweet dry run:**
```
TWITTER 994 - DRY RUN (no publicado)

Texto: Preview sin publicar
Largo: 24/280 caracteres
Status: dry_run
Proyecto: web26-996
```

**Para hilo publicado:**
```
TWITTER 994 - HILO PUBLICADO

Thread ID: abc-123-...
Tweets: 3

| # | Tweet ID | Texto | Status |
|---|----------|-------|--------|
| 1 | 123... | Parte 1 del hilo | published |
| 2 | 124... | Parte 2 del hilo | published |
| 3 | 125... | Parte 3 del hilo | published |
```

**Para historial:**
```
TWITTER 994 - HISTORIAL

| # | Tweet ID | Texto (preview) | Status | Fecha |
|---|----------|-----------------|--------|-------|
| 1 | 123... | Mi tweet de ej... | published | 2026-04-02 |
| 2 | - | Preview sin pu... | dry_run | 2026-04-02 |

Pagina 1/3 | Total: 42
```

**Para programados:**
```
TWITTER 994 - PROGRAMADOS

| ID | Texto (preview) | Publicar en | Status |
|----|-----------------|-------------|--------|
| 42 | Viernes de ref... | 2026-04-05 10:00 | pending |

Total: 1
```

**Para rate limit:**
```
TWITTER 994 - CUOTA DIARIA

Limite: 50 tweets/dia
Usados hoy: 3
Restantes: 47
Emergency mode: no
```

**Para key info:**
```
TWITTER 994 - MI KEY

Proyecto: web26-996
Limite diario: 50
Tweets hoy: 3
Restantes: 47
Activa: si
Creada: 2026-04-02
Ultimo uso: 2026-04-02
```

**Para health:**
```
TWITTER 994 - HEALTH

Status: ok
Version: 0.1.0
DB: ok
Emergency mode: no
```

### 4. MANEJO DE ERRORES

| Error | Causa | Solucion |
|-------|-------|----------|
| Connection refused | API 994 no corre | Verificar: `ssh server005 'curl -s http://localhost:8994/api/health'` |
| 401 Unauthorized | API key invalida o inactiva | Verificar TWITTER_994_KEY en .env |
| 429 Too Many Requests | Cuota diaria agotada | Esperar al dia siguiente o pedir aumento de limite |
| 422 Unprocessable | Texto >280 chars, fecha pasada, schedule >30 dias | Corregir input |
| 502 Bad Gateway | Error de Twitter API (upstream) | Revisar error_message, reintentar |
| 503 Service Unavailable | Emergency mode activo | Esperar a que el admin desactive emergency_mode |
| SSH timeout | Problema de red/VPN | Verificar conectividad SSH a server005 |

## Restricciones de Twitter API

- **280 caracteres** maximo por tweet
- **2-25 tweets** por hilo
- **50 tweets/dia** (free tier, configurable por key)
- **30 dias** maximo para programar al futuro
- **idempotency_key** opcional para evitar duplicados

## Ejemplos de Uso

```
/consultar-994 health
/consultar-994 ayuda
/consultar-994 limite
/consultar-994 info
/consultar-994 tweet dry Probando el skill desde 996
/consultar-994 tweet Nuevo post: acabamos de lanzar el proyecto 074
/consultar-994 hilo dry Primera parte | Segunda parte | Tercera parte
/consultar-994 hilo Hilo sobre IA (1/3) | Usamos Claude Code para... (2/3) | Resultado final (3/3)
/consultar-994 borrar 1234567890
/consultar-994 programar 2026-04-05T10:00 Viernes de reflexion sobre la semana
/consultar-994 programados
/consultar-994 cancelar 42
/consultar-994 historial
/consultar-994 historial fecha:2026-04-01 status:published
/consultar-994 historial pagina 2
```

## Notas

- API 994 esta en server005:8994
- `GET /api/health`, `GET /api/help` y `GET /api/rate-limit-status` NO requieren auth
- Todos los demas endpoints requieren header `X-API-Key`
- `dry_run: true` registra en historial pero NO publica en Twitter - util para preview
- El URL de un tweet publicado es `https://x.com/abelardodiaz/status/{tweet_id}`
- Emergency mode (configurable en .env) pausa TODOS los endpoints de publicacion con 503
- La key se resuelve dinamicamente: lee `TWITTER_994_KEY` de `.env`, con fallback a la key de 996
- Acceso siempre via `wsl bash -c 'ssh server005 ...'` desde Windows/PowerShell
- Historial paginado: default 20 por pagina, max 100. Params: page, per_page
- Schedule: scheduled_at debe ser ISO 8601 en UTC o con timezone
- Thread: minimo 2 tweets, maximo 25
- DELETE /api/tweet/{tweet_id} borra el tweet de Twitter Y marca como "deleted" en historial
