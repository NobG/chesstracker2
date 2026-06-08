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
- `scripts/smoke-test-hostnet.sh` prueft den laufenden Hostnet-Compose-Betrieb.
- Hostnet-Smoke-Test prueft `/today`, `/week`, `/month`, `/categories`.
- Oeffentliche URL `https://chesstracker2.litux.de/today` wird optional geprueft, wenn `PUBLIC_BASE_URL` gesetzt ist.
- README dokumentiert Ausfuehrung.

## Technische Hinweise

`smoke-test.sh` erwartet eine laufende Anwendung. `smoke-test-docker.sh` startet die Standard-Docker-Compose-Container lokal und prueft die App. `smoke-test-hostnet.sh` erwartet einen laufenden Hostnet-Compose-Stack und prueft Status sowie Kernrouten.

## Tests

Smoke-Test kann ausgefuehrt werden, sobald Maven und PostgreSQL verfuegbar sind. Docker-Smoke-Test kann ausgefuehrt werden, sobald Docker Compose verfuegbar ist. Der Ubuntu-Zielserver wurde nicht durch Codex validiert; der laufende Hostnet-Betrieb wurde vom Nutzer manuell gemeldet.

## Status

umgesetzt
