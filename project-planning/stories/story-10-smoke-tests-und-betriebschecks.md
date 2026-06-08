# Story 10: Smoke-Tests und Betriebschecks

## Ziel

Schnelle Betriebspruefung fuer lokale und produktionsnahe Umgebungen.

## Ausgangslage

Anwendung braucht pruefbare Kernrouten.

## Akzeptanzkriterien

- `scripts/smoke-test.sh` existiert.
- `/today`, `/week`, `/month` werden geprueft.
- Copy-Block wird geprueft.
- README dokumentiert Ausfuehrung.

## Technische Hinweise

Das Skript erwartet eine laufende Anwendung.

## Tests

Smoke-Test kann ausgefuehrt werden, sobald Maven und PostgreSQL verfuegbar sind.

## Status

umgesetzt
