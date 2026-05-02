Plan: Add request-scoped logging with traceId propagation

Context

The service currently has zero logging — no SLF4J usage anywhere in src/main/java, no logback-spring.xml, no MDC. When something goes wrong (a fixer.io 5xx, a cache-miss path, a malformed date) it is silently mapped to an ErrorResponse in GlobalExceptionHandler.java:39-43 with no diagnostic trail.

Goal: introduce a traceId per inbound request, attach it to MDC for the entire request thread, propagate it on the outbound fixer.io call, surface it on the response header, and add structured INFO/WARN/ERROR logs at each layer (controller → service → provider → exception handler) so the full flow of a single request can be reconstructed from logs.

This is a Spring Boot 2.7 servlet app (not WebFlux), single-threaded per request — MDC propagates naturally through the call chain (including the synchronous Resilience4j @Retry), so we do not need Sleuth, Micrometer Tracing, or any task-decorator gymnastics.

Approach

1.  Inbound trace ID filter — OncePerRequestFilter that:

- Reads the X-Trace-Id request header; if missing/blank, generates a new short ID (UUID.randomUUID().toString().replace("-", "").substring(0, 16) — 16-char hex, compact for log lines, sufficiently unique for our scale).
- Puts it on MDC under key traceId.
- Sets the same value on the response header X-Trace-Id so callers can correlate.
- Clears MDC in a finally block (critical to avoid leaks across pooled Tomcat threads).
- Registration: declared as a @Bean FilterRegistrationBean<TraceIdFilter> in AppConfig.java with setOrder(Ordered.HIGHEST_PRECEDENCE). (Not @Component + @Order — that path is auto-wired by Spring Boot but the order hint is not always honoured for plain Filter beans, and "MDC must be set before any other filter" is load-bearing.)

2.  Logback pattern — add src/main/resources/logback-spring.xml that extends Spring Boot's default console appender to include [%X{traceId:-}] between the thread and logger fields. Lines without a traceId (startup, scheduled tasks) just render [] — no NPE, no missing-key noise.

3.  Outbound propagation — register a ClientHttpRequestInterceptor on the RestTemplate bean in AppConfig.java that copies MDC.get("traceId") onto the outbound request as X-Trace-Id. fixer.io ignores it, but if we ever swapproviders or proxy through an internal gateway the header is already there.

4.  Loggers in each layer — one private static final Logger per class via LoggerFactory.getLogger(...):

