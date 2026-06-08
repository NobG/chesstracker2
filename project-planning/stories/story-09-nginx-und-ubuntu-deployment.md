# Story 09: nginx und Ubuntu Deployment

## Ziel

Deployment-Beispiele fuer Ubuntu, Docker Compose, systemd und nginx bereitstellen.

## Ausgangslage

Die Anwendung soll privat auf einem Ubuntu Server laufen. Zielbetrieb ist Docker Compose: App und PostgreSQL laufen in getrennten Containern, nginx bleibt Host-Reverse-Proxy.

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
- nginx Reverse-Proxy-Beispiel existiert.
- `.env.example` existiert.
- README beschreibt Docker Compose als empfohlene Betriebsart.
- `docs/INSTALL_UBUNTU_DOCKER.md` beschreibt die Serverinstallation.

## Technische Hinweise

Spring Boot lauscht im Container auf `8080`. Docker Compose veroeffentlicht den Port auf `127.0.0.1:${APP_PORT}`. nginx leitet auf diesen lokalen Host-Port weiter.

## Tests

Manueller Deploy-Test auf Ubuntu steht aus. Codex hat keinen Zugriff auf den Zielserver und hat dort keine Befehle ausgefuehrt.

## Status

umgesetzt fuer Docker-Compose-MVP
