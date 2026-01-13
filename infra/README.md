# Eloquia infra (Docker Desktop Kubernetes MVP)

Goal: deploy `apps/backend` (Go) + `apps/desktop` (Nx: portal + bff) onto Docker Desktop Kubernetes, with in-cluster Postgres and stable dev URLs.

## One-command UX

- `./scripts/up.sh` → build images locally + terraform apply + wait
- `./scripts/down.sh` → terraform destroy + cleanup
- `./scripts/status.sh` → show current URLs + k8s resources

## DNS / host strategy

### Mode A (default): sslip.io (zero hassle)

- The Ansible role `dns_mode` determines an ingress endpoint IP (or falls back to node IP).
- It generates `.env.runtime` at repo root:
  - `http://api.<ip>.sslip.io`
  - `http://app.<ip>.sslip.io`

### Mode B (optional): dnsmasq + eloquia.test

Not implemented for MVP (documented only).

If you want pretty names:
- Configure a local DNS resolver (e.g. `dnsmasq`) so:
  - `api.eloquia.test` → ingress IP
  - `app.eloquia.test` → ingress IP
- Then run terraform with `base_host=eloquia.test`.

## Kubernetes layout

- Namespace: `eloquia-dev`
- Services:
  - `postgres` (via Bitnami chart, ClusterIP)
  - `backend` (ClusterIP)
  - `desktop` (ClusterIP, two ports: portal + bff)
- Ingress:
  - `api.<base_host>` → backend
  - `app.<base_host>` → desktop (with `/api` path routed to bff)

## Acceptance checklist

- `./scripts/up.sh`
- `source ./.env.runtime`
- `curl "$ELOQUIA_API_URL/health"`
- Open `$ELOQUIA_APP_URL` in browser
- `./scripts/down.sh`

See also: `infra/DEPLOYMENT_CONTRACT.md` and `infra/ENV_WIRING.md`.
