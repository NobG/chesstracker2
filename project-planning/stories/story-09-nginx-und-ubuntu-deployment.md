# Story 09: nginx und Ubuntu Deployment

## Ziel

Deployment-Beispiele fuer Ubuntu, Docker Compose, systemd und nginx bereitstellen.

## Ausgangslage

Die Anwendung soll privat auf einem Ubuntu Server laufen. Standard-Zielbetrieb ist Docker Compose: App und PostgreSQL laufen in getrennten Containern, nginx bleibt Host-Reverse-Proxy. Auf dem konkreten Zielserver ist laut Nutzer-Validierung das Docker-Bridge-Netz defekt oder blockiert; dort ist aktuell Hostnet-Compose die validierte Betriebsart.

## Akzeptanzkriterien

- Dockerfile existiert.
- `docker-compose.yml` definiert `chesstracker2-app` und `chesstracker2-db`.
- PostgreSQL nutzt ein persistentes Docker Volume.
- Docker Volume heisst `chesstracker2_pgdata`.
- Datenbank und Datenbanknutzer heissen `chesstracker2`.
- Zielpfad ist `/opt/chesstracker2`.
- systemd-Service heisst `chesstracker2`.
- App wird nur lokal auf dem Host veroeffentlicht.
- systemd-Service fuer Docker Compose existiert.
- systemd-Service fuer Hostnet-Compose existiert.
- nginx Reverse-Proxy-Beispiel existiert.
- `.env.example` existiert.
- README beschreibt Docker Compose als empfohlene Betriebsart.
- `docs/INSTALL_UBUNTU_DOCKER.md` beschreibt die Serverinstallation.
- Zielserverbetrieb fuer `chesstracker2.litux.de` ist dokumentiert.
- `docker-compose.hostnet.yml` dokumentiert den funktionierenden Hostnet-Workaround.

## Technische Hinweise

Spring Boot lauscht im Container auf `8080`. Standard-Compose veroeffentlicht den Port auf `127.0.0.1:${APP_PORT}`. nginx leitet auf diesen lokalen Host-Port weiter.

Im Hostnet-Betrieb lauscht die App auf `127.0.0.1:8080`, PostgreSQL auf `127.0.0.1:15432`. Die App verbindet sich mit `jdbc:postgresql://127.0.0.1:15432/chesstracker2`.

## Tests

Codex hat keinen Zugriff auf den Zielserver und hat dort keine Befehle ausgefuehrt. Laut Nutzer-Validierung laeuft der Zielserver unter `https://chesstracker2.litux.de/today`, App und DB sind healthy, HTTPS funktioniert und Flyway hat `V001` sowie `V002` erfolgreich angewendet.

## Status

umgesetzt fuer Docker-Compose-MVP; Hostnet-Zielserverbetrieb dokumentiert
