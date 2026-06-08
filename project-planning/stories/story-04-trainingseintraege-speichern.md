# Story 04: Trainingseintraege speichern

## Ziel

Tageswerte pro Kategorie speichern und erneut anzeigen.

## Ausgangslage

Das Tagesdashboard hat Eingabefelder, aber braucht Persistenz.

## Akzeptanzkriterien

- `POST /today/entries` speichert alle Kategorien des Tages.
- Ergebnisformat `7/10` wird als `success_count` und `total_count` gespeichert.
- Ungueltige Ergebnisse werden abgewiesen.
- Tagesnotiz wird separat gespeichert.

## Technische Hinweise

Die fachliche Validierung liegt im Service.

## Tests

Service-Test fuer Ergebnisformat und Tageszusammenfassung.

## Status

umgesetzt
