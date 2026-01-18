# Secrets (out of git)

This repo does not commit Kubernetes Secret manifests. Secrets are sourced
via ExternalSecrets by default, with a manual `kubectl create secret` fallback.

## ExternalSecrets (default)

The overlays include `ExternalSecret` resources that expect a
`ClusterSecretStore` named `bajaga-secrets` (change if needed).

Expected keys in your external store:

- Postgres:
  - `eloquia/<env>/postgres/password`
  - `eloquia/<env>/postgres/postgres-password`
- Backend:
  - `eloquia/<env>/backend/database_url`
  - `eloquia/<env>/backend/auth_cookie_secret`

Where `<env>` is `home`, `staging`, or `production`.

## Manual fallback

If you prefer to create secrets by hand, keep the names and keys below.

### 1) Postgres password secret

Name: `postgres-auth`

Keys:
- `password`: app password for the `eloquia` user
- `postgres-password`: optional superuser password (unused by app, but kept for parity)

Example (replace values):

```bash
kubectl -n <namespace> create secret generic postgres-auth \
  --from-literal=password='REPLACE_ME' \
  --from-literal=postgres-password='REPLACE_ME'
```

### 2) Backend secret

Name: `backend-secrets`

Keys:
- `DATABASE_URL`
- `AUTH_COOKIE_SECRET`

Example (replace values):

```bash
kubectl -n <namespace> create secret generic backend-secrets \
  --from-literal=DATABASE_URL='postgres://eloquia:REPLACE_ME@postgres:5432/eloquia?sslmode=disable&search_path=public' \
  --from-literal=AUTH_COOKIE_SECRET='REPLACE_ME'
```

Notes:
- `AUTH_COOKIE_SECRET` should be >= 32 bytes.
- If you use SOPS instead, keep the key names identical.
