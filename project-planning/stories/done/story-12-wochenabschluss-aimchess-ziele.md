# Story 12: Wochenabschluss fuer Aimchess-Ziele dokumentieren

## User Story

Als Schachspieler moechte ich pro Kalenderwoche dokumentieren, ob ich die von Aimchess vorgegebenen Wochenziele erreicht habe, damit die Wochenstatistik nicht nur Trainingseintraege, sondern auch den externen Aimchess-Wochenabschluss abbildet.

## Ziel

Die Wochenansicht soll einen einfachen Wochenabschluss enthalten. Fuer jede ISO-Kalenderwoche kann gespeichert werden, ob die Aimchess-Wochenziele erreicht wurden.

## Hintergrund

Aimchess gibt woechentliche Ziele vor, zum Beispiel:

- `Play 10 Rapid games`
- `Complete daily plan`
- `Compete in Daily Tactics Challenge`

Diese Ziele werden ausserhalb von chesstracker2 gepflegt. chesstracker2 soll im MVP nicht versuchen, einzelne Aimchess-Ziele automatisch zu synchronisieren oder detailliert nachzubauen. Wichtig ist, den Wochenabschluss manuell festhalten zu koennen.

## MVP-Funktionsumfang

### Wochenabschluss erfassen

Auf der vorhandenen Wochenansicht:

```text
/week/{year}/{week}
```

wird ein kompakter Abschnitt `Aimchess Wochenabschluss` angezeigt.

Felder:

- Status: `Nicht erfasst`, `Erreicht`, `Nicht erreicht`
- optionale Notiz

Die Notiz kann genutzt werden, um die konkreten Aimchess-Ziele oder den sichtbaren Stand aus Aimchess zu dokumentieren, zum Beispiel:

```text
Play 10 Rapid games: 10/10
Complete daily plan: 5/5
Compete in Daily Tactics Challenge: 5/5
```

### Wochenabschluss anzeigen

Die Wochenansicht zeigt den gespeicherten Status gut sichtbar neben den bestehenden Wochenkennzahlen.

Moegliche Anzeige:

- `Aimchess Wochenziel erreicht`
- `Aimchess Wochenziel nicht erreicht`
- `Aimchess Wochenabschluss noch nicht erfasst`

Wenn eine Notiz vorhanden ist, wird sie unter dem Status angezeigt.

### Wochenabschluss bearbeiten

Ein vorhandener Wochenabschluss kann fuer dieselbe ISO-Woche erneut gespeichert werden. Dabei wird der vorhandene Datensatz aktualisiert.

## Datenmodell-Vorschlag

Neue Tabelle:

```text
weekly_goal_closures
```

Spalten:

```sql
id bigserial primary key
iso_year integer not null
iso_week integer not null
status varchar(20) not null
note text null
created_at timestamptz not null default now()
updated_at timestamptz not null default now()
```

Unique Constraint:

```sql
unique(iso_year, iso_week)
```

Erlaubte Statuswerte:

- `NOT_RECORDED`
- `ACHIEVED`
- `MISSED`

## Backend-Vorschlag

Neue oder erweiterte Klassen:

- `WeeklyGoalClosure`
- `WeeklyGoalClosureRepository`
- `WeeklyGoalClosureService`
- `WeeklyGoalClosureForm`
- `WeeklyGoalClosureViewModel`

Route:

- `POST /week/{year}/{week}/goal-closure`

POST-Verhalten:

- Status und optionale Notiz speichern.
- Vorhandenen Wochenabschluss fuer dieselbe ISO-Woche aktualisieren.
- Danach Redirect auf `/week/{year}/{week}`.

## UI-Anforderungen

- Abschnitt auf der bestehenden Wochenseite, kein eigener Hauptnavigationspunkt.
- Statusauswahl als klar erkennbare Kontrolle.
- Speichern-Button im Wochenabschluss-Abschnitt.
- Bestehendes dunkles chesstracker2-Design beibehalten.
- Kein Nachbau der Aimchess-Zielverwaltung.

## Validierung

- Jahr und Kalenderwoche kommen aus der Route.
- Status ist Pflicht.
- Notiz ist optional.
- Notizlaenge begrenzen, zum Beispiel maximal 1000 Zeichen.
- Ungueltige Statuswerte werden abgelehnt.

## Akzeptanzkriterien

- `/week/{year}/{week}` zeigt einen Abschnitt `Aimchess Wochenabschluss`.
- Fuer eine ISO-Woche kann `Erreicht` gespeichert werden.
- Fuer eine ISO-Woche kann `Nicht erreicht` gespeichert werden.
- Eine optionale Notiz kann gespeichert und wieder angezeigt werden.
- Erneutes Speichern fuer dieselbe ISO-Woche aktualisiert den bestehenden Wochenabschluss.
- Wochen ohne gespeicherten Abschluss zeigen `noch nicht erfasst`.
- Die bestehenden Wochenstatistiken bleiben unveraendert.
- Es gibt keine automatische Aimchess-Anbindung.
- Einzelne Aimchess-Ziele muessen im MVP nicht strukturiert gepflegt werden.

## Tests bei Umsetzung

- Repository-/Service-Test: Wochenabschluss neu anlegen.
- Repository-/Service-Test: Wochenabschluss fuer gleiche ISO-Woche aktualisieren.
- Repository-/Service-Test: keine Duplikate fuer gleiche ISO-Woche.
- Service-Test: nicht erfasste Woche liefert Default-Status.
- MVC-Test: `GET /week/{year}/{week}` zeigt Wochenabschluss-Abschnitt.
- MVC-Test: `POST /week/{year}/{week}/goal-closure` speichert Status `Erreicht`.
- MVC-Test: `POST /week/{year}/{week}/goal-closure` speichert Status `Nicht erreicht`.
- MVC-Test: optionale Notiz wird angezeigt.
- Validierungstest: ungueltiger Status wird abgelehnt.
- Regression: `/today`, `/week`, `/month`, `/categories` bleiben erreichbar.

## Nicht-Ziele

- Aimchess API-Anbindung
- automatisches Auslesen der Aimchess-Wochenziele
- strukturierte Verwaltung einzelner Aimchess-Ziele
- Fortschrittsberechnung pro Aimchess-Ziel
- Benachrichtigungen oder Erinnerungen

## Status

umgesetzt
