# chesstracker2 Zielserver-Notizen

Diese Notizen dokumentieren den vom Nutzer manuell installierten und validierten Zielserver-Stand. Codex hat keinen Zugriff auf den Zielserver und hat dort keine Befehle ausgefuehrt.

## Zielserver-Validierung durch Nutzer am 2026-06-08

- Repository-Stand: `2ad479f Rename project to chesstracker2`
- Projektpfad: `/opt/chesstracker2`
- DNS: `chesstracker2.litux.de -> 82.165.0.60`
- AAAA: nicht gesetzt
- Docker-App: `chesstracker2-app` healthy
- Docker-DB: `chesstracker2-db` healthy
- DB-Port im Hostnet: `15432`
- App-Port lokal: `8080`
- nginx Reverse Proxy: `127.0.0.1:8080`
- HTTPS: funktioniert
- URL: `https://chesstracker2.litux.de/today`
- Flyway: `V001` und `V002` erfolgreich angewendet
- Besonderheit: Docker-Bridge-Netz auf Zielserver nicht nutzbar, Hostnet-Workaround aktiv

## Technischer Befund

Das normale Docker-Bridge-Netz war auf dem Zielserver defekt oder blockiert. Laut manueller Validierung des Nutzers konnten Container oeffentliche IPs/DNS nicht stabil erreichen, und die App konnte `chesstracker2-db:5432` im Compose-Bridge-Netz nicht per TCP erreichen. Spring Boot/Flyway brach dadurch mit `NoRouteToHostException` beim PostgreSQL-Verbindungsaufbau ab.

Der aktuell funktionierende Betrieb nutzt deshalb:

```bash
docker compose -f docker-compose.hostnet.yml up -d
```

Dabei bindet PostgreSQL nur auf `127.0.0.1:15432`, und nginx leitet HTTPS-Anfragen fuer `chesstracker2.litux.de` auf `127.0.0.1:8080` weiter.

## Sicherheitsnotizen

- Keine Passwoerter oder Secrets in Repository-Dateien dokumentieren.
- `15432/tcp` darf nicht oeffentlich freigegeben werden.
- `8080/tcp` darf nicht oeffentlich freigegeben werden.
- UFW soll oeffentlich nur `80/tcp` und `443/tcp` fuer nginx erlauben.
- Hostnet ist ein Workaround, bis das Docker-Bridge-Routing auf dem Zielserver repariert ist.
