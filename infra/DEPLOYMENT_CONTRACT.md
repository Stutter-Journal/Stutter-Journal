# Eloquia MVP Deployment Contract (Discovery)

This document is generated from codebase discovery.

Scope of discovery: `apps/backend/**` and `apps/desktop/**` only.

## Backend (Go) — `apps/backend`

### Runtime
- **Protocol**: HTTP
- **Default listen port**: `8080`
- **Port env var**: `PORT` (string/int)

### Health / readiness
- `GET /health` → `200 {"status":"ok"}` (liveness)
- `GET /ready` → `200 {"status":"ready"}` when DB responds; `503` otherwise (readiness)

### Database configuration
The backend connects to Postgres via Ent + pgx.

Supported env var patterns:
- Preferred single var:
  - `DATABASE_URL` (also accepts `BLUEPRINT_DATABASE_URL`)
    - Example DSN format used by the code: `postgres://user:pass@host:port/db?sslmode=disable&search_path=public`
- Or split vars (Blueprint-style):
  - `BLUEPRINT_DB_HOST`
  - `BLUEPRINT_DB_PORT` (defaults to `5432`)
  - `BLUEPRINT_DB_USERNAME` (also accepts `BLUEPRINT_DB_USER`)
  - `BLUEPRINT_DB_PASSWORD`
  - `BLUEPRINT_DB_DATABASE`
  - `BLUEPRINT_DB_SCHEMA` (defaults to `public`)

Migration behavior:
- `BLUEPRINT_DB_APPLY_MIGRATIONS=true|false` (when true, runs migrations automatically **only in non-production**)
- `BLUEPRINT_DB_MIGRATION_DIR` (defaults to `file://ent/migrate/migrations`)

Environment selection:
- DB/migration “environment” is derived from first non-empty of:
  - `BLUEPRINT_ENV`, `ENVIRONMENT`, `APP_ENV`
  - Default: `development`

### Auth/session cookies
The backend uses signed session cookies (HMAC) via `securecookie`.

Env vars:
- `AUTH_COOKIE_SECRET`
  - If unset, the backend will generate an **ephemeral** secret and warn.
  - For stable logins across restarts, set this explicitly (>= 32 bytes, base64 or plain).
- Optional knobs:
  - `AUTH_COOKIE_NAME` (default `eloquia_session`)
  - `AUTH_COOKIE_DOMAIN` (default empty)
  - `AUTH_COOKIE_PATH` (default `/`)
  - `AUTH_COOKIE_SECURE` (default inferred; true in prod-like env)
  - `AUTH_COOKIE_SAMESITE` (`Lax|Strict|None`, default `Lax`)
  - `AUTH_SESSION_TTL` (Go duration, default `24h`)
  - `AUTH_BCRYPT_COST` (default `12`)

### Notes / hints
- Swagger metadata is currently set to `localhost:8080` in the binary (used for docs UI), but the server itself listens on `PORT`.

### docker-compose hint (existing)
There is an existing `apps/backend/docker-compose.yml` provisioning Postgres with:
- `POSTGRES_DB=${BLUEPRINT_DB_DATABASE}`
- `POSTGRES_USER=${BLUEPRINT_DB_USERNAME}`
- `POSTGRES_PASSWORD=${BLUEPRINT_DB_PASSWORD}`
- Port mapping `${BLUEPRINT_DB_PORT}:5432`

## Desktop (Nx workspace) — `apps/desktop`

This workspace contains at least two relevant deployable apps:

### 1) `portal` (Angular SSR app)
- Build target: `pnpm nx build portal` (default config is `production`)
- Output path: `dist/apps/portal`
- Output mode: **server** (`outputMode: "server"`), i.e. Node/Express server + browser assets
- Runtime:
  - Node/Express server (Angular SSR)
  - Port env var: `PORT`
  - Default port: `4000`
- Static assets served from: `dist/apps/portal/browser`

### 2) `bff` (NestJS “backend-for-frontend”)
- Build target: `pnpm nx build bff`
- Runtime:
  - NestJS server
  - Global prefix: `/api`
  - Port env var: `PORT`
  - Default port: `3000`
- Upstream API base URL:
  - `ELOQUIA_API_BASE_URL` (default `http://localhost:8080`)

Build output note:
- The `webpack.config.js` currently outputs to `dist/apps/api` (and a built artifact exists at `dist/apps/api/main.js`). Some Nx config still references `dist/apps/bff`, so treat `dist/apps/api` as the source of truth for the runnable bundle.

### How portal talks to upstream
In dev, `portal` is configured with a proxy:
- `apps/portal/proxy.conf.json` forwards `/api` to `http://localhost:3000` (the `bff` app)

In production Kubernetes, the simplest compatible approach is:
- Keep browser requests relative to `/api` on `app.<host>`
- Route `/api` to the `bff`
- Route `/` to the `portal`

This can be implemented either via:
- A single Kubernetes `Service` with two ports + one `Ingress` with two paths, or
- Separate Services (not preferred for MVP given the “host app.<host> → desktop service” requirement).

## Minimal environment variables (MVP)

These are the smallest set that cover “stable URLs, DB wiring, and auth”.

### Backend
- `PORT` (optional; defaults to `8080`)
- `DATABASE_URL` (recommended; otherwise use `BLUEPRINT_DB_*` split vars)
- `AUTH_COOKIE_SECRET` (recommended for stable auth)
- `ENVIRONMENT` (recommended; affects cookie secure default and migration gating)
- `BLUEPRINT_DB_APPLY_MIGRATIONS` (optional; typically `true` in dev)

### Desktop
- `PORT` (for each container: portal default `4000`, bff default `3000`)
- `ELOQUIA_API_BASE_URL` (set to in-cluster backend URL, e.g. `http://backend:8080`)

### Cross-cutting (optional)
- `LOG_LEVEL` (not currently enforced by code in discovery, but commonly used)
