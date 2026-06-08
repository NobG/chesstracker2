# chesstracker2

Privates Webtool zum Tracken des taeglichen Aimchess-Trainings. Der technische Projektname ist durchgehend `chesstracker2`. Der MVP fokussiert Tagesdashboard, Trainingseintraege, Tagesauswertung, Copy-Block fuer ChatGPT sowie einfache Wochen-, Monats- und Kategorie-Statistiken.

## Technologie

- Java 21
- Spring Boot 3.3.x, Spring MVC, Thymeleaf
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker Compose fuer den empfohlenen Betrieb
- nginx als Reverse Proxy auf Ubuntu

## Docker-Schnellstart

Der Standardbetrieb fuer normale Docker-Hosts ist Docker Compose mit Bridge-Netz. Die Spring-Boot-App laeuft im Container `chesstracker2-app`, PostgreSQL im Container `chesstracker2-db`. Daten liegen dauerhaft im Docker Volume `chesstracker2_pgdata`. nginx kann optional auf dem Host laufen und auf `127.0.0.1:8080` weiterleiten.

```bash
cp .env.example .env
vi .env
docker compose up -d --build
docker compose logs -f chesstracker2-app
```

Wichtig: `POSTGRES_PASSWORD` und `SPRING_DATASOURCE_PASSWORD` muessen identisch sein, solange derselbe Datenbanknutzer verwendet wird.

## Zielserverbetrieb

Der aktuell validierte Zielserverbetrieb fuer `chesstracker2.litux.de` nutzt `docker-compose.hostnet.yml`:

```bash
docker compose -f docker-compose.hostnet.yml up -d
docker compose -f docker-compose.hostnet.yml ps
```

Fuer Deployments auf dem Zielserver ist das automatisierte Skript vorgesehen:

```bash
cd /opt/chesstracker2
./scripts/deploy-chesstracker2.sh
```

Das Skript zieht `origin/main`, erstellt vor dem Deploy ein Backup, baut das Image mit `docker build --network=host`, startet `docker-compose.hostnet.yml`, wartet auf Container-Health und fuehrt danach den Hostnet-Smoke-Test aus.

Grund: Auf dem Zielserver ist das Docker-Bridge-Routing laut manueller Nutzer-Validierung defekt oder blockiert. Die App konnte die DB im Bridge-Netz nicht erreichen und brach mit `NoRouteToHostException` ab. Codex hat den Zielserver nicht selbst geprueft.

nginx laeuft auf dem Host und leitet weiter:

```text
chesstracker2.litux.de -> http://127.0.0.1:8080
```

PostgreSQL laeuft im Hostnet-Workaround auf `127.0.0.1:15432`. Die Ports `8080` und `15432` duerfen nicht oeffentlich freigegeben werden.

Ausfuehrliche Ubuntu-Anleitung:

```text
docs/INSTALL_UBUNTU_DOCKER.md
```

Zielserver-Notizen:

```text
docs/TARGET_DEPLOYMENT_NOTES.md
```

## Lokale Entwicklung ohne Docker-App

Voraussetzungen:

- Java 21
- Maven Wrapper (`./mvnw` oder `mvnw.cmd`)
- PostgreSQL lokal oder per Docker Compose

Die Standard-Dev-Konfiguration nutzt:

- Datenbank: `chesstracker2`
- Benutzer: `chesstracker2`
- Passwort: `chesstracker2_dev`

Start mit dem Wrapper:

```bash
./mvnw spring-boot:run
```

Oder mit explizitem Profil:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Produktiv/Deployment:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chesstracker2
export SPRING_DATASOURCE_USERNAME=chesstracker2
export SPRING_DATASOURCE_PASSWORD=change-me
./mvnw spring-boot:run
```

Die Anwendung laeuft dann standardmaessig unter `http://localhost:8080`.

## Konfiguration

Die zentrale Konfiguration liegt in `src/main/resources/application.yml`.

