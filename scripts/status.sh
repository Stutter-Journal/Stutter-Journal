#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NAMESPACE="eloquia-dev"
INGRESS_NS="ingress-nginx"

BASE_HOST_FILE="$ROOT/infra/.state/base_host"

if [[ -f "$BASE_HOST_FILE" ]]; then
  BASE_HOST="$(cat "$BASE_HOST_FILE" | tr -d '\n')"
else
  # Best-effort fallback (Mode A)
  ip="$(kubectl get svc -n "$INGRESS_NS" ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)"
  if [[ -z "$ip" ]]; then
    ctx="$(kubectl config current-context 2>/dev/null || true)"
    if echo "$ctx" | grep -qi 'docker'; then
      ip="127.0.0.1"
    else
      ip="$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}' 2>/dev/null || true)"
    fi
  fi
  ip="${ip:-127.0.0.1}"
  BASE_HOST="$ip.sslip.io"
fi

echo "Ingress base host: $BASE_HOST"
echo "API URL: http://api.$BASE_HOST"
echo "APP URL: http://app.$BASE_HOST"
echo

echo "== Namespace resources ($NAMESPACE) =="
kubectl get pods,svc,ingress -n "$NAMESPACE" || true

echo

echo "== Ingress controller ($INGRESS_NS) =="
kubectl get svc,deploy -n "$INGRESS_NS" || true
