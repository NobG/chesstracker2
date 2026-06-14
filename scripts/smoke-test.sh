#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="${TMP_DIR}/chesstracker2-smoke-body-$$.html"
STATUS_FILE="${TMP_DIR}/chesstracker2-smoke-status-$$.txt"
COOKIE_FILE="${TMP_DIR}/chesstracker2-smoke-cookie-$$.txt"

cleanup() {
  rm -f "$BODY_FILE" "$STATUS_FILE" "$COOKIE_FILE"
}
trap cleanup EXIT

require_auth_env() {
  if [[ -z "${CHESSTRACKER2_AUTH_USER:-}" || -z "${CHESSTRACKER2_AUTH_PASSWORD:-}" ]]; then
    echo "CHESSTRACKER2_AUTH_USER und CHESSTRACKER2_AUTH_PASSWORD muessen fuer den Smoke-Test gesetzt sein" >&2
    exit 1
  fi
}

login() {
  local csrf
  local status

  status="$(curl -sS -c "$COOKIE_FILE" --max-time 30 -o "$BODY_FILE" -w '%{http_code}' "$BASE_URL/login")"
  if [[ "$status" != "200" ]]; then
    echo "Login-Seite nicht erreichbar: HTTP $status" >&2
    exit 1
  fi
  csrf="$(sed -n 's/.*name="_csrf"[^>]*value="\([^"]*\)".*/\1/p' "$BODY_FILE" | head -n 1)"
  if [[ -z "$csrf" ]]; then
    echo "CSRF-Token auf Login-Seite nicht gefunden" >&2
    exit 1
  fi

  status="$(
    curl -sS -L -b "$COOKIE_FILE" -c "$COOKIE_FILE" --max-time 30 \
      -o "$BODY_FILE" -w '%{http_code}' \
      --data-urlencode "username=${CHESSTRACKER2_AUTH_USER}" \
      --data-urlencode "password=${CHESSTRACKER2_AUTH_PASSWORD}" \
      --data-urlencode "_csrf=${csrf}" \
      "$BASE_URL/login"
  )"
  if [[ "$status" != "200" ]] || ! grep -Eq "Copy-Block|Aimchess Training|chesstracker2" "$BODY_FILE"; then
    echo "Login fehlgeschlagen oder App-Startseite nicht erreicht" >&2
    exit 1
  fi
}

fetch_page() {
  local path="$1"
  local status

  status="$(curl -sS -L -b "$COOKIE_FILE" -c "$COOKIE_FILE" --max-time 30 --retry 3 --retry-delay 2 -o "$BODY_FILE" -w '%{http_code}' "$BASE_URL$path")" || return 1
  printf '%s' "$status" > "$STATUS_FILE"
  printf '%s' "$status"
}

check_page() {
  local path="$1"
  local expected_status="${2:-200}"
  local expected_text="$3"
  local status

  status="$(fetch_page "$path")"
  if [[ "$status" != "$expected_status" ]]; then
    echo "HTTP-Status fehlerhaft fuer $path: erwartet $expected_status, erhalten $status" >&2
    return 1
  fi

  if ! grep -Eq "$expected_text" "$BODY_FILE"; then
    echo "Text '$expected_text' nicht auf $path gefunden" >&2
    return 1
  fi
}

require_auth_env
login

check_page "/today" "200" "Copy-Block"
check_page "/week" "200" "Wochenstatistik"
check_page "/month" "200" "Monatsstatistik"
check_page "/categories" "200" "Tactics"
check_page "/today" "200" "Aimchess Training"

echo "Smoke-Test erfolgreich: $BASE_URL"
