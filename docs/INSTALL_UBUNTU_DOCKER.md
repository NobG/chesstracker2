# chesstracker2 auf Ubuntu mit Docker Compose installieren

Diese Anleitung beschreibt die Installation auf einem Ubuntu-Server. Codex hat keinen Zugriff auf den Zielserver und hat dort keine Befehle ausgefuehrt. Die Zielserver-Befunde in dieser Datei stammen aus der manuellen Installation und Validierung des Nutzers.

Repository:

```text
https://github.com/NobG/chesstracker2.git
```

Zielserver des Nutzers:

```text
IP: 82.165.0.60
Domain: chesstracker2.litux.de
Projektpfad: /opt/chesstracker2
```

## 1. Betriebsvarianten

Es gibt zwei Compose-Dateien:

- `docker-compose.yml`: Standardvariante fuer normale Docker-Hosts mit getrenntem Docker-Bridge-Netz.
- `docker-compose.hostnet.yml`: aktuell validierter Zielserver-Workaround mit `network_mode: host`.

Auf dem Zielserver des Nutzers muss aktuell die Hostnet-Datei verwendet werden:

```bash
docker compose -f docker-compose.hostnet.yml up -d
```

Nicht versehentlich nur `docker compose up -d` ausfuehren, wenn der Zielserver weiter das gemeldete Docker-Bridge-Problem hat.

Fuer Updates und regulaere Deployments ist auf dem Zielserver das Deploy-Skript vorgesehen:

```bash
cd /opt/chesstracker2
./scripts/deploy-chesstracker2.sh
```

## 2. Zielarchitektur

Standard-Bridge-Compose:

```text
Ubuntu Host
|-- nginx auf dem Host
`-- Docker Compose Bridge-Netz
    |-- chesstracker2-app
    `-- chesstracker2-db mit Volume chesstracker2_pgdata
```

Aktueller Zielserver-Workaround:

```text
https://chesstracker2.litux.de
  -> nginx auf dem Host
  -> http://127.0.0.1:8080
  -> chesstracker2-app im Host-Network
  -> PostgreSQL auf 127.0.0.1:15432
  -> chesstracker2-db im Host-Network
```

PostgreSQL lauscht im Hostnet-Compose bewusst nur auf `127.0.0.1:15432`.

## 3. Voraussetzungen pruefen

```bash
lsb_release -a
docker --version
docker compose version
git --version
nginx -v
```

Wenn Docker fehlt, installiere Docker nach der offiziellen Docker-Dokumentation fuer Ubuntu. Verwende keine unsicheren Curl-Pipe-Shell-Kommandos.

## 4. Projektverzeichnis anlegen

```bash
sudo mkdir -p /opt/chesstracker2
sudo chown "$USER":"$USER" /opt/chesstracker2
cd /opt/chesstracker2
```

## 5. Projekt auf den Server bringen

```bash
git clone https://github.com/NobG/chesstracker2.git /opt/chesstracker2
cd /opt/chesstracker2
```

Wenn das Verzeichnis bereits existiert:

```bash
cd /opt/chesstracker2
git pull
```

## 6. `.env` erstellen

```bash
cp .env.example .env
vi .env
```

Wichtige Werte:

```text
POSTGRES_DB=chesstracker2
POSTGRES_USER=chesstracker2
POSTGRES_PASSWORD=<sicheres-passwort>
APP_PORT=8080
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_PASSWORD=<gleiches-passwort>
```

Wichtig:

- `.env` nicht committen.
- Keine Passwoerter oder Secrets in Dokumentation oder Git-Historie schreiben.
- `POSTGRES_PASSWORD` und `SPRING_DATASOURCE_PASSWORD` muessen identisch sein, solange derselbe Datenbanknutzer verwendet wird.

## 7. Standardbetrieb fuer normale Docker-Hosts

Diese Variante nutzt `docker-compose.yml` mit Docker-Bridge-Netz:

```bash
docker compose config
docker compose build
docker compose up -d
docker compose ps
docker compose logs -f chesstracker2-app
```

Die interne JDBC-URL lautet in dieser Variante:

```text
jdbc:postgresql://chesstracker2-db:5432/chesstracker2
```

## 8. Betriebsvariante auf dem Zielserver: Host-Network-Compose

Das normale Docker-Bridge-Netz war auf dem Zielserver defekt oder blockiert. Die App konnte die DB im Docker-Bridge-Netz nicht erreichen und brach mit `NoRouteToHostException` ab. Laut Nutzer-Validierung scheiterten auch Container-Outbound/DNS-Tests teilweise im Bridge-Netz.

