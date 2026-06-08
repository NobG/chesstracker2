# Current Focus

## Stand

Der chesstracker2-MVP ist als neues Spring-Boot-Projekt aufgebaut. Die Anwendung wurde laut Nutzer-Validierung auf dem Zielserver installiert und laeuft unter `https://chesstracker2.litux.de/today`.

## Aktueller Fokus

1. Dokumentierter Docker-Compose-Zielbetrieb fuer Ubuntu
2. App-Container `chesstracker2-app`
3. PostgreSQL-Container `chesstracker2-db` mit persistentem Volume
4. nginx als Host-Reverse-Proxy auf `127.0.0.1:8080`
5. Hostnet-Compose als aktuell validierte Zielserver-Betriebsart
6. Docker-Bridge-Reparatur als technische Folgeaufgabe

## Technische Zielnamen

- Projektname: `chesstracker2`
- Zielpfad: `/opt/chesstracker2`
- systemd-Service: `chesstracker2`
- Docker App-Service: `chesstracker2-app`
- Docker DB-Service: `chesstracker2-db`
- Docker Volume: `chesstracker2_pgdata`
- Datenbank: `chesstracker2`
- Domain: `chesstracker2.litux.de`
- Zielserver-IP: `82.165.0.60`
- Hostnet DB-Port: `15432`
- Lokaler App-Port: `8080`

## Status

- Story 01: umgesetzt
- Story 02: umgesetzt
- Story 03: umgesetzt
- Story 04: umgesetzt
- Story 05: umgesetzt
- Story 06-08: umgesetzt als MVP-Grundlage
- Story 09: Docker-Compose-Deployment vorbereitet und Zielserver-Hostnet-Betrieb dokumentiert
- Story 10: Smoke-Tests fuer App, Docker und Hostnet-Betrieb vorbereitet

## Hinweis

Der Ubuntu-Zielserver wurde nicht durch Codex validiert, weil kein Serverzugriff besteht. Der dokumentierte Zielserverstatus stammt aus der manuellen Installation und Validierung des Nutzers.

Gemeldeter Zielserverstand am 2026-06-08:

- Subdomain `chesstracker2.litux.de` eingerichtet
- HTTPS funktioniert
- `chesstracker2-app` und `chesstracker2-db` laufen healthy
- Flyway `V001` und `V002` erfolgreich angewendet
- Bridge-Compose scheiterte wegen Docker-Netzwerkproblem
- Hostnet-Compose ist aktuell die validierte Betriebsart
- systemd muss auf dem Zielserver `docker-compose.hostnet.yml` verwenden
