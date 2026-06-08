# chesstracker

Privates Webtool zum Tracken des taeglichen Aimchess-Trainings. Der MVP fokussiert Tagesdashboard, Trainingseintraege, Tagesauswertung, Copy-Block fuer ChatGPT sowie einfache Wochen-, Monats- und Kategorie-Statistiken.

## Technologie

- Java 21
- Spring Boot 3.3.x, Spring MVC, Thymeleaf
- Spring Data JPA
- PostgreSQL
- Flyway
- nginx als Reverse Proxy auf Ubuntu

## Lokaler Start

Voraussetzungen:

- Java 21
- Maven Wrapper (`./mvnw` oder `mvnw.cmd`)
- Docker Compose (optional fuer lokale PostgreSQL-Entwicklung)

Lokale PostgreSQL-Entwicklung:

```bash
docker compose up -d postgres
```

Die Standard-Dev-Konfiguration nutzt dann:

- Datenbank: `chesstracker`
- Benutzer: `chesstracker`
- Passwort: `chesstracker_dev`

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
export DB_URL=jdbc:postgresql://localhost:5432/chesstracker
export DB_USER=chesstracker
export DB_PASSWORD=change-me
./mvnw spring-boot:run
```

Die Anwendung laeuft dann standardmaessig unter `http://localhost:8080`.

## Konfiguration

Die zentrale Konfiguration liegt in `src/main/resources/application.yml`.

Wichtige Umgebungsvariablen:

- `SERVER_PORT`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
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

Das Artefakt liegt danach unter `target/chesstracker-0.1.0-SNAPSHOT.jar`.

## Ubuntu-Betrieb

Beispiele liegen in `deploy/`:

- `chesstracker.service`
- `nginx-chesstracker.conf`
- `env.example`

Typischer Ablauf:

```bash
sudo useradd --system --home /opt/chesstracker --shell /usr/sbin/nologin chesstracker
sudo mkdir -p /opt/chesstracker
sudo cp target/chesstracker-0.1.0-SNAPSHOT.jar /opt/chesstracker/chesstracker.jar
sudo cp deploy/env.example /opt/chesstracker/env
sudo cp deploy/chesstracker.service /etc/systemd/system/chesstracker.service
sudo systemctl daemon-reload
sudo systemctl enable --now chesstracker
```

nginx leitet auf den lokalen Spring-Boot-Port weiter. Siehe `deploy/nginx-chesstracker.conf`.

## Smoke-Test

Wenn die Anwendung lokal laeuft:

```bash
BASE_URL=http://localhost:8080 ./scripts/smoke-test.sh
```

Der Smoke-Test prueft `/today`, `/week`, `/month`, `/categories`, die HTTP-Statuscodes sowie den Copy-Block und eine Aimchess-Kategorie. Optional kann mit `BASE_URL` ein anderer Host festgelegt werden.

## Projektplanung

Die Planung liegt in `project-planning/` mit Backlog, Epics, Meilensteinplan und Story-Dateien. Der aktuelle Fokus steht in `project-planning/current-focus.md`.