Die aktuell funktionierende Zielserver-Variante ist:

```bash
docker compose -f docker-compose.hostnet.yml up -d
```

Dabei gilt:

- `chesstracker2-app` nutzt `network_mode: host`.
- `chesstracker2-db` nutzt `network_mode: host`.
- PostgreSQL lauscht auf `127.0.0.1:15432`.
- Die App lauscht lokal auf `127.0.0.1:8080`.
- Die App verbindet sich mit `jdbc:postgresql://127.0.0.1:15432/chesstracker2`.
- Daten liegen im Volume `chesstracker2_pgdata`.

Status pruefen:

```bash
docker compose -f docker-compose.hostnet.yml ps
```

Logs:

```bash
docker compose -f docker-compose.hostnet.yml logs -f chesstracker2-app
docker compose -f docker-compose.hostnet.yml logs -f chesstracker2-db
```

Restart:

```bash
docker compose -f docker-compose.hostnet.yml restart chesstracker2-app
```

Stop:

```bash
docker compose -f docker-compose.hostnet.yml down
```

Nicht `docker compose down -v` ausfuehren, ausser die Datenbankdaten sollen bewusst geloescht werden.

## 9. Funktion pruefen

Lokal auf dem Server:

```bash
curl -I http://127.0.0.1:8080/today
curl -I http://127.0.0.1:8080/week
curl -I http://127.0.0.1:8080/month
curl -I http://127.0.0.1:8080/categories
```

Smoke-Tests:

```bash
BASE_URL=http://127.0.0.1:8080 ./scripts/smoke-test.sh
BASE_URL=http://127.0.0.1:8080 ./scripts/smoke-test-hostnet.sh
PUBLIC_BASE_URL=https://chesstracker2.litux.de ./scripts/smoke-test-hostnet.sh
```

Der Hostnet-Smoke-Test prueft Compose-Status, laufende App/DB-Container, `/today`, `/week`, `/month`, `/categories`, den Text `chesstracker2` auf `/today`, die Kategorie `Tactics` und optional die oeffentliche URL. Diese Tests sind lokal bzw. auf dem Server ausfuehrbar. Sie wurden nicht auf dem Zielserver durch Codex ausgefuehrt.

## 10. nginx einrichten

Die Datei `deploy/nginx-chesstracker2.conf` enthaelt den HTTP-Reverse-Proxy fuer:

```text
chesstracker2.litux.de -> http://127.0.0.1:8080
```

Installation:

```bash
sudo cp deploy/nginx-chesstracker2.conf /etc/nginx/sites-available/chesstracker2
sudo ln -s /etc/nginx/sites-available/chesstracker2 /etc/nginx/sites-enabled/chesstracker2
sudo nginx -t
sudo systemctl reload nginx
```

HTTPS mit Let's Encrypt/Certbot:

```bash
sudo certbot --nginx -d chesstracker2.litux.de
```

Certbot ergaenzt die echten Zertifikatspfade in der nginx-Konfiguration und kann HTTP auf HTTPS umleiten lassen.

Firewall-Erwartung:

- `80/tcp` oeffentlich erlaubt
- `443/tcp` oeffentlich erlaubt
- `8080/tcp` nicht oeffentlich freigegeben
- `15432/tcp` nicht oeffentlich freigegeben

## 11. systemd-Service einrichten

Fuer normale Docker-Hosts mit Bridge-Netz:

```bash
sudo cp deploy/chesstracker2-docker.service /etc/systemd/system/chesstracker2.service
```

Fuer den aktuellen Zielserver mit Hostnet-Workaround:

```bash
sudo cp deploy/chesstracker2-hostnet.service /etc/systemd/system/chesstracker2.service
```

Danach:

```bash
sudo systemctl daemon-reload
sudo systemctl enable chesstracker2
sudo systemctl start chesstracker2
sudo systemctl status chesstracker2
```

Wenn dein Projekt nicht unter `/opt/chesstracker2` liegt, passe `WorkingDirectory` in der Unit vorher an.

## 12. Updates im Hostnet-Betrieb

Auf dem Zielserver nutzt der Build aktuell `--network=host`, weil Maven im normalen Docker-Build-Netz keine externen Repositories erreichen konnte. Der empfohlene Update-Weg ist das automatisierte Deploy-Skript:

```bash
cd /opt/chesstracker2
./scripts/deploy-chesstracker2.sh
```

Das Skript:

