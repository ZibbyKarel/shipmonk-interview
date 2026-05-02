Run the whole app in Docker (not just the DB)

Context

docker-compose.yml today only starts the Postgres database; the Spring Boot app still has to be launched separately via ./mvnw spring-boot:run. We want a single docker compose up that brings up both the database and the app, so a fresh checkout can run end-to-end with no local Java/Maven required.

Two small frictions to resolve along the way:

- application.properties hardcodes 127.0.0.1:5432 for the DB, which won't resolve from inside a container — needs to become the compose service name (database) when running in Docker.
- The fixer.io API key is committed in application.properties; we'll keep that as the default but allow an env-var override so the compose file (and CI) can inject one without editing the file.

Changes

1.  New Dockerfile (multi-stage)

At repo root. Build stage uses the Maven wrapper and a JDK; runtime stage uses a slim JRE.

# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline
COPY src ./src
RUN ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/\*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

Notes:

- dependency:go-offline step is cached separately from the source copy so iterating on Java code doesn't re-download dependencies.
- The Spring Boot Maven plugin (already configured in pom.xml:73-75) produces an executable fat jar in target/, so java -jar is enough — no repackaging needed.
- If .mvn/ doesn't exist in the repo, drop that COPY line. (Quick check during execution.)

2.  New .dockerignore

Keep the build context small and avoid copying local build/IDE state into the image:

target/
.data/
.idea/
.vscode/
.git/
graphify-out/
\*.iml

3.  docker-compose.yml — add app service + DB healthcheck

version: "3.8"

services:
database:
image: postgres:14-alpine
volumes: - ./.data/db-local:/var/lib/postgresql/data
environment: - POSTGRES_DB=shipmonk-exchange-rates - POSTGRES_USER=shipmonk - POSTGRES_PASSWORD=secret
ports: - "127.0.0.1:5432:5432"
command: postgres -c random_page_cost=1.0
healthcheck:
test: ["CMD-SHELL", "pg_isready -U shipmonk -d shipmonk-exchange-rates"]
interval: 5s
timeout: 3s
retries: 10

app:
build: .
depends_on:
database:
condition: service_healthy
environment: - DB_HOST=database - FIXER_API_KEY=${FIXER_API_KEY:-ec4cadf1d3bcf4807427b6a5e570ed4d}
ports: - "127.0.0.1:8080:8080"

Why a healthcheck: without condition: service_healthy, the app container can race the DB on first boot and crashbefore Postgres accepts connections. depends_on alone only waits for the container to start, not for Postgres to be ready.

4.  src/main/resources/application.properties — make DB host + key overridable

Two one-line edits using Spring's ${VAR:default} placeholder syntax (so local non-Docker runs still workunchanged):

spring.datasource.url=jdbc:postgresql://${DB_HOST:127.0.0.1}:5432/shipmonk-exchange-rates
 ...
 fixer.api.key=${FIXER_API_KEY:ec4cadf1d3bcf4807427b6a5e570ed4d}

Critical files:

- /Users/zibby/Workspace/shipmonk-interview/docker-compose.yml
- /Users/zibby/Workspace/shipmonk-interview/Dockerfile (new)
- /Users/zibby/Workspace/shipmonk-interview/.dockerignore (new)
- /Users/zibby/Workspace/shipmonk-interview/src/main/resources/application.properties

Out of scope

- Switching the DB port binding off 127.0.0.1 (still useful for psql from the host).
- Live-reload bind mounts for development — first goal is just "it runs"; can layer on adocker-compose.override.yml later if wanted.
- Pushing the image anywhere / multi-arch builds.

Verification

1.  Clean build & up:
    docker compose build
    docker compose up -d
    docker compose ps # both services should be "running"; database should be "healthy"
    docker compose logs -f app # watch for "Started TestingdayExchangeRatesApplication"
2.  Smoke-test the API (uses the existing script):
    bash test.sh
3.  Expect HTTP 200s with fixer.io-shaped responses. The first call hits the upstream provider; the second call for the same date should be served from Postgres (cache hit).
4.  Confirm cache landed in Postgres:
    docker compose exec database psql -U shipmonk -d shipmonk-exchange-rates -c "select day, currency_count from<cache_table> limit 5;"
5.  (Replace <cache_table> with the actual table name — discoverable via \dt once connected.)
6.  Confirm local dev path still works (no Docker for the app): stop the app service, run ./mvnw spring-boot:run against the still-running database container, hit test.sh again — should behave identically because DB_HOST fallsback to 127.0.0.1.
7.  Tear down cleanly:
    docker compose down
8.  .data/db-local/ persists across restarts; delete it manually for a true reset.
