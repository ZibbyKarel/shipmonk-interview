Plan: Add a Resilience4j circuit breaker around the fixer.io provider call

Context

`FixerExchangeRateProvider.fetchRates` is the only outbound network dependency in the service and already carries `@Retry(name = "fixer")` (3 attempts, exponential backoff 1s/2s, only on `TransientProviderException`). The service is otherwise fully cache-aside in `ExchangeRatesService.getRatesForDay` — historical exchange rates are immutable, so once a date is cached, the provider is never called again for that date.

Today, when fixer.io is down or persistently rate-limited, every cache-miss request burns ~3s (1s + 2s backoff between three attempts) of a Tomcat worker thread before failing. Under any reasonable burst against an uncached date, that translates into thread-pool starvation and queued requests — the cache cannot help us during the very window where the breaker would.

Goal: wrap the outbound call in a circuit breaker so that after a configurable failure rate against fixer.io, subsequent cache-miss requests fail fast (`503` with a `Retry-After` hint) instead of stacking up on the retry budget. Cache hits remain unaffected — they never enter the breaker.

`resilience4j-spring-boot2` is already on the classpath, so this is configuration + one annotation + one extra exception mapper, not a new dependency.

Approach

1.  **Annotation placement** — add `@CircuitBreaker(name = "fixer")` to `FixerExchangeRateProvider.fetchRates(LocalDate)`, *alongside* the existing `@Retry`. The breaker must wrap the retry, not be wrapped by it: a single logical fixer.io call (= one retry cycle of up to 3 HTTP attempts) should count as **one** outcome from the breaker's perspective. If the breaker were inner, every individual HTTP attempt would be tallied separately and 3 attempts on one bad request would fully open a small breaker.

    Resilience4j enforces ordering via Spring's `Ordered` semantics — the lower the order number, the higher the precedence, meaning the lower-numbered aspect is the *outermost* one in the proxy chain. Default order in `resilience4j-spring-boot2` puts RetryAspect outer and CircuitBreakerAspect inner — the opposite of what we want. We invert it:

    ```
    resilience4j.circuitbreaker.circuit-breaker-aspect-order=1
    resilience4j.retry.retry-aspect-order=2
    ```

    Resulting call chain: CircuitBreaker(outer) → Retry(inner) → `fetchRates` body. One logical fixer.io call = one retry cycle = one outcome registered with the breaker. Document this inversion in `application.properties` with a brief comment — it is the single most error-prone knob in this whole plan.

2.  **Breaker configuration** — count-based sliding window, conservative thresholds tuned for low-volume interview traffic:

    ```
    resilience4j.circuitbreaker.instances.fixer.sliding-window-type=COUNT_BASED
    resilience4j.circuitbreaker.instances.fixer.sliding-window-size=10
    resilience4j.circuitbreaker.instances.fixer.minimum-number-of-calls=5
    resilience4j.circuitbreaker.instances.fixer.failure-rate-threshold=50
    resilience4j.circuitbreaker.instances.fixer.wait-duration-in-open-state=30s
    resilience4j.circuitbreaker.instances.fixer.permitted-number-of-calls-in-half-open-state=2
    resilience4j.circuitbreaker.instances.fixer.automatic-transition-from-open-to-half-open-enabled=true
    resilience4j.circuitbreaker.instances.fixer.record-exceptions=com.shipmonk.testingday.modules.rates.exception.ProviderException
    resilience4j.circuitbreaker.instances.fixer.ignore-exceptions=com.shipmonk.testingday.modules.rates.exception.RatesNotFoundException,com.shipmonk.testingday.modules.rates.exception.InvalidDateException
    ```

    `record-exceptions=ProviderException` covers `TransientProviderException` too (subclass), which is exactly what we want — both transient (5xx, 429, network) and permanent provider errors (non-success body, malformed quota errors) count toward the failure rate. `ignore-exceptions` explicitly excludes business cases:
    - `RatesNotFoundException` (fixer code 106 — "no rates for date") is a legitimate `404`, not a fixer outage. Counting it as failure would open the breaker after a few requests for ancient dates.
    - `InvalidDateException` cannot reach the provider (validated in service), but listed defensively for symmetry.

    `minimum-number-of-calls=5` prevents the breaker from tripping on the very first failing call after startup. `wait-duration=30s` is short enough that a transient outage recovers before users notice a sustained 503 storm; `automatic-transition-from-open-to-half-open-enabled` removes the need for a probe call to flip the state.