- verhindert parallele Deploys per Lockfile
- prueft Projektverzeichnis, `.git`, `.env`, Docker Compose, Git, Curl und optional `nginx -t`
- bricht bei lokalen Git-Aenderungen ab
- zieht `origin/main` per Fast-Forward
- erstellt standardmaessig ein Predeploy-Backup, wenn `chesstracker2-db` laeuft
- baut `chesstracker2-chesstracker2-app:latest` und einen Commit-Tag mit `docker build --network=host`
- prueft `docker compose -f docker-compose.hostnet.yml config`
- startet `docker compose -f docker-compose.hostnet.yml up -d`
- wartet auf Health von `chesstracker2-db` und `chesstracker2-app`
- fuehrt am Ende `scripts/smoke-test-hostnet.sh` aus
- gibt bei Fehlern Compose-Status und Logs aus, ohne Secrets aus `.env` auszugeben

Optionen:

```bash
SKIP_BACKUP=true ./scripts/deploy-chesstracker2.sh
SKIP_GIT_PULL=true ./scripts/deploy-chesstracker2.sh
PUBLIC_BASE_URL= ./scripts/deploy-chesstracker2.sh
```

## 13. Backup und Restore im Hostnet-Betrieb

Backup:

```bash
cd /opt/chesstracker2
mkdir -p backups

docker compose -f docker-compose.hostnet.yml exec -T chesstracker2-db \
  pg_dump -h 127.0.0.1 -p 15432 -U chesstracker2 -d chesstracker2 \
  > backups/chesstracker2_$(date +%F_%H-%M-%S).sql
```

Restore:

```bash
cat backups/backup.sql | docker compose -f docker-compose.hostnet.yml exec -T chesstracker2-db \
  psql -h 127.0.0.1 -p 15432 -U chesstracker2 -d chesstracker2
```

## 14. Diagnose

Hostnet-Betrieb:

```bash
cd /opt/chesstracker2

docker compose -f docker-compose.hostnet.yml ps
docker compose -f docker-compose.hostnet.yml logs --tail=200 chesstracker2-app
docker compose -f docker-compose.hostnet.yml logs --tail=200 chesstracker2-db

curl -I http://127.0.0.1:8080/today
curl -I https://chesstracker2.litux.de/today

ss -tulpen | grep -E ':8080|:15432'
ufw status numbered
nginx -t
journalctl -u chesstracker2 -n 100 --no-pager
```

Bei einem Fehler im Deploy-Skript werden automatisch ausgegeben:

```bash
docker compose -f docker-compose.hostnet.yml ps
docker compose -f docker-compose.hostnet.yml logs --tail=120 chesstracker2-app
docker compose -f docker-compose.hostnet.yml logs --tail=80 chesstracker2-db
```

Docker-Bridge-Problem pruefen:

```bash
docker run --rm alpine:3.20 ping -c 2 1.1.1.1
docker run --rm alpine:3.20 nslookup repo.maven.apache.org
docker run --rm --network chesstracker2_default alpine:3.20 nc -vz chesstracker2-db 5432
```

Erwartung auf dem Zielserver bisher:

```text
Diese Tests koennen wegen Bridge-/Outbound-Problem fehlschlagen. Fuer chesstracker2 wird deshalb aktuell docker-compose.hostnet.yml verwendet.
```

## 15. Dokumentiertes Installationsergebnis

Siehe:

```text
docs/TARGET_DEPLOYMENT_NOTES.md
```

Kurzstand laut Nutzer-Validierung am 2026-06-08:

- `chesstracker2-app` healthy
- `chesstracker2-db` healthy
- Flyway `V001` und `V002` erfolgreich angewendet
- HTTPS unter `https://chesstracker2.litux.de/today` funktioniert
- Hostnet-Workaround aktiv, weil Docker-Bridge-Netz auf dem Zielserver nicht nutzbar ist

## 16. Deinstallation

Container stoppen, Daten behalten:

```bash
docker compose -f docker-compose.hostnet.yml down
```

Alles inklusive Daten loeschen:

```bash
docker compose -f docker-compose.hostnet.yml down -v
```

Das Datenbank-Volume wird dabei geloescht. Das ist gefaehrlich und darf nur bewusst gemacht werden.

systemd-Service entfernen:

```bash
sudo systemctl disable --now chesstracker2
sudo rm /etc/systemd/system/chesstracker2.service
sudo systemctl daemon-reload
```

nginx-Site entfernen:

```bash
sudo rm /etc/nginx/sites-enabled/chesstracker2
sudo rm /etc/nginx/sites-available/chesstracker2
sudo nginx -t
sudo systemctl reload nginx
```
