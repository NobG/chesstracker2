# Story 05: Copy-Block fuer ChatGPT

## Ziel

Ein formatierter Tagesblock soll direkt kopierbar sein.

## Ausgangslage

Tageswerte und Tagesnotiz liegen vor.

## Akzeptanzkriterien

- Tagesseite zeigt Copy-Block.
- Copy-Block enthaelt Datum, Kategorien, Quoten, Zeit und Tagesnotiz.
- Button kopiert den Text in die Zwischenablage.

## Technische Hinweise

Der Text wird im Service generiert, nicht im Template zusammengesetzt.

## Tests

Service-Test prueft Inhalt des Copy-Blocks. Smoke-Test sucht Copy-Block auf `/today`.

## Status

umgesetzt
