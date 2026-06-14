#!/usr/bin/env bash
set -euo pipefail

COMPOSE="${COMPOSE:-docker compose}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="${TMP_DIR}/chesstracker2-docker-smoke-body-$$.html"
COOKIE_FILE="${TMP_DIR}/chesstracker2-docker-smoke-cookie-$$.txt"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

BASE_URL="${BASE_URL:-http://127.0.0.1:${APP_PORT:-8080}}"

cleanup() {
  rm -f "$BODY_FILE" "$COOKIE_FILE"
}
trap cleanup EXIT

fail() {
  echo "Docker-Smoke-Test fehlgeschlagen: $*" >&2
  exit 1
}

require_auth_env() {
  if [[ -z "${CHESSTRACKER2_AUTH_USER:-}" || -z "${CHESSTRACKER2_AUTH_PASSWORD:-}" ]]; then
    fail "CHESSTRACKER2_AUTH_USER und CHESSTRACKER2_AUTH_PASSWORD muessen gesetzt sein"
  fi
}

login() {
  local csrf
  local status

  status="$(curl -sS -c "$COOKIE_FILE" --max-time 30 -o "$BODY_FILE" -w '%{http_code}' "$BASE_URL/login")" || fail "Login-Seite nicht erreichbar"
  if [[ "$status" != "200" ]]; then
    fail "Login-Seite liefert HTTP $status"
  fi
  csrf="$(sed -n 's/.*name="_csrf"[^>]*value="\([^"]*\)".*/\1/p' "$BODY_FILE" | head -n 1)"
  if [[ -z "$csrf" ]]; then
    fail "CSRF-Token auf Login-Seite nicht gefunden"
  fi

  status="$(
    curl -sS -L -b "$COOKIE_FILE" -c "$COOKIE_FILE" --max-time 30 \
      -o "$BODY_FILE" -w '%{http_code}' \
      --data-urlencode "username=${CHESSTRACKER2_AUTH_USER}" \
      --data-urlencode "password=${CHESSTRACKER2_AUTH_PASSWORD}" \
      --data-urlencode "_csrf=${csrf}" \
      "$BASE_URL/login"
  )" || fail "Login-POST fehlgeschlagen"
  if [[ "$status" != "200" ]] || ! grep -Eq "Copy-Block|Aimchess Training|chesstracker2" "$BODY_FILE"; then
    fail "Login fehlgeschlagen oder App-Startseite nicht erreicht"
  fi
}

echo "Pruefe Docker Compose Konfiguration ..."
$COMPOSE config >/dev/null || fail "docker compose config ist ungueltig"
require_auth_env

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
  curl -sS -L -b "$COOKIE_FILE" -c "$COOKIE_FILE" --max-time 30 --retry 5 --retry-delay 2 -o "$BODY_FILE" -w '%{http_code}' "$BASE_URL$path"
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
login
check_page "/today" "Copy-Block"
check_page "/today" "Tactics"
check_page "/week" "Wochenstatistik"
check_page "/month" "Monatsstatistik"
check_page "/categories" "Tactics"

echo "Pruefe Containerstatus ..."
$COMPOSE ps

echo "Docker-Smoke-Test erfolgreich: $BASE_URL"
