#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="${PROJECT_DIR:-/opt/chesstracker2}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.hostnet.yml}"
IMAGE_NAME="${IMAGE_NAME:-chesstracker2-chesstracker2-app}"
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
PUBLIC_BASE_URL="${PUBLIC_BASE_URL:-https://chesstracker2.litux.de}"
SKIP_BACKUP="${SKIP_BACKUP:-false}"
SKIP_GIT_PULL="${SKIP_GIT_PULL:-false}"
LOCK_FILE="${LOCK_FILE:-/tmp/chesstracker2-deploy.lock}"
DIAGNOSTICS_PRINTED=false

log() {
  echo "[$(date '+%F %T')] $*"
}

fail() {
  echo "[$(date '+%F %T')] ERROR: $*" >&2
  print_diagnostics
  exit 1
}

compose() {
  docker compose -f "$COMPOSE_FILE" "$@"
}

print_diagnostics() {
  if [[ "$DIAGNOSTICS_PRINTED" == "true" ]]; then
    return
  fi
  DIAGNOSTICS_PRINTED=true

  if [[ -f "$COMPOSE_FILE" ]] && command -v docker >/dev/null 2>&1; then
    log "Compose status:"
    compose ps >&2 || true
    log "App logs:"
    compose logs --tail=120 chesstracker2-app >&2 || true
    log "DB logs:"
    compose logs --tail=80 chesstracker2-db >&2 || true
  fi
}

on_error() {
  local line="$1"
  log "Deploy failed at line $line"
  print_diagnostics
}

trap 'on_error $LINENO' ERR

acquire_lock() {
  exec 9>"$LOCK_FILE"
  if ! flock -n 9; then
    fail "Another chesstracker2 deploy is already running."
  fi
}

require_command() {
  local command_name="$1"
  command -v "$command_name" >/dev/null 2>&1 || fail "Required command not found: $command_name"
}

preflight() {
  log "Running preflight checks ..."
  [[ -d "$PROJECT_DIR" ]] || fail "Project directory does not exist: $PROJECT_DIR"
  cd "$PROJECT_DIR"

  [[ -d .git ]] || fail ".git directory missing in $PROJECT_DIR"
  [[ -f .env ]] || fail ".env missing in $PROJECT_DIR"
  [[ -f "$COMPOSE_FILE" ]] || fail "Compose file missing: $COMPOSE_FILE"

  require_command git
  require_command docker
  require_command curl

  docker compose version >/dev/null || fail "docker compose version failed"

  if command -v nginx >/dev/null 2>&1; then
    log "Checking nginx configuration ..."
    nginx -t
  else
    log "nginx not installed or not in PATH, skipping nginx -t"
  fi
}

update_git() {
  if [[ "$SKIP_GIT_PULL" == "true" ]]; then
    log "Skipping git pull because SKIP_GIT_PULL=true"
    git log --oneline -1
    return
  fi

  log "Fetching origin/main ..."
  git fetch origin

  local status
  status="$(git status --short)"
  if [[ -n "$status" ]]; then
    echo "$status" >&2
    fail "Working tree is not clean. Commit/stash local changes before deploy."
  fi

  log "Updating main with fast-forward only ..."
  git checkout main
  git pull --ff-only origin main
  git log --oneline -1
}

db_container_running() {
  local running
  running="$(docker inspect --format '{{.State.Running}}' chesstracker2-db 2>/dev/null || true)"
  [[ "$running" == "true" ]]
}

backup_database() {
  if [[ "$SKIP_BACKUP" == "true" ]]; then
    log "Skipping database backup because SKIP_BACKUP=true"
    return
  fi

  if ! db_container_running; then
    log "DB container not running yet, skipping backup."
    return
  fi

  local backup_file
  backup_file="backups/chesstracker2_$(date +%F_%H-%M-%S)_predeploy.sql"
  mkdir -p backups
  log "Creating predeploy database backup: $backup_file"
  compose exec -T chesstracker2-db \
    pg_dump -h 127.0.0.1 -p 15432 -U chesstracker2 -d chesstracker2 \
    > "$backup_file"
}

build_image() {
  local git_sha
  git_sha="$(git rev-parse --short HEAD)"
  log "Building Docker image with host network: $IMAGE_NAME:latest and $IMAGE_NAME:$git_sha"
  docker build --network=host -t "$IMAGE_NAME:latest" -t "$IMAGE_NAME:$git_sha" .
}

start_stack() {
  log "Validating Compose configuration ..."
  compose config >/dev/null

  log "Starting Hostnet Compose stack ..."
  compose up -d
  compose ps
}

container_running() {
  local container="$1"
  docker inspect --format '{{.State.Running}}' "$container" 2>/dev/null || true
}

container_health() {
  local container="$1"
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$container" 2>/dev/null || true
}

wait_for_container_health() {
  local container="$1"
  local timeout_seconds="$2"
  local elapsed=0
  local interval=3
  local health
  local running

  log "Waiting for $container to become healthy or running ..."
  while (( elapsed <= timeout_seconds )); do
    health="$(container_health "$container")"
    running="$(container_running "$container")"

    if [[ "$health" == "healthy" ]]; then
      log "$container is healthy"
      return
    fi

    if [[ "$health" == "none" && "$running" == "true" ]]; then
      log "$container has no healthcheck but is running"
      return
    fi

    if [[ "$health" == "unhealthy" ]]; then
      fail "$container is unhealthy"
    fi

    log "$container status: running=${running:-unknown}, health=${health:-unknown}"
    sleep "$interval"
    elapsed=$((elapsed + interval))
  done

  fail "Timed out waiting for $container after ${timeout_seconds}s"
}

run_smoke_test() {
  log "Running Hostnet smoke test ..."
  BASE_URL="$BASE_URL" PUBLIC_BASE_URL="$PUBLIC_BASE_URL" COMPOSE_FILE="$COMPOSE_FILE" ./scripts/smoke-test-hostnet.sh
}

main() {
  acquire_lock
  preflight
  update_git
  backup_database
  build_image
  start_stack
  wait_for_container_health chesstracker2-db 90
  wait_for_container_health chesstracker2-app 90
  run_smoke_test
  log "Deploy completed successfully."
}

main "$@"