3.  **Exception mapping** — when the breaker is OPEN, calls throw `io.github.resilience4j.circuitbreaker.CallNotPermittedException`. We add one `@ExceptionHandler` to `GlobalExceptionHandler`:

    ```java
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitOpen(CallNotPermittedException ex) {
      log.warn("Circuit breaker OPEN — fixer.io call rejected: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .header(HttpHeaders.RETRY_AFTER, "30")
          .body(new ErrorResponse(503, "Upstream rate provider temporarily unavailable"));
    }
    ```

    `503 + Retry-After: 30` is the correct semantic (we *will* retry the upstream in ~30s once the breaker half-opens) and pairs better with consumer-side back-off than reusing the existing `502` for `ProviderException`. Note the `Retry-After` value mirrors `wait-duration-in-open-state` — keep them consistent if the duration is ever tuned.

    Do **not** add a `@Recover` / fallback method that returns stale data: the assignment never expires cache entries, so any cached date already short-circuits before reaching the provider; for an *uncached* date we have no rates to return, and silently fabricating a 200 with empty rates would violate the contract. Fail fast is the correct behaviour.

4.  **Observability** — expose breaker state through Spring Boot Actuator. Add the actuator dependency and turn on the relevant endpoints:

    ```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    ```

    ```
    management.endpoints.web.exposure.include=health,circuitbreakers,circuitbreakerevents
    management.endpoint.health.show-details=always
    management.health.circuitbreakers.enabled=true
    resilience4j.circuitbreaker.instances.fixer.register-health-indicator=true
    ```

    This gives `GET /actuator/circuitbreakers` (current state per instance) and `GET /actuator/circuitbreakerevents/fixer` (a ring buffer of recent transitions and failures, invaluable for diagnosing "why is the service returning 503"). The health indicator surfaces `OPEN` as `DOWN` on `/actuator/health`, which lets a load balancer or k8s readiness probe drain the instance until the breaker half-opens — but only if a downstream consumer wires that up. For this assignment it is enough that the data is exposed.

5.  **Resilience4j instance name reuse** — the existing `@Retry(name = "fixer")` and the new `@CircuitBreaker(name = "fixer")` deliberately share the same instance name. Resilience4j keeps the two registries separate, so reusing the name does not conflict; it just makes the actuator output and metric tags line up under one logical dependency ("fixer").

Files to modify

New: none. (Everything is anchored on existing classes.)

Modify:

- `pom.xml` — add `spring-boot-starter-actuator` dependency. No version, inherited from the BOM.
- `src/main/resources/application.properties` — add the eight `resilience4j.circuitbreaker.instances.fixer.*` properties from §2, the two `circuit-breaker-aspect-order` / `retry-aspect-order` properties from §1 (with a comment explaining the inversion), and the four `management.*` properties from §4.
- `src/main/java/com/shipmonk/testingday/modules/rates/provider/fixer/FixerExchangeRateProvider.java` — add `@CircuitBreaker(name = "fixer")` immediately above the existing `@Retry(name = "fixer")` on `fetchRates(LocalDate)`. Import `io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker`.
- `src/main/java/com/shipmonk/testingday/web/GlobalExceptionHandler.java` — add the `CallNotPermittedException` handler from §3. Import `io.github.resilience4j.circuitbreaker.CallNotPermittedException` and `org.springframework.http.HttpHeaders`.

Tests:

- `src/test/java/com/shipmonk/testingday/modules/rates/provider/fixer/FixerExchangeRateProviderTest.java` — already constructs the provider directly with `new RestTemplate()` and no Spring context, so the `@CircuitBreaker` aspect does not engage. Existing assertions therefore remain valid (they test pure behaviour). No changes needed; document this in a one-line comment near the `@BeforeEach` so a future reader does not assume the breaker is being exercised here.

- `src/test/java/com/shipmonk/testingday/ExchangeRatesIT.java` — add a `CircuitBreakerRegistry` field, reset the breaker in `@BeforeEach` so prior tests do not leak state, and add **one** new test that proves the breaker-rejected path produces a `503` (not `502`):

    ```java
    @Test
    void breakerOpen_uncachedDate_returns503_withRetryAfter() {
      circuitBreakerRegistry.circuitBreaker("fixer").transitionToOpenState();
      // No fixer expectation — any outbound call would fail mockServer.verify().
      mockMvc.perform(get("/api/v1/rates/2024-03-04"))
          .andExpect(status().isServiceUnavailable())
          .andExpect(header().string("Retry-After", "30"))
          .andExpect(jsonPath("$.error.code").value(503));
    }
    ```

    Forcing the breaker open via the registry rather than driving N failures keeps the assertion focused on the `CallNotPermittedException` → `503` mapping. The exact threshold-trip semantics (sliding window, minimum calls, aspect ordering) are configuration concerns better verified manually against a real outage — not via brittle counting tests that depend on subtle interactions between Retry and CircuitBreaker aspect ordering.

    **Breaker state isolation.** The breaker is a singleton bean within the Spring test context. The new `@BeforeEach` resets it via `circuitBreakerRegistry.circuitBreaker("fixer").reset()`, so a manually-opened breaker in one test does not bleed into the next.

