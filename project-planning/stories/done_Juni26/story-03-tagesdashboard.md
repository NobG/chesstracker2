# Story 03: Tagesdashboard

## Ziel

Startseite fuer den aktuellen Trainingstag bereitstellen.

## Ausgangslage

Kategorien sind in der Datenbank gepflegt und aktivierbar.

## Akzeptanzkriterien

- `GET /` leitet auf `/today` weiter.
- `/today` zeigt aktive Kategorien.
- Pro Kategorie sind Ergebnis, Score, Zeit und Notiz erfassbar.
- UI ist dunkel, ruhig und responsive.

## Technische Hinweise

Thymeleaf rendert serverseitig. JavaScript wird nur fuer Kopieren verwendet.

## Tests

Smoke-Test prueft `/today`.

## Status

umgesetzt
