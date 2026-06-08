# chesstracker auf Ubuntu mit Docker Compose installieren

Diese Anleitung beschreibt die Installation auf deinem Ubuntu-Server. Codex hat keinen Zugriff auf deinen Zielserver und hat dort keine Befehle ausgefuehrt. Du fuehrst die Schritte selbst auf dem Server aus.

Java und PostgreSQL muessen auf dem Server nicht direkt installiert werden. Beide laufen in Containern. Benoetigt werden Docker, Docker Compose, Git und optional nginx auf dem Host.

## 10.1 Ueberblick

Zielarchitektur:

```text
Ubuntu Host
|-- nginx auf dem Host
`-- Docker Compose
    |-- chesstracker-app
    `-- chesstracker-db mit persistentem Volume
```

Der App-Container lauscht intern auf Port `8080`. Docker Compose veroeffentlicht ihn nur lokal auf dem Host, zum Beispiel `127.0.0.1:8080`. nginx kann diesen lokalen Port als Reverse Proxy verwenden.

## 10.2 Voraussetzungen pruefen

```bash
lsb_release -a
docker --version
docker compose version
git --version
nginx -v
```

Wenn Docker fehlt, installiere Docker nach der offiziellen Docker-Dokumentation fuer Ubuntu. Verwende keine unsicheren Curl-Pipe-Shell-Kommandos.

nginx ist optional, aber fuer einen oeffentlichen Hostnamen als Reverse Proxy empfohlen.

## 10.3 Projektverzeichnis anlegen

```bash
sudo mkdir -p /opt/chesstracker
sudo chown "$USER":"$USER" /opt/chesstracker
cd /opt/chesstracker
```

## 10.4 Projekt auf den Server bringen

Weg A: per Git

```bash
git clone <REPOSITORY_URL> /opt/chesstracker
cd /opt/chesstracker
```

Weg B: per ZIP/SCP

```bash
scp chesstracker.zip user@server:/opt/
unzip chesstracker.zip -d /opt/chesstracker
cd /opt/chesstracker
```

Wenn du Weg B nutzt, pruefe danach, dass `Dockerfile`, `docker-compose.yml`, `.env.example` und `src/` im Projektverzeichnis liegen.

## 10.5 `.env` erstellen

```bash
cp .env.example .env
vi .env
```

Passe mindestens diese Werte an:

- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_PORT`
- optional `SPRING_PROFILES_ACTIVE`

Wichtig:

- `.env` nicht committen.
- Setze ein sicheres Passwort.
- `POSTGRES_PASSWORD` und `SPRING_DATASOURCE_PASSWORD` muessen identisch sein, solange derselbe Datenbanknutzer verwendet wird.
- Die interne JDBC-URL lautet `jdbc:postgresql://chesstracker-db:5432/chesstracker`.

## 10.6 Container bauen und starten

```bash
docker compose config
docker compose build
docker compose up -d
docker compose ps
docker compose logs -f chesstracker-app
```

Beim ersten Start fuehrt Flyway die Datenbankmigrationen automatisch aus.

## 10.7 Funktion pruefen

```bash
curl -I http://127.0.0.1:8080/today
curl http://127.0.0.1:8080/today | head
```

Falls vorhanden:

```bash
BASE_URL=http://127.0.0.1:8080 ./scripts/smoke-test.sh
./scripts/smoke-test-docker.sh
```

Diese Smoke-Tests sind lokal bzw. auf deinem Server ausfuehrbar. Sie wurden nicht auf deinem Zielserver durch Codex ausgefuehrt.

## 10.8 nginx einrichten

Passe zuerst `server_name` in `deploy/nginx-chesstracker.conf` auf deine Domain an.

```bash
sudo cp deploy/nginx-chesstracker.conf /etc/nginx/sites-available/chesstracker
sudo ln -s /etc/nginx/sites-available/chesstracker /etc/nginx/sites-enabled/chesstracker
sudo nginx -t
sudo systemctl reload nginx
```

Die MVP-Konfiguration enthaelt keine TLS-Einrichtung. TLS kannst du spaeter zum Beispiel mit certbot ergaenzen.

## 10.9 systemd-Service fuer Docker Compose einrichten

Die empfohlene Betriebsart ist Docker Compose. Der systemd-Service startet und stoppt Compose im Projektverzeichnis.

```bash
sudo cp deploy/chesstracker-docker.service /etc/systemd/system/chesstracker.service
sudo systemctl daemon-reload
sudo systemctl enable chesstracker
sudo systemctl start chesstracker
sudo systemctl status chesstracker
```

Wenn dein Projekt nicht unter `/opt/chesstracker` liegt, passe `WorkingDirectory` in der Unit vorher an.

## 10.10 Betrieb

```bash
docker compose ps
docker compose logs -f
docker compose logs -f chesstracker-app
docker compose logs -f chesstracker-db
docker compose restart chesstracker-app
docker compose down
docker compose up -d
```

`docker compose down` stoppt und entfernt Container, behaelt aber das Datenbank-Volume.

## 10.11 Updates

```bash
cd /opt/chesstracker
git pull
docker compose build
docker compose up -d
docker compose logs -f chesstracker-app
```

Flyway laeuft beim App-Start automatisch und wendet neue Migrationen an.

## 10.12 Backup

Backup:

```bash
mkdir -p backups
docker compose exec -T chesstracker-db pg_dump -U chesstracker -d chesstracker > backups/chesstracker_$(date +%F_%H-%M-%S).sql
```

Restore:

```bash
cat backups/backup.sql | docker compose exec -T chesstracker-db psql -U chesstracker -d chesstracker
```

Unterschied beim Stoppen:

```bash
docker compose down
```

Daten bleiben erhalten.

```bash
docker compose down -v
```

Das Datenbank-Volume wird geloescht. Das ist gefaehrlich und darf nur bewusst gemacht werden.

## 10.13 Fehlerdiagnose

```bash
docker compose ps
docker compose logs chesstracker-app
docker compose logs chesstracker-db
docker compose exec chesstracker-db pg_isready -U chesstracker -d chesstracker
docker compose exec chesstracker-db psql -U chesstracker -d chesstracker
curl -I http://127.0.0.1:8080/today
sudo nginx -t
sudo journalctl -u chesstracker -n 100 --no-pager
```

Typische Fehler:

- App startet vor DB: Compose wartet auf den DB-Healthcheck, aber bei langsamen Servern koennen Logs helfen.
- Falsches Datenbankpasswort: `POSTGRES_PASSWORD` und `SPRING_DATASOURCE_PASSWORD` muessen zusammenpassen.
- Port 8080 bereits belegt: `APP_PORT` in `.env` aendern und nginx auf den neuen lokalen Port zeigen lassen.
- nginx `server_name` falsch: Domain in `deploy/nginx-chesstracker.conf` anpassen.
- `.env` fehlt: `.env.example` kopieren und Werte setzen.
- Volume versehentlich geloescht: Backup einspielen, wenn `docker compose down -v` ausgefuehrt wurde.

## 10.14 Deinstallation

Container stoppen, Daten behalten:

```bash
docker compose down
```

Alles inklusive Daten loeschen:

```bash
docker compose down -v
```

systemd-Service entfernen:

```bash
sudo systemctl disable --now chesstracker
sudo rm /etc/systemd/system/chesstracker.service
sudo systemctl daemon-reload
```

nginx-Site entfernen:

```bash
sudo rm /etc/nginx/sites-enabled/chesstracker
sudo rm /etc/nginx/sites-available/chesstracker
sudo nginx -t
sudo systemctl reload nginx
```
