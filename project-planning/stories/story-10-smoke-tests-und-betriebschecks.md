# Story 10: Smoke-Tests und Betriebschecks

## Ziel

Schnelle Betriebspruefung fuer lokale und produktionsnahe Umgebungen.

## Ausgangslage

Anwendung braucht pruefbare Kernrouten.

## Akzeptanzkriterien

- `scripts/smoke-test.sh` existiert.
- `scripts/smoke-test-docker.sh` existiert.
- `/today`, `/week`, `/month` werden geprueft.
- Copy-Block wird geprueft.
- Docker Compose Konfiguration, Containerstart und PostgreSQL-Bereitschaft werden im Docker-Smoke-Test geprueft.
- README dokumentiert Ausfuehrung.

## Technische Hinweise

`smoke-test.sh` erwartet eine laufende Anwendung. `smoke-test-docker.sh` startet die Docker-Compose-Container lokal und prueft die App.

## Tests

Smoke-Test kann ausgefuehrt werden, sobald Maven und PostgreSQL verfuegbar sind. Docker-Smoke-Test kann ausgefuehrt werden, sobald Docker Compose verfuegbar ist. Der Ubuntu-Zielserver wurde nicht durch Codex validiert.

## Status

umgesetzt
