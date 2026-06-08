# Story 02: Datenmodell und Migrationen

## Ziel

PostgreSQL-Schema fuer Kategorien, Trainingseintraege und Tagesnotizen bereitstellen.

## Ausgangslage

Es gibt noch keine Datenbankstruktur.

## Akzeptanzkriterien

- Flyway-Migration fuer initiales Schema existiert.
- Seed-Migration fuer Aimchess-Kategorien existiert.
- Unique Constraint pro Datum und Kategorie ist definiert.
- Werteconstraints fuer Ergebnis und Dauer sind definiert.

## Technische Hinweise

`category_key` wird statt `key` als Spaltenname verwendet.

## Tests

Repository-Test prueft Unique Constraint.

## Status

umgesetzt