- One additional IT, `breakerOpen_doesNotBlockCachedDates`, complements the rejection test: it caches a date with a single fixer round-trip, then forces the breaker OPEN, then re-requests the same date and asserts a `200` is served from cache without touching the breaker. This is the load-bearing claim of the whole feature — that breaker-induced failure-fast behaviour does not degrade cache-hit traffic.

- One new unit-style test in `FixerExchangeRateProviderTest` would require manual `CircuitBreaker` wiring (the annotation does nothing without the Spring AOP proxy), which adds significant setup cost for marginal value. Skip it; the ITs cover the wired behaviour.

Existing utilities to reuse

- `io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker` — annotation, identical wiring style to the existing `@Retry`.
- `io.github.resilience4j.circuitbreaker.CallNotPermittedException` — thrown by the proxy when the breaker rejects; mapped centrally in `GlobalExceptionHandler` exactly like `ProviderException`, `RatesNotFoundException`, etc.
- `io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry` — autowired in the IT test for `reset()` between runs. Already exposed as a bean by `resilience4j-spring-boot2`.
- Existing `ProviderException` / `TransientProviderException` / `RatesNotFoundException` hierarchy — used directly in `record-exceptions` / `ignore-exceptions` config. No new exception types needed.
- `spring-boot-starter-actuator` — only new module dependency, version managed by the Spring Boot BOM.

No code-level abstraction is introduced. The breaker is purely declarative.

Verification

1.  Unit tests: `./mvnw test` — must pass with no changes (provider unit tests construct the provider without Spring AOP, breaker is invisible).
2.  Integration tests: `./mvnw verify` — the new `breakerOpens_*` test plus all existing IT tests must pass. Pay attention to test order: the new test resets the breaker in its own `@AfterEach`, but verify locally by reordering or running the full class twice (`./mvnw verify -Dit.test=ExchangeRatesIT -DfailIfNoTests=false`).
3.  Manual smoke test:

    - `docker-compose up -d database`
    - `./mvnw spring-boot:run`
    - Verify breaker is exposed: `curl -s http://localhost:8080/actuator/circuitbreakers | jq` → `state: CLOSED`.
    - Force a real outage by setting an obviously bad `FIXER_API_KEY=invalid` and querying ~5 distinct uncached dates: `for d in 2024-01-{01..06}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/rates/$d; done`. Expect a mix of `502` early (provider failures pass through) then `503` once the breaker opens.
    - `curl -s http://localhost:8080/actuator/circuitbreakers | jq` → `state: OPEN`.
    - `curl -i http://localhost:8080/api/v1/rates/2024-02-01` → `HTTP/1.1 503` with `Retry-After: 30`.
    - Wait 30s, then re-query → breaker enters HALF_OPEN, a probe call goes out, and on success the state returns to CLOSED.
    - `curl -s http://localhost:8080/actuator/circuitbreakerevents/fixer | jq '.circuitBreakerEvents[-5:]'` → recent transitions visible.

4.  Watch the logs: every transition (`CLOSED → OPEN`, `OPEN → HALF_OPEN`, `HALF_OPEN → CLOSED`) is logged at INFO by Resilience4j by default; the new `WARN` line in the `CallNotPermittedException` handler shows up once per rejected request.

5.  Update the graphify graph after edits land: `graphify update .`

Out of scope (consciously deferred)

- **Per-error-code breaker tuning.** Treating fixer code 104 (quota exhausted) the same as a 5xx is a slight oversimplification — quota is recovered next billing period, not in 30s. A future iteration could split into two breakers or use a separate exception ignored from the breaker but still surfaced as `502`. Not worth the complexity for this assignment.
- **Bulkhead** (`@Bulkhead`) and **time limiter** (`@TimeLimiter`). The existing `RestTemplate` connect/read timeouts (5s/10s) already bound the per-call latency; bulkhead would matter under genuinely concurrent load, which this service does not see. Listed here so a future reader knows it was considered, not forgotten.
- **Metrics export** (Micrometer + Prometheus). Resilience4j auto-registers metrics if Micrometer is on the classpath, but we do not have a Prometheus scrape target wired up; adding one belongs to a future observability phase, not this one.
