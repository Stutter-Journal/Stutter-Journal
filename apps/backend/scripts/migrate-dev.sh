#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MIGRATION_DIR="${BLUEPRINT_DB_MIGRATION_DIR:-file://ent/migrate/migrations}"
SCHEMA="${BLUEPRINT_DB_SCHEMA:-public}"
PORT="${BLUEPRINT_DB_PORT:-5432}"
DSN="${BLUEPRINT_DATABASE_URL:-${DATABASE_URL:-}}"

if [[ -z "$DSN" ]]; then
  : "${BLUEPRINT_DB_USERNAME:?Missing BLUEPRINT_DB_USERNAME}"
  : "${BLUEPRINT_DB_PASSWORD:?Missing BLUEPRINT_DB_PASSWORD}"
  : "${BLUEPRINT_DB_HOST:?Missing BLUEPRINT_DB_HOST}"
  : "${BLUEPRINT_DB_DATABASE:?Missing BLUEPRINT_DB_DATABASE}"

  DSN="postgres://${BLUEPRINT_DB_USERNAME}:${BLUEPRINT_DB_PASSWORD}@${BLUEPRINT_DB_HOST}:${PORT}/${BLUEPRINT_DB_DATABASE}?sslmode=disable&search_path=${SCHEMA}"
fi

if command -v atlas >/dev/null 2>&1; then
  ATLAS_CMD="atlas"
elif command -v go >/dev/null 2>&1; then
  ATLAS_CMD="go run ariga.io/atlas/cmd/atlas@v0.32.1"
else
  echo "atlas binary not found and Go unavailable; install atlas to run migrations." >&2
  exit 1
fi

echo "Applying migrations with ${ATLAS_CMD} (dir: ${MIGRATION_DIR})"
eval "${ATLAS_CMD} migrate apply --dir \"${MIGRATION_DIR}\" --url \"${DSN}\""
