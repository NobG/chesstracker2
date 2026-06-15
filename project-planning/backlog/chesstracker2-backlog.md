# chesstracker2 Backlog

## MVP

- Tagesdashboard fuer aktuelle Aimchess-Kategorien
- Trainingseintraege mit Ergebnisformat `success/total`
- Tagesnotiz
- Tageszusammenfassung mit Gesamtquote und Gesamtzeit
- Copy-Block fuer ChatGPT
- Wochenstatistik
- Monatsstatistik
- Kategorieuebersicht
- Smoke-Test und Deployment-Beispiele

## Spaeter

- Lichess Rating automatisch per API abrufen
- Partien und Performance automatisch von chess-results abrufen
- Training und Rating-Verlauf korrelieren
- Turniermodul ergaenzen
- Partienlog ergaenzen
- Kategoriepflege im UI
- Detailseite je Kategorie mit Diagramm
- Export als CSV
- Kalenderansicht
- Ratingaenderung direkt auf der Startseite anzeigen, z. B. `+10` oder `-10` gegenueber dem vorherigen gespeicherten Ratingwert.
- Session-/Login-Ablauf benutzerfreundlicher machen: Wenn die Seite laenger offen ist und der Nutzer danach Ergebnisse speichert, sollen die eingegebenen Daten nicht verloren gehen. Statt aktueller Fehlermeldung und erneutem Login mit Neueingabe eine robuste Loesung umsetzen, z. B. Session verlaengern, Warnhinweis vor Ablauf, Zwischenspeichern im Browser oder sauberer Redirect mit Erhalt der Formulardaten.
- Verbesserte Tagesbewertung: aktuelle `automaticSummary` nicht nur nach Erfolgsquote bewerten, sondern Trainingsumfang, Tagesabschluss, Kategorie-Mix, Quote, Score und gespeicherte Notizen getrennt ausweisen; Formulierung neutraler machen und erklaeren, dass es eine Regelbewertung ist, keine KI-Analyse.
- Verbesserte Validierung mit Inline-Fehlern
- Optional TLS-Automatisierung fuer nginx/certbot dokumentieren
- Restore-Prozess auf Staging-System testen
- Docker-Image-Publishing ueber GitHub Actions pruefen
- Docker-Bridge-Routing auf Zielserver sauber reparieren und danach von hostnet zurueck auf normales Compose pruefen.
- Deploy-Skript optional auf systemd timer oder manuell ausgeloesten Release-Prozess abstimmen.
