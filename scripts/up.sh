#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT/infra/ansible"
exec ansible-playbook -i localhost, -c local playbooks/deploy.yml "$@"
