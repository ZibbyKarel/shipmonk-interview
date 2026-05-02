# Exchange Rates Service

Original assignment description [here](./ASSIGNMENT.md).

Caching proxy over [fixer.io](https://fixer.io/) historical exchange rates. Rates are stored in PostgreSQL — a second request for the same date is served from cache without hitting the external API. Base currency is always USD.

---

## Quick Start (Docker)

```bash
# Starts PostgreSQL and the application together
FIXER_API_KEY=<your_key> docker compose up --build
```

If `FIXER_API_KEY` is omitted, the demo key from `application.properties` is used. The app is available at `http://localhost:8080`.

---

## Local Development

```bash
# 1. Start only the database
docker-compose up -d database

# 2. Run the application locally
./mvnw spring-boot:run
```

---

## Build

```bash
./mvnw package -DskipTests
```

---

## Tests

```bash
# Unit tests (*Test.java, Surefire)
./mvnw test

# Unit + integration tests (*IT.java, Failsafe)
./mvnw verify

# Integration tests only (unit tests must have passed in this build)
./mvnw failsafe:integration-test

# Single unit test class
./mvnw test -Dtest=ExchangeRatesServiceTest

# Single integration test class
./mvnw verify -Dit.test=ExchangeRatesIT -DfailIfNoTests=false
```

Integration tests run against an H2 in-memory database — no PostgreSQL required.

### Smoke test

```bash
bash test.sh
```

Sends 5 example requests to a running application (mix of historical and future dates).

---

## API

```
GET /api/v1/rates/{day}
```

- `{day}` — date in `YYYY-MM-DD` format
- Returns exchange rates for all currencies relative to USD on the given day

**HTTP status codes:**

| Code | Situation |
| ---- | --------- |
| 200 | Rates found (from cache or freshly fetched) |
| 400 | Invalid date format or future date |
| 404 | fixer.io has no data for the requested date |
| 502 | fixer.io unavailable or permanent error (retries exhausted) |

**Example response:**

```json
{
    "date": "2022-06-20",
    "timestamp": 1655769599,
    "base": "USD",
    "rates": {
        "EUR": 0.948,
        "GBP": 0.817,
        "CZK": 23.15
    }
}
```

**Request tracing:** every request gets an `X-Trace-Id` header (or one can be passed in). The ID is propagated through all layers via MDC.

**OpenAPI / Swagger UI:** available at `http://localhost:8080/swagger-ui/index.html` when the application is running.

---

## Architecture

Detailed dependency map and request-flow sequence diagram: **[docs/architecture.html](docs/architecture.html)**

Layers:

- **Controller** (`rates/controller`) — parses and validates `{day}`, delegates to service
- **Service** (`rates/service`) — cache-first logic; on cache miss calls provider and persists to DB
- **Repository** (`rates/repository`) — JPA/Hibernate, `ExchangeRateSnapshot` entity with unique index on date
- **Provider** (`rates/provider`) — `ExchangeRateProvider` interface; `FixerExchangeRateProvider` implementation with `@Retry` (3 attempts, exponential backoff)
- **Shared** (`shared/`) — `GlobalExceptionHandler`, `TraceIdFilter`, `AppConfig`

---

## Tooling

This project was built with [Claude Code](https://claude.ai/code) (Anthropic's AI coding assistant). Two Claude Code features were used throughout development to improve code quality and reduce token usage:

- **Skills** — reusable prompt workflows invoked with `/skill-name`; kept recurring tasks (e.g. architecture review) consistent and cheap across sessions.
- **Graphify** — maintains a persistent knowledge graph of the codebase (AST-based, no API cost); lets Claude answer architecture and cross-file questions without re-reading the entire repo each time.

---

## Development Phases

| Phase | What was solved |
| ----- | --------------- |
| **1 — Core cache** | JPA entity + `RatesMapConverter` (JSON in TEXT column), `FixerClient`, service with cache-aside logic, exception hierarchy (`InvalidDateException`, `RatesNotFoundException`, `ProviderException`), `GlobalExceptionHandler` |
| **2 — Provider abstraction + retry** | `ExchangeRateProvider` interface decoupled fixer.io from the service layer; `TransientProviderException` + Resilience4j `@Retry` (3× with exponential backoff 1 s/2 s); USD rebasing moved into the provider |
| **3 — Package-by-domain** | Migrated from flat package-by-layer to a `rates/` domain and `shared/` cross-cutting layer |
| **4 — Dockerization** | Multi-stage Dockerfile (build → slim JRE), database healthcheck in `docker-compose.yml`, env-var overrides for `DB_HOST` and `FIXER_API_KEY` |
| **5 — Logging + traceId** | `TraceIdFilter` generates/propagates `X-Trace-Id`, MDC integration across all layers, `logback-spring.xml`, interceptor copying traceId onto outbound fixer.io requests |
