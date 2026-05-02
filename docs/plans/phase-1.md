ShipMonk interview task: build a Spring Boot caching layer over fixer.io that serves GET /api/v1/rates/{day}. The app fetches from fixer.io on cache miss, stores in PostgreSQL, and returns fixer.io-shaped JSON with USD as base currency.

Current State

- Spring Boot 2.7 / Java 17 / Maven skeleton exists
- ExchangeRatesController stub returns empty Map.of()
- DB configured (PostgreSQL 14 via Docker Compose) but JPA not wired
- pom.xml is missing spring-boot-starter-data-jpa
- Fixer API key already in application.properties

Critical fixer.io constraint: Free plan does not allow changing the base currency — always returns EUR-based rates. We must rebase from EUR to USD in the service layer: usdRate(X) = eurRate(X) / eurRate(USD).

---

Package Structure

com.shipmonk.testingday/
├── config/ AppConfig.java (RestTemplate bean, @Value properties)
├── controller/ ExchangeRatesController.java
├── service/ ExchangeRatesService.java
├── repository/ ExchangeRateSnapshotRepository.java
├── entity/ ExchangeRateSnapshot.java
├── client/ FixerClient.java
│ └── dto/ FixerRatesResponse.java, FixerErrorResponse.java
├── dto/ RatesResponse.java, ErrorResponse.java
├── converter/ RatesMapConverter.java (Map<String,BigDecimal> ↔ JSON)
└── exception/ ExchangeRateException.java, RatesNotFoundException.java,
FixerApiException.java, GlobalExceptionHand

---

Step-by-Step Implementation

1 — pom.xml: add missing dependency

Add spring-boot-starter-data-jpa to pom.xml. No version needed (managed by Spring Boot BOM).

File: pom.xml

2 — application.properties: add JPA + fixer config

# Existing DB config stays as-is

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

fixer.api.base-url=http://data.fixer.io/api
fixer.api.key=<loaded from existing property key>
Note: rename spring.datasource.fixer_api_key → fixer.api.key (wrong namespace in existing file).

File: src/main/resources/application.properties

3 — ExchangeRateSnapshot entity

JPA entity storing one row per date.

@Entity
@Table(name = "exchange_rate_snapshot",
uniqueConstraints = @UniqueConstraint(columnNames = "date"))
public class ExchangeRateSnapshot {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private String base;       // always "USD"

    @Column(nullable = false)
    private Long timestamp;    // UNIX timestamp from fixer

