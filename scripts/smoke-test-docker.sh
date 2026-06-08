#!/usr/bin/env bash
set -euo pipefail

COMPOSE="${COMPOSE:-docker compose}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="${TMP_DIR}/chesstracker2-docker-smoke-body-$$.html"

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
  echo "Docker-Smoke-Test fehlgeschlagen: $*" >&2
  exit 1
}

echo "Pruefe Docker Compose Konfiguration ..."
$COMPOSE config >/dev/null || fail "docker compose config ist ungueltig"

echo "Starte Container ..."
$COMPOSE up -d --build || fail "Container konnten nicht gestartet werden"

echo "Warte auf PostgreSQL ..."
for attempt in $(seq 1 30); do
  if $COMPOSE exec -T chesstracker2-db pg_isready -U "${POSTGRES_USER:-chesstracker2}" -d "${POSTGRES_DB:-chesstracker2}" >/dev/null 2>&1; then
    break
  fi
  if [[ "$attempt" -eq 30 ]]; then
    $COMPOSE ps >&2 || true
    $COMPOSE logs chesstracker2-db >&2 || true
    fail "PostgreSQL ist nicht bereit"
  fi
  sleep 2
done

fetch_page() {
  local path="$1"
  curl -sS -L --max-time 30 --retry 5 --retry-delay 2 -o "$BODY_FILE" -w '%{http_code}' "$BASE_URL$path"
}

check_page() {
  local path="$1"
  local expected_text="$2"
  local status

  status="$(fetch_page "$path")" || fail "HTTP-Aufruf fuer $path fehlgeschlagen"
  if [[ "$status" != "200" ]]; then
    $COMPOSE logs chesstracker2-app >&2 || true
    fail "HTTP-Status fuer $path: erwartet 200, erhalten $status"
  fi
  if ! grep -Eq "$expected_text" "$BODY_FILE"; then
    fail "Text '$expected_text' nicht auf $path gefunden"
  fi
}

echo "Pruefe App-Endpunkte ..."
check_page "/today" "Copy-Block"
check_page "/today" "Tactics"
check_page "/week" "Wochenstatistik"
check_page "/month" "Monatsstatistik"
check_page "/categories" "Tactics"

echo "Pruefe Containerstatus ..."
$COMPOSE ps

echo "Docker-Smoke-Test erfolgreich: $BASE_URL"