Wichtige Umgebungsvariablen:

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE=dev|prod`

PostgreSQL wird fuer `dev` und `prod` verwendet. H2 ist bewusst nur als Test-Dependency gescoped und landet nicht im normalen Spring-Boot-Artefakt. Dadurch bleiben Repository- und Service-Tests lokal reproduzierbar, waehrend die laufende Anwendung dieselbe Datenbankklasse nutzt wie die Zielumgebung.

## Migrationen

Flyway fuehrt beim Start automatisch Migrationen aus:

- `V001__initial_schema.sql`
- `V002__seed_training_categories.sql`

## Build

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw package
```

Das Artefakt liegt danach unter `target/chesstracker2-0.1.0-SNAPSHOT.jar`.

## Ubuntu-Betrieb

Beispiele liegen in `deploy/`:

- `chesstracker2-docker.service`
- `chesstracker2-hostnet.service`
- `chesstracker2-java.service.example`
- `nginx-chesstracker2.conf`
- `env.example`

`chesstracker2-docker.service` ist die Standard-Bridge-Variante. Fuer den aktuellen Zielserver wird `chesstracker2-hostnet.service` empfohlen, weil dort `docker-compose.hostnet.yml` verwendet werden muss.

Die vollstaendige Schritt-fuer-Schritt-Anleitung liegt in:

```text
docs/INSTALL_UBUNTU_DOCKER.md
```

nginx leitet auf den lokalen Spring-Boot-Port weiter. Siehe `deploy/nginx-chesstracker2.conf`.

## Backup und Update im Hostnet-Betrieb

Backup:

```bash
cd /opt/chesstracker2
mkdir -p backups

docker compose -f docker-compose.hostnet.yml exec -T chesstracker2-db \
  pg_dump -h 127.0.0.1 -p 15432 -U chesstracker2 -d chesstracker2 \
  > backups/chesstracker2_$(date +%F_%H-%M-%S).sql
```

Update:

```bash
cd /opt/chesstracker2
./scripts/deploy-chesstracker2.sh
```

Der Build nutzt auf dem Zielserver aktuell `--network=host`, weil Maven im normalen Docker-Build-Netz keine externen Repositories erreichen konnte. Das Skript gibt bei Fehlern Compose-Status sowie Logs von `chesstracker2-app` und `chesstracker2-db` aus. Es verwendet nie `docker compose down -v`, `docker system prune -a` oder Volume-Loeschbefehle.

Optionen:

```bash
SKIP_BACKUP=true ./scripts/deploy-chesstracker2.sh
SKIP_GIT_PULL=true ./scripts/deploy-chesstracker2.sh
PUBLIC_BASE_URL= ./scripts/deploy-chesstracker2.sh
```

## Smoke-Test

Wenn die Anwendung lokal laeuft:

```bash
BASE_URL=http://localhost:8080 ./scripts/smoke-test.sh
```

Der Smoke-Test prueft `/today`, `/week`, `/month`, `/categories`, die HTTP-Statuscodes sowie den Copy-Block und eine Aimchess-Kategorie. Optional kann mit `BASE_URL` ein anderer Host festgelegt werden.

Docker-Smoke-Test:

```bash
./scripts/smoke-test-docker.sh
```

Dieser Test prueft Compose-Konfiguration, Containerstart, HTTP-Endpunkte und PostgreSQL-Bereitschaft. Er wurde nicht auf dem Ubuntu-Zielserver durch Codex ausgefuehrt.

Hostnet-Smoke-Test:

```bash
BASE_URL=http://127.0.0.1:8080 ./scripts/smoke-test-hostnet.sh
PUBLIC_BASE_URL=https://chesstracker2.litux.de ./scripts/smoke-test-hostnet.sh
```

Dieser Test erwartet einen laufenden Hostnet-Compose-Stack und prueft die lokalen Kernrouten, `chesstracker2` im `/today`-HTML sowie die Kategorie `Tactics`. Die oeffentliche URL wird nur geprueft, wenn `PUBLIC_BASE_URL` gesetzt ist.

## Projektplanung

Die Planung liegt in `project-planning/` mit Backlog, Epics, Meilensteinplan und Story-Dateien. Der aktuelle Fokus steht in `project-planning/current-focus.md`.
