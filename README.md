# chesstracker2

Privates Webtool zum Tracken des taeglichen Aimchess-Trainings. Der MVP fokussiert Tagesdashboard, Trainingseintraege, Tagesauswertung, Copy-Block fuer ChatGPT sowie einfache Wochen-, Monats- und Kategorie-Statistiken.

## Technologie

- Java 21
- Spring Boot 3.3.x, Spring MVC, Thymeleaf
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker Compose fuer den empfohlenen Betrieb
- nginx als Reverse Proxy auf Ubuntu

## Docker-Schnellstart

Der empfohlene Betrieb ist Docker Compose. Die Spring-Boot-App laeuft im Container `chesstracker2-app`, PostgreSQL im Container `chesstracker2-db`. Daten liegen dauerhaft im Docker Volume `chesstracker2_pgdata`. nginx kann optional auf dem Host laufen und auf `127.0.0.1:8080` weiterleiten.

```bash
cp .env.example .env
vi .env
docker compose up -d --build
docker compose logs -f chesstracker2-app
```

Wichtig: `POSTGRES_PASSWORD` und `SPRING_DATASOURCE_PASSWORD` muessen identisch sein, solange derselbe Datenbanknutzer verwendet wird.

Ausfuehrliche Ubuntu-Anleitung:

```text
docs/INSTALL_UBUNTU_DOCKER.md
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
- `chesstracker2-java.service.example`
- `nginx-chesstracker2.conf`
- `env.example`

Docker Compose ist die empfohlene Betriebsart. Die vollstaendige Schritt-fuer-Schritt-Anleitung liegt in:

```text
docs/INSTALL_UBUNTU_DOCKER.md
```

nginx leitet auf den lokalen Spring-Boot-Port weiter. Siehe `deploy/nginx-chesstracker2.conf`.

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

## Projektplanung

Die Planung liegt in `project-planning/` mit Backlog, Epics, Meilensteinplan und Story-Dateien. Der aktuelle Fokus steht in `project-planning/current-focus.md`.
