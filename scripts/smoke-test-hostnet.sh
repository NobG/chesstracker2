#!/usr/bin/env bash
set -euo pipefail

COMPOSE="${COMPOSE:-docker compose}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.hostnet.yml}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="${TMP_DIR}/chesstracker2-hostnet-smoke-body-$$.html"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

BASE_URL="${BASE_URL:-http://127.0.0.1:${APP_PORT:-8080}}"

cleanup() {
  rm -f "$BODY_FILE"
}
trap cleanup EXIT

fail() {
  echo "Hostnet-Smoke-Test fehlgeschlagen: $*" >&2
  exit 1
}

compose() {
  $COMPOSE -f "$COMPOSE_FILE" "$@"
}

check_service_running() {
  local service="$1"
  local state

  state="$(compose ps --status running --services | grep -x "$service" || true)"
  if [[ "$state" != "$service" ]]; then
    compose ps >&2 || true
    fail "$service laeuft nicht"
  fi
}

fetch_page() {
  local url="$1"
  curl -sS -L --max-time 30 --retry 5 --retry-delay 2 -o "$BODY_FILE" -w '%{http_code}' "$url"
}

check_page() {
  local path="$1"
  local status

  status="$(fetch_page "$BASE_URL$path")" || fail "HTTP-Aufruf fuer $path fehlgeschlagen"
  if [[ "$status" != "200" ]]; then
    compose logs --tail=200 chesstracker2-app >&2 || true
    fail "HTTP-Status fuer $path: erwartet 200, erhalten $status"
  fi
}

echo "Pruefe Hostnet-Compose-Status ..."
compose ps || fail "docker compose ps fehlgeschlagen"

echo "Pruefe laufende Services ..."
check_service_running "chesstracker2-db"
check_service_running "chesstracker2-app"

echo "Pruefe lokale HTTP-Endpunkte unter $BASE_URL ..."
check_page "/today"
check_page "/week"
check_page "/month"
check_page "/categories"

if [[ -n "${PUBLIC_BASE_URL:-}" ]]; then
  echo "Pruefe oeffentliche URL $PUBLIC_BASE_URL/today ..."
  status="$(fetch_page "$PUBLIC_BASE_URL/today")" || fail "Oeffentlicher HTTP-Aufruf fehlgeschlagen"
  if [[ "$status" != "200" ]]; then
    fail "HTTP-Status fuer $PUBLIC_BASE_URL/today: erwartet 200, erhalten $status"
  fi
fi

echo "Hostnet-Smoke-Test erfolgreich"