- ExchangeRatesController: INFO at entry ("Received request for date={}"), no exit log (the response status is logged by the access pattern naturally and we don't want duplication).
- ExchangeRatesService: DEBUG for cache hit, INFO for cache miss ("Cache miss for date={}, fetching from provider"), INFO after successful save ("Cached rates for date={}"), WARN on the concurrent-write race path("Concurrent write conflict for date={}, re-reading").
- FixerExchangeRateProvider: INFO before each call ("Calling fixer.io for date={}"), WARN on transient failures (so retry attempts are visible — important since @Retry will swallow them otherwise), ERROR on terminal failures with the exception, INFO on success with response.timestamp.
- GlobalExceptionHandler: WARN for 4xx client errors (InvalidDateException, type mismatch, RatesNotFoundException), ERROR with stack trace for ProviderException and the generic Exception fallback. This is where today's silentfailures become visible.

5.  Response header — emitting X-Trace-Id on every response (handled by the filter in step 1) makes it trivial for a caller to grep server logs given a failing request.

6.  Resilience4j retry interaction — @Retry invokes the method synchronously on the same thread, so MDC is preserved across attempts. Each attempt's WARN log will carry the same traceId, which is exactly what we want — multiple log lines for the same logical request all share an ID. No ContextAwareScheduledThreadPoolExecutor or MDC-copyingdecorator needed.

Files to modify

New:

- src/main/java/com/shipmonk/testingday/shared/TraceIdFilter.java — the OncePerRequestFilter component (steps 1).
- src/main/resources/logback-spring.xml — console pattern with %X{traceId:-} (step 2). Use <include resource="org/springframework/boot/logging/logback/defaults.xml"/> and <includeresource="org/springframework/boot/logging/logback/console-appender.xml"/> and override CONSOLE_LOG_PATTERN so we keep Spring Boot's defaults (color, level, etc.) and only inject the MDC field.

Modify:

- src/main/java/com/shipmonk/testingday/shared/AppConfig.java — (a) add @Bean FilterRegistrationBean<TraceIdFilter> with HIGHEST_PRECEDENCE order (step 1 registration), (b) add the outbound ClientHttpRequestInterceptor to theRestTemplate builder at lines 13–19 via .additionalInterceptors(...) (step 3).
- src/main/java/com/shipmonk/testingday/rates/controller/ExchangeRatesController.java:25-36 — add logger, INFO atentry (step 4).
- src/main/java/com/shipmonk/testingday/rates/service/ExchangeRatesService.java:27-58 — add logger, cachehit/miss/save/conflict logs (step 4).
- src/main/java/com/shipmonk/testingday/rates/provider/fixer/FixerExchangeRateProvider.java:44-93 — add logger,call/success/failure logs in fetchRates and callFixer (step 4).
- src/main/java/com/shipmonk/testingday/shared/GlobalExceptionHandler.java:15-43 — add logger, log every handledexception before mapping (step 4). This is the highest-value change for debuggability.
- src/main/resources/application.properties — add logging.level.com.shipmonk=INFO (explicit, so we don'taccidentally rely on the root default).

Tests:

- src/test/java/com/shipmonk/testingday/ExchangeRatesIT.java:
    - Add MockMvcResultMatchers.header().exists("X-Trace-Id") on one happy-path test (proves the filter is wired into the servlet chain and emits the response header).
    - Add header("X-Trace-Id", Matchers.notNullValue()) to one MockRestServiceServer expectation to assert the outbound fixer.io call carries the propagated trace header. Note: MockRestServiceServer.bindTo(restTemplate) swapsthe request factory but preserves the RestTemplate's registered interceptors via InterceptingClientHttpRequestFactory, so the interceptor should fire — if Spring's mock layer drops it in thisversion, fall back to trusting the unit-level wiring and document why the assertion was removed.
    - No need to assert log content (would require OutputCaptureExtension); the filter and logger calls are simpleand the chain is exercised end-to-end above.
- No changes required to ExchangeRatesControllerTest (@WebMvcTest slice does not load @Bean-registered filters from AppConfig, so the controller logger fires but MDC is empty — acceptable, the pattern just renders []).
- FixerExchangeRateProviderTest constructs its own RestTemplate bypassing AppConfig, so the outbound interceptorisn't tested there — the integration test covers wiring end-to-end.

Existing utilities to reuse

- org.slf4j.MDC — standard SLF4J, already on classpath via spring-boot-starter-logging. No new dependency.
- org.springframework.web.filter.OncePerRequestFilter — already on classpath via spring-boot-starter-web.
- org.springframework.http.client.ClientHttpRequestInterceptor — same.
- RestTemplateBuilder.additionalInterceptors(...) — already used to set timeouts at AppConfig.java:14-18; justchain .additionalInterceptors(traceIdInterceptor) before .build().
- Spring Boot's bundled Logback defaults (org/springframework/boot/logging/logback/defaults.xml,console-appender.xml) — include them in our logback-spring.xml so we inherit colour/level/timezone formatting and only override the pattern.

No new dependencies required

Everything above uses what's already in pom.xml (Spring Boot 2.7 starters bring SLF4J + Logback + servlet API). Wedeliberately avoid spring-cloud-sleuth (heavy, EOL on the SB3 line) and micrometer-tracing (SB3-only) — a hand-rolled filter is the right size for this scope.

Verification

1.  Unit tests: ./mvnw test — should pass unchanged (no behaviour change to controller/service/provider/converter).
2.  Integration test: ./mvnw verify — the new X-Trace-Id header assertion in ExchangeRatesIT validates the filter is wired into the servlet chain and emits the response header.
3.  Manual smoke test:

- docker-compose up -d database
- ./mvnw spring-boot:run
- In another terminal: bash test.sh
- Console logs should show every log line prefixed with a 16-char traceId, the same ID across controller/service/provider lines for one request, a different ID for the next request.
- curl -i http://localhost:8080/api/v1/rates/2024-01-15 — verify X-Trace-Id header is on the response.
- curl -i -H "X-Trace-Id: my-correlation-id" http://localhost:8080/api/v1/rates/2024-01-15 — verify the responseechoes the same my-correlation-id and the server logs use it (caller-supplied ID is honoured).
- curl -i http://localhost:8080/api/v1/rates/not-a-date — verify GlobalExceptionHandler now logs theInvalidDateException at WARN with the traceId, and the response still carries X-Trace-Id.

4.  Update graphify after edits land: graphify update .
