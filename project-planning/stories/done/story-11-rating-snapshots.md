# Story 11: Rating-Snapshots erfassen und Verlauf anzeigen

## User Story

Als Schachspieler moechte ich regelmaessig meine Ratingstaende erfassen, damit ich langfristig sehen kann, ob mein Training mit einer Verbesserung meiner Spielstaerke zusammenhaengt.

## Ziel

chesstracker2 soll neben Aimchess-Training auch die Entwicklung der Spielstaerke sichtbar machen. Der erste MVP erfasst Rating-Snapshots manuell und zeigt einen einfachen Verlauf.

## Hintergrund

Aktuell trackt chesstracker2 Aimchess-Training pro Tag, Tagesstatus, Kategorien sowie Wochen- und Monatsstatistiken. Als naechster Ausbau soll ein manueller Rating-Verlauf entstehen. Automatische API-Anbindungen, Charts und komplexe Korrelationen bleiben spaeteren Stories vorbehalten.

## MVP-Funktionsumfang

### Rating-Snapshot erfassen

Neue Seite:

```text
/rating
```

Felder:

- Datum
- Lichess Blitz
- Lichess Rapid
- Lichess Classical
- DWZ
- FIDE Elo
- Notiz

Datum ist Pflicht. Alle Ratingfelder und die Notiz sind optional.

### Rating-Verlauf anzeigen

`/rating` zeigt eine Tabelle mit den letzten Rating-Snapshots, neueste zuerst.

Spalten:

- Datum
- Lichess Blitz
- Lichess Rapid
- Lichess Classical
- DWZ
- FIDE Elo
- Notiz

### Veraenderung anzeigen

Wenn mindestens zwei Snapshots vorhanden sind, zeigt die Seite einfache Differenzen seit dem vorherigen Snapshot. Verglichen werden nur Ratingfelder, die im aktuellen und vorherigen Snapshot gesetzt sind.

Beispiele:

- Lichess Blitz: +25
- Lichess Rapid: -10
- DWZ: +18

## Datenmodell-Vorschlag

Neue Tabelle:

```text
rating_snapshots
```

Spalten:

```sql
id bigserial primary key
snapshot_date date not null
lichess_blitz integer null
lichess_rapid integer null
lichess_classical integer null
dwz integer null
fide_elo integer null
note text null
created_at timestamptz not null default now()
updated_at timestamptz not null default now()
```

Unique Constraint:

```sql
unique(snapshot_date)
```

Fuer den MVP reicht ein Snapshot pro Tag. Wenn fuer dasselbe Datum erneut gespeichert wird, soll der vorhandene Snapshot aktualisiert werden.

Vorgesehene Migration:

```text
V005__add_rating_snapshots.sql
```

## Backend-Vorschlag

Neue Klassen:

- `RatingSnapshot`
- `RatingSnapshotRepository`
- `RatingSnapshotService`
- `RatingSnapshotForm`
- `RatingSnapshotViewModel`
- `RatingController`

Routen:

- `GET /rating`
- `POST /rating`

POST-Verhalten:

- Snapshot fuer neues Datum anlegen
- vorhandenen Snapshot fuer dasselbe Datum aktualisieren
- danach Redirect auf `/rating`

## UI-Anforderungen

- Navigation um `Rating` erweitern
- kompakter Eingabebereich oben
- Verlaufstabelle darunter
- Differenz zum letzten Snapshot als kleine Statistik-Karten
- dunkles chesstracker2-Design beibehalten
- keine ueberladene UI

## Validierung

- Datum ist Pflicht
- Ratingfelder duerfen leer sein
- gesetzte Ratingfelder muessen nicht-negativ sein
- negative Werte werden abgelehnt oder nicht gespeichert
- unrealistische Hoechstwerte muessen im MVP nicht hart blockiert werden

## Akzeptanzkriterien

- Es gibt eine neue Seite `/rating`.
- Navigation enthaelt einen Link `Rating`.
- Ein Snapshot fuer ein Datum kann gespeichert werden.
- Lichess Blitz, Rapid, Classical, DWZ und FIDE Elo koennen optional eingetragen werden.
- Ein erneutes Speichern fuer dasselbe Datum aktualisiert den vorhandenen Snapshot.
- Die Tabelle zeigt Snapshots neueste zuerst.
- Wenn mindestens zwei Snapshots vorhanden sind, werden Differenzen zum vorherigen Snapshot angezeigt.
- Leere Ratingfelder fuehren nicht zu Fehlern.
- Bestehende Aimchess-Funktionen bleiben unveraendert.
- Keine automatische API-Anbindung wird in dieser Story umgesetzt.

## Tests bei Umsetzung

- Repository-/Service-Test: Snapshot neu anlegen.
- Repository-/Service-Test: Snapshot fuer gleiches Datum aktualisieren.
- Repository-/Service-Test: keine Duplikate fuer gleiches Datum.
- Service-Test: Differenz zum vorherigen Snapshot berechnen.
- Service-Test: fehlende Werte bei Differenzberechnung ignorieren.
- MVC-Test: `GET /rating` liefert 200.
- MVC-Test: Navigation enthaelt `Rating`.
- MVC-Test: `POST /rating` speichert Snapshot.
- MVC-Test: `POST /rating` aktualisiert bestehenden Snapshot.
- Validierungstest: negatives Rating wird abgelehnt oder nicht gespeichert.
- Validierungstest: leere optionale Felder sind erlaubt.
- Regression: `/today`, `/week`, `/month`, `/categories` bleiben erreichbar.

## Nicht-Ziele

- Lichess API
- automatische Synchronisierung
- Chess.com Integration
- DWZ/FIDE Scraping
- Rating-Charts
- Turnierverwaltung
- Partienverwaltung
- Korrelationsanalyse Training zu Rating

## Status

umgesetzt
