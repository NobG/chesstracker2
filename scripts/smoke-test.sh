#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="${TMP_DIR}/chesstracker2-smoke-body-$$.html"
STATUS_FILE="${TMP_DIR}/chesstracker2-smoke-status-$$.txt"

cleanup() {
  rm -f "$BODY_FILE" "$STATUS_FILE"
}
trap cleanup EXIT

fetch_page() {
  local path="$1"
  local status

  status="$(curl -sS -L --max-time 30 --retry 3 --retry-delay 2 -o "$BODY_FILE" -w '%{http_code}' "$BASE_URL$path")" || return 1
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

check_page "/today" "200" "Copy-Block"
check_page "/week" "200" "Wochenstatistik"
check_page "/month" "200" "Monatsstatistik"
check_page "/categories" "200" "Tactics"
check_page "/today" "200" "Aimchess Training"

echo "Smoke-Test erfolgreich: $BASE_URL"
