# chesstracker2 Backlog

## Erledigt

- Tagesdashboard fuer aktuelle Aimchess-Kategorien
- Trainingseintraege mit Ergebnisformat `success/total`
- Tagesnotiz
- Tageszusammenfassung mit Gesamtquote und Gesamtzeit
- Copy-Block fuer ChatGPT
- Wochenstatistik
- Monatsstatistik
- Kategorieuebersicht
- Smoke-Test und Deployment-Beispiele
- Rating-Snapshots manuell erfassen und anzeigen
- Session-/Login-Ablauf benutzerfreundlicher machen: 12h Session-Timeout, eigene 403-Seite und Browser-Draft fuer `/today`
- Ratingaenderungen fuer manuelle Ratings und Aimchess-Scores auf `/today` anzeigen

## Empfohlener naechster Umsetzungspunkt

- Wochenziel-Status auf der Wochenseite erfassen: Button/Feld `Aimchess Wochenziel geschafft`, damit pro Kalenderwoche gespeichert werden kann, ob das externe Aimchess-Wochenziel erreicht wurde. Keine Detailerfassung der einzelnen Aimchess-Ziele noetig; einfacher Ja/Nein-Status reicht.

## Produkt-Backlog

- Verbesserte Tagesbewertung: aktuelle `automaticSummary` nicht nur nach Erfolgsquote bewerten, sondern Trainingsumfang, Tagesabschluss, Kategorie-Mix, Quote, Score und gespeicherte Notizen getrennt ausweisen; Formulierung neutraler machen und erklaeren, dass es eine Regelbewertung ist, keine KI-Analyse.
- Verbesserte Validierung mit Inline-Fehlern
- Detailseite je Kategorie mit Diagramm
- Kalenderansicht
- Export als CSV
- Kategoriepflege im UI
- Training und Rating-Verlauf korrelieren
- Lichess Rating automatisch per API abrufen
- Partien und Performance automatisch von chess-results abrufen
- Partienlog ergaenzen
- Turniermodul ergaenzen

## Betriebs-Backlog

- Optional TLS-Automatisierung fuer nginx/certbot dokumentieren
- Restore-Prozess auf Staging-System testen
- Docker-Image-Publishing ueber GitHub Actions pruefen
- Docker-Bridge-Routing auf Zielserver sauber reparieren und danach von hostnet zurueck auf normales Compose pruefen.
- Deploy-Skript optional auf systemd timer oder manuell ausgeloesten Release-Prozess abstimmen.
