# Current Focus

## Stand

Der chesstracker2-MVP ist abgeschlossen und als Version v1.0.0 markiert. Die Anwendung wurde laut Nutzer-Validierung auf dem Zielserver installiert und laeuft unter `https://chesstracker2.litux.de/today`.

## Aktueller Fokus

1. MVP stabil betreiben
2. Zielserver-Deployment weiter validieren
3. Version-2-Erweiterungen planen
4. Backup-/Restore-Prozess regelmaessig wiederholen/automatisieren

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
- Story 06: umgesetzt
- Story 07: umgesetzt
- Story 08: umgesetzt
- Story 09: umgesetzt
- Story 10: umgesetzt
- Story 11: Rating-Snapshots umgesetzt
- MVP: abgeschlossen
- Version: `v1.0.0`
- Abschlussdatum: 2026-06-08

## MVP-Abschluss v1.0.0

Der MVP ist fachlich abgeschlossen. Weitere Ideen wie automatische Rating-Quellen, chess-results, Turniermodul, Partienlog, CSV-Export, Kalenderansicht und verbesserte Tagesbewertung bleiben im Backlog fuer spaetere Versionen.

## Backup-/Restore-Validierung

Am 2026-06-09 wurde der Backup-/Restore-Prozess auf dem Zielserver erfolgreich getestet.

Vorgehen:
- Backup der Produktivdatenbank per `pg_dump -Fc`
- Restore in separate Testdatenbank `chesstracker2_restore_test`
- Vergleich der wichtigsten Tabellen-Counts zwischen Produktiv- und Restore-Testdatenbank

Ergebnis:
Der Restore war erfolgreich. Die geprueften Tabellen hatten identische Datensaetze.

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
- `scripts/deploy-chesstracker2.sh` automatisiert Git-Update, Backup, Hostnet-Build, Compose-Start, Health-Wait und Smoke-Test
