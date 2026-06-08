# Current Focus

## Stand

Der chesstracker2-MVP ist als neues Spring-Boot-Projekt aufgebaut. Der Zielbetrieb wird auf Docker Compose fuer Ubuntu umgestellt.

## Aktueller Fokus

1. Docker-Compose-Zielbetrieb fuer Ubuntu
2. App-Container `chesstracker2-app`
3. PostgreSQL-Container `chesstracker2-db` mit persistentem Volume
4. nginx als Host-Reverse-Proxy auf `127.0.0.1:8080`
5. Installationsanleitung fuer manuelle Serverausfuehrung

## Technische Zielnamen

- Projektname: `chesstracker2`
- Zielpfad: `/opt/chesstracker2`
- systemd-Service: `chesstracker2`
- Docker App-Service: `chesstracker2-app`
- Docker DB-Service: `chesstracker2-db`
- Docker Volume: `chesstracker2_pgdata`
- Datenbank: `chesstracker2`

## Status

- Story 01: umgesetzt
- Story 02: umgesetzt
- Story 03: umgesetzt
- Story 04: umgesetzt
- Story 05: umgesetzt
- Story 06-08: umgesetzt als MVP-Grundlage
- Story 09: Docker-Compose-Deployment vorbereitet
- Story 10: Smoke-Tests fuer App und Docker vorbereitet

## Hinweis

Der Ubuntu-Zielserver wurde nicht durch Codex validiert, weil kein Serverzugriff besteht. Die Installation muss anhand `docs/INSTALL_UBUNTU_DOCKER.md` auf dem Zielserver ausgefuehrt und geprueft werden.
