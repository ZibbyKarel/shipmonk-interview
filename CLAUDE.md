# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project purpose

Exchange rates caching service built as an interview task for ShipMonk. It acts as a caching layer over [fixer.io](https://fixer.io/) and stores rates in PostgreSQL. Base currency is USD.

## Commands

```bash
# Start the local database
docker-compose up -d database

# Build (skipping tests)
./mvnw package -DskipTests

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ExchangeRatesControllerTest

# Example API calls
bash test.sh
```

## Architecture

Spring Boot 2.7 / Java 17 / Maven project. Entry point: `TestingdayExchangeRatesApplication`.

The single implemented endpoint is `GET /api/v1/rates/{day}` in `ExchangeRatesController`. The response shape should mirror the fixer.io historical rates API.

Expected layers to build:
- **Controller** (`ExchangeRatesController`) — parses the `{day}` path variable (format `YYYY-MM-DD`) and delegates to a service.
- **Service** — checks PostgreSQL cache first; on cache miss, calls fixer.io and persists the result.
- **Repository** — Hibernate/JPA entity + repository for cached daily rates.
- **Fixer.io client** — HTTP client (e.g. `RestTemplate` or `WebClient`) calling `https://data.fixer.io/api/{date}?access_key=...&base=USD`.

## Database

PostgreSQL 14 runs via Docker Compose. Connection details in `application.properties`:
- URL: `jdbc:postgresql://127.0.0.1:5432/shipmonk-exchange-rates`
- User/password: `shipmonk` / `secret`

The assignment calls for Hibernate — use `spring-boot-starter-data-jpa` and define schema via `spring.jpa.hibernate.ddl-auto` or a migration tool.

## Key constraints from the assignment

- Architecture quality is prioritised over full feature coverage.
- No authentication needed (internal service).
- Edge cases and error handling are explicitly evaluated — downstream consumers should not need to handle exchange-rate fetch errors themselves.
- Fixer.io free tier uses `http` (not `https`) and requires an access key; store the key in `application.properties` (not hardcoded).

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