    @Convert(converter = RatesMapConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private Map<String, BigDecimal> rates;

    @Column(nullable = false)
    private LocalDateTime fetchedAt;  // when we retrieved it fro

}

File: src/main/java/com/shipmonk/testingday/entity/ExchangeRateSnapshot.java

4 — RatesMapConverter

JPA AttributeConverter<Map<String, BigDecimal>, String> using Jackson ObjectMapper.

File: src/main/java/com/shipmonk/testingday/converter/RatesMapConverter.java

5 — ExchangeRateSnapshotRepository

public interface ExchangeRateSnapshotRepository extends JpaRepository<ExchangeRateSnapshot, Long> {
Optional<ExchangeRateSnapshot> findByDate(LocalDate date);
}

File: src/main/java/com/shipmonk/testingday/repository/ExchangeRateSnapshotRepository.java

6 — Fixer.io DTOs

FixerRatesResponse: maps fixer JSON (success, historical, timestamp, base, date, rates).
FixerErrorResponse: maps {"success": false, "error": {"code": int

File: src/main/java/com/shipmonk/testingday/client/dto/

7 — FixerClient

Wraps RestTemplate. Single method:
FixerRatesResponse fetchHistoricalRates(LocalDate date);

- Calls GET {base-url}/{date}?access_key={key}
- On HTTP error or success=false in body → throw FixerApiException with fixer's code + info
- On network/timeout → throw FixerApiException wrapping the cause

File: src/main/java/com/shipmonk/testingday/client/FixerClient.ja

8 — ExchangeRatesService (cache-aside)

RatesResponse getRatesForDay(LocalDate date);

Logic:

1. Validate date: reject future dates with IllegalArgumentException
2. Check DB: repository.findByDate(date) → if present, map to res
3. Cache miss: call fixerClient.fetchHistoricalRates(date)
4. Rebase EUR→USD: divide every rate by rates.get("USD"); set bas
5. Persist: save ExchangeRateSnapshot; handle DataIntegrityViolationException (concurrent write) by re-reading from DB
6. Return mapped RatesResponse

File: src/main/java/com/shipmonk/testingday/service/ExchangeRates

9 — Response / Error DTOs

RatesResponse (mirrors fixer historical response):
{ "success": true, "historical": true, "date": "...", "timestamp": ..., "base": "USD", "rates": {...} }
ErrorResponse:
{ "success": false, "error": { "code": int, "info": "..." } }

File: src/main/java/com/shipmonk/testingday/dto/

10 — Exception hierarchy

ExchangeRateException (base, RuntimeException)
├── RatesNotFoundException → 404 (date not available in fixer)
├── FixerApiException → 502 (upstream error — rate limit, auth, server error)
└── InvalidDateException → 400 (future date, malformed forma

File: src/main/java/com/shipmonk/testingday/exception/

11 — GlobalExceptionHandler (@ControllerAdvice)

Maps exceptions to ErrorResponse + HTTP status:

│ Exception │ HTTP Status │
├───────────────────────────────────────────────────────┼───────
│ InvalidDateException │ 400 │
├───────────────────────────────────────────────────────┼───────
│ RatesNotFoundException │ 404 │
├───────────────────────────────────────────────────────┼───────
│ FixerApiException │ 502 │
├───────────────────────────────────────────────────────┼───────
│ MethodArgumentTypeMismatchException (bad date format) │ 400 │
├───────────────────────────────────────────────────────┼───────
│ Exception (catch-all) │ 500 │
└───────────────────────────────────────────────────────┴───────

File: src/main/java/com/shipmonk/testingday/exception/GlobalExce

12 — ExchangeRatesController (update)

- Parse {day} as @PathVariable String, manually parse to LocalDation on failure (more explicit than relying on Spring's converter for error messaging)
- Inject ExchangeRatesService, call getRatesForDay(date)
- Return ResponseEntity<RatesResponse>

File: src/main/java/com/shipmonk/testingday/controller/ExchangeRatesController.java

13 — AppConfig

@Configuration bean providing:

- RestTemplate with reasonable timeouts (connect=5s, read=10s)
- @Value bindings for fixer.api.base-url and fixer.api.key

File: src/main/java/com/shipmonk/testingday/config/AppConfig.java

14 — One integration test (optional but demonstrates testability)

ExchangeRatesControllerTest using @SpringBootTest + MockRestServiceServer to mock fixer.io, verifying:

- Cache hit returns from DB without calling fixer
- Cache miss calls fixer, rebases to USD, stores in DB
- Invalid date returns 400
- Future date returns 400

---

Edge Cases Handled

┌───────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────┐
│ Edge Case │ Handling │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Malformed date in URL (e.g. 2022-13-40) │ Parsed in controller → InvalidDateException → 400 │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Future date requested │ Service validates before calling fixer → 400 │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Today's date │ Allowed — cached like any other date (rates are intraday; may reflect previous close) │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Fixer free plan: base currency locked to EUR │ Service rebases EUR→USD using rate(X)/rate(USD) │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Fixer success:false response (rate limit, auth, etc.) │ FixerClient throws FixerApiException → 502 with fixer's message │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Fixer returns no data for date (very old/weekend) │ Fixer returns success:false or empty rates → RatesNotFoundException → 404 │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Network timeout to fixer.io │ Wrapped in FixerApiException → 502 │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ Concurrent requests for same uncached date │ DB unique constraint → catch DataIntegrityViolationException → re-read from DB │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ DB unavailable │ Let Spring propagate → 500 via catch-all handler │
├───────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
│ USD missing from fixer rates (shouldn't happen, but) │ Throw FixerApiException with clear message │
└───────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────┘

---

Files to Create / Modify

Modify (2 files)

- pom.xml — add spring-boot-starter-data-jpa
- src/main/resources/application.properties — add JPA config, fix fixer key property name
- src/main/java/.../ExchangeRatesController.java — wire service,nse

Create (12 files)

- config/AppConfig.java
- entity/ExchangeRateSnapshot.java
- converter/RatesMapConverter.java
- repository/ExchangeRateSnapshotRepository.java
- client/FixerClient.java
- client/dto/FixerRatesResponse.java
- client/dto/FixerErrorResponse.java
- service/ExchangeRatesService.java
- dto/RatesResponse.java
- dto/ErrorResponse.java
- exception/ExchangeRateException.java (+ subclasses)
- exception/GlobalExceptionHandler.java

---

Verification

docker-compose up -d database
./mvnw spring-boot:run

# Happy path

curl http://localhost:8080/api/v1/rates/2022-06-20

# Invalid date format

curl http://localhost:8080/api/v1/rates/not-a-date # expec

# Future date

curl http://localhost:8080/api/v1/rates/2099-01-01 # expect 400

# Second call same date — should return from cache (check logs, no fixer call)

curl http://localhost:8080/api/v1/rates/2022-06-20

# Run provided test script

bash test.sh
