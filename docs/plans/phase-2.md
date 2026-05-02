Decouple rates source behind a provider interface, then add resilience4j retry

Context

Two problems, one refactor:

1.  Tight coupling to fixer.io. ExchangeRatesService directly imports FixerClient and FixerRatesResponse, reads fixer-specific fields (getRates(), getTimestamp()), runs fixer-specific math (rebaseToUsd divides EUR-based rates by the USD rate because the free tier is EUR-based), and throws FixerApiException. GlobalExceptionHandler even surfaces fixerCode in the public error body. Swapping providers today would require touching the controller, service, exception handler, and tests.
2.  No retry on transient failures. Two quick GET /api/v1/rates/{day} requests trip fixer's free-tier per-second rate limit (HTTP 429 / fixer code 106 rate_limit_reached). Our generic catch (RestClientException) flattens that into FixerApiException and we return HTTP 502 with no retry.

Solution: introduce an ExchangeRateProvider interface + provider-agnostic ExchangeRates value object + provider-agnostic exception hierarchy. FixerExchangeRateProvider becomes one implementation that owns the fixer DTOs, the USD rebasing math, and the resilience4j @Retry config. Adding a future provider becomes "implement the interface, add a config flag."

The retry mechanism is unchanged in spirit from the previous plan: a marker subclass TransientProviderException is thrown only for retryable failures (HTTP 429, 5xx, IO/timeout). resilience4j is configured to retry exclusively on that type.

Target architecture

controller/ -> ExchangeRatesController (depends on service, no provider knowledge)
service/ -> ExchangeRatesService (depends on ExchangeRateProvider interface only)
provider/ -> ExchangeRateProvider (interface), ExchangeRates (value object)
provider/fixer/ -> FixerExchangeRateProvider, FixerRatesResponse, FixerErrorDetail
exception/ -> ExchangeRateException, ProviderException, TransientProviderException,
RatesNotFoundException, InvalidDateException (FixerApiException deleted)
entity/, repository/, dto/ (unchanged — already provider-agnostic)

Implementation

1.  New domain model — provider/ExchangeRates.java (new)

Immutable value object the service operates on. No provider specifics.

Fields: LocalDate date, String base (guaranteed "USD" by the interface contract), long timestamp, Map<String, BigDecimal> rates (USD-based). Constructor + getters only.

2.  New interface — provider/ExchangeRateProvider.java (new)

public interface ExchangeRateProvider {
/\*\*
_ Fetch rates for the given date with USD as the base currency.
_ Implementations are responsible for any provider-specific rebasing. \*
_ @throws RatesNotFoundException when the provider has no rates for that date
_ @throws TransientProviderException for retryable failures (rate limit, 5xx, IO)
_ @throws ProviderException for permanent provider failures (auth, quota, malformed response)
_/
ExchangeRates fetchRates(LocalDate date);
}

Single method, contract-documented exceptions. The interface promises USD-based output — no caller ever needs to knowfixer.io returns EUR.

3.  New exception hierarchy — exception/

Replace FixerApiException with provider-agnostic types. All extend the existing ExchangeRateException parent.

- ProviderException extends ExchangeRateException (new) — base for all provider failures. Constructor (String, Throwable). Maps to HTTP 502.
- TransientProviderException extends ProviderException (new) — retryable. Constructor (String, Throwable). Used by resilience4j retry-exceptions.
- RatesNotFoundException (existing) — unchanged, still maps to 404.
- InvalidDateException (existing) — unchanged, still maps to 400.
- FixerApiException (existing) — deleted. All call sites move to ProviderException / TransientProviderException.

The fixerCode field is dropped. Today the GlobalExceptionHandler surfaces it as error.code in the response body (e.g. error.code = 104 when fixer's monthly quota is exceeded). After the refactor, the body always shows error.code = 502 forprovider failures. This is a deliberate, small public-API change — provider-specific codes don't belong in our contract anyway. Existing IT cases that assert on body code 104 / 502 will be updated accordingly (see step 9).

4.  New fixer implementation — provider/fixer/FixerExchangeRateProvider.java (new, replaces client/FixerClient.java)

Move the existing FixerClient here, rename to FixerExchangeRateProvider, implement ExchangeRateProvider. Also moved into this package: FixerRatesResponse and FixerErrorDetail (from client/dto/ → provider/fixer/dto/). These DTOs become package-private to underscore that they don't escape the provider boundary.

Responsibilities the implementation now owns:

- HTTP call via RestTemplate (same as today).
- USD rebasing logic moved here from ExchangeRatesService (free tier returns EUR base — provider-specific concern). Same math, same BASE_CURRENCY = "USD" constant, same "USD rate missing" error → throw ProviderException.
- Translate RestClientException subclasses into the new exception types:
  a. HttpClientErrorException — if getStatusCode() == HttpStatus.TOO_MANY_REQUESTS → TransientProviderException. Other 4xx →ProviderException.
  b. HttpServerErrorException (5xx) → TransientProviderException.
  c. ResourceAccessException (timeouts, IO) → TransientProviderException.
  d. Fallback RestClientException (malformed JSON, etc.) → ProviderException.
- Translate fixer body errors (success=false):
    - code 106 → RatesNotFoundException (unchanged).
    - any other code → ProviderException (no fixer code leaked into the message).
- Annotate fetchRates(LocalDate) with @Retry(name = "fixer").
- Construct and return ExchangeRates (not FixerRatesResponse) to callers.

AOP proxy works because FixerExchangeRateProvider is a @Component and is invoked from a different bean (ExchangeRatesService).

5.  Refactor service/ExchangeRatesService.java

- Inject ExchangeRateProvider instead of FixerClient.
- Delete the rebaseToUsd method and BASE_CURRENCY constant — both now live in the fixer provider. (Service does still keep the "USD" literal when constructing ExchangeRateSnapshot from the ExchangeRates value object; it can copy it fromrates.getBase() to stay loosely coupled.)
- fetchAndCache(LocalDate) body becomes:
  a. ExchangeRates rates = provider.fetchRates(date);
  b. Build ExchangeRateSnapshot from rates.getDate(), rates.getBase(), rates.getTimestamp(), rates.getRates(),LocalDateTime.now().
  c. Save / handle DataIntegrityViolationException exactly as today.
- Remove import com.shipmonk.testingday.client.FixerClient, FixerRatesResponse, FixerApiException. Replace concurrent-write fallback's FixerApiException with ProviderException.

6.  Refactor exception/GlobalExceptionHandler.java

Replace the @ExceptionHandler(FixerApiException.class) handler with @ExceptionHandler(ProviderException.class) returning HTTP 502 with body {success:false, error:{code:502, info: ex.getMessage()}}. TransientProviderException is caught by the same handler since it extends ProviderException (matters only when retries are exhausted — handled identically). TheRatesNotFoundException → 404 and InvalidDateException → 400 handlers stay unchanged.

7.  Add Maven dependencies — pom.xml

 <dependency>
     <groupId>io.github.resilience4j</groupId>
     <artifactId>resilience4j-spring-boot2</artifactId>
     <version>1.7.1</version>
 </dependency>
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-aop</artifactId>
 </dependency>

1.7.1 is the last 1.x release and the version aligned with Spring Boot 2.7. Do NOT use 2.x (requires Boot 3 / Jakarta). spring-boot-starter-aop is required for the @Retry AOP proxy. No actuator needed (we're not exposing retry metrics).

8.  Configure resilience4j

src/main/resources/application.properties — append:

resilience4j.retry.instances.fixer.max-attempts=3
resilience4j.retry.instances.fixer.wait-duration=1s
resilience4j.retry.instances.fixer.enable-exponential-backoff=true
resilience4j.retry.instances.fixer.exponential-backoff-multiplier=2
resilience4j.retry.instances.fixer.retry-exceptions=com.shipmonk.testingday.exception.TransientProviderException

Behavior: 3 total attempts (1 initial + 2 retries) with ~1s, ~2s waits — worst case ~3s extra latency before 502. Comfortably within fixer's 1-minute rate-limit window so the retry has a real chance of succeeding.

src/test/resources/application.properties — append the same keys, but wait-duration=1ms and enable-exponential-backoff=false so the test suite stays fast while still exercising real retry paths.

9.  Tests

Renames / moves:

- FixerClientTest.java → FixerExchangeRateProviderTest.java. Same class-instantiation pattern (new FixerExchangeRateProvider(...)), so @Retry does NOT fire here (no Spring proxy) — a feature, not a bug: this file tests theprovider's translation logic in isolation.

Updates needed in FixerExchangeRateProviderTest:

- serverError\_\* test now expects TransientProviderException (still passes any ProviderException.class assertion viainheritance). Update .hasMessageContaining(...) to match the new "fixer.io server error" message.
- fixerError104\_\* test now expects ProviderException (no longer FixerApiException, no longer carries fixerCode = 104). Bodycode is no longer asserted at this layer.
- fixerError106\_\* test unchanged — still expects RatesNotFoundException.
- The provider now does USD rebasing internally, so the happy-path test must assert the rebased map is returned in the ExchangeRates object (whereas today the rebasing is asserted in ExchangeRatesServiceTest). Move/duplicate the rebasingassertions into this file.
- Add new direct unit tests:
    - httpClientError429_throwsTransientProviderException — andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)).
    - httpClientError400_throwsPermanentProviderException — assert thrown is ProviderException but notTransientProviderException.

ExchangeRatesServiceTest.java:

- Replace Mockito mock of FixerClient with mock of ExchangeRateProvider. Stub it to return ExchangeRates directly(already-rebased) — no fixer DTOs in this test anymore.
- Delete the rebasing-math assertions (logic moved to the provider).
- Keep cache hit/miss, persistence, concurrent-write tests — they're orthogonal to the provider change.
- Replace FixerApiException references with ProviderException.

ExchangeRatesControllerTest.java — @WebMvcTest + mocked service. Update any test that throws FixerApiException from themocked service to throw ProviderException instead. Body assertions that expected error.code = <fixer code> change to expect error.code = 502.

ExchangeRatesIT.java — full @SpringBootTest, AOP proxy active, retry fires:

- Update fixerServerError_returns502_withGenericCode: change the single mockServer.expect(...) to mockServer.expect(ExpectedCount.times(3), requestTo(...)) because retry now fires 3 attempts.
- Update fixerError104_returns502_withCode104: rename to drop "withCode104"; assert body error.code = 502 (not 104). Single expectation — code 104 isn't transient, no retry.
- Other 106 / success / cache-hit cases keep their single expectation.

New IT cases to add:

- transient429ThenSuccess_retriesAndReturns200 — register two ordered expectations: first withStatus(TOO_MANY_REQUESTS),second withSuccess(validBody, APPLICATION_JSON). Expect 200, snapshot persisted, mockServer.verify().
- transient429OnEveryAttempt_exhaustsRetriesAndReturns502 — expect(ExpectedCount.times(3),...).andRespond(withStatus(TOO_MANY_REQUESTS)). Expect 502, repository empty, mockServer.verify().
- permanent400_doesNotRetry_returns502 — single expect(...) responding withStatus(BAD_REQUEST). Expect 502.mockServer.verify() will fail if retry incorrectly fires — this locks in "no retry on permanent 4xx".

Required new imports in IT: org.springframework.test.web.client.ExpectedCount, org.springframework.http.HttpStatus, org.springframework.test.web.client.response.MockRestResponseCreators.withStatus.

Critical files to create / modify / delete

New:

- src/main/java/com/shipmonk/testingday/provider/ExchangeRateProvider.java
- src/main/java/com/shipmonk/testingday/provider/ExchangeRates.java
- src/main/java/com/shipmonk/testingday/provider/fixer/FixerExchangeRateProvider.java (replaces FixerClient)
- src/main/java/com/shipmonk/testingday/provider/fixer/dto/FixerRatesResponse.java (moved)
- src/main/java/com/shipmonk/testingday/provider/fixer/dto/FixerErrorDetail.java (moved)
- src/main/java/com/shipmonk/testingday/exception/ProviderException.java
- src/main/java/com/shipmonk/testingday/exception/TransientProviderException.java

Modified:

- pom.xml — resilience4j + AOP starter
- src/main/java/com/shipmonk/testingday/service/ExchangeRatesService.java — depend on interface, drop rebasing
- src/main/java/com/shipmonk/testingday/exception/GlobalExceptionHandler.java — handle ProviderException instead ofFixerApiException
- src/main/resources/application.properties — resilience4j config
- src/test/resources/application.properties — resilience4j test config
- src/test/java/com/shipmonk/testingday/ExchangeRatesIT.java — ExpectedCount.times(3) on transient cases, body codeexpectations, three new tests
- src/test/java/com/shipmonk/testingday/ExchangeRatesServiceTest.java — mock ExchangeRateProvider, drop rebasing assertions
- src/test/java/com/shipmonk/testingday/ExchangeRatesControllerTest.java — exception type + body code updates
- src/test/java/com/shipmonk/testingday/FixerClientTest.java — rename to FixerExchangeRateProviderTest, exception updates,add rebasing assertions, add 429 + 400 cases

Deleted:

- src/main/java/com/shipmonk/testingday/client/FixerClient.java
- src/main/java/com/shipmonk/testingday/client/dto/FixerRatesResponse.java
- src/main/java/com/shipmonk/testingday/client/dto/FixerErrorDetail.java
- src/main/java/com/shipmonk/testingday/exception/FixerApiException.java
- (and the now-empty client/ package)

Verification

1.  ./mvnw test — full unit suite passes.
2.  ./mvnw verify — Failsafe IT passes including the three new retry cases.
3.  End-to-end reproduction of the original 502:

- docker-compose up -d database
- ./mvnw spring-boot:run
- bash test.sh — multiple quick requests. Before: second/third returns 502 from rate-limit. After: app log shows Retry 'fixer' events; requests succeed once retry slips past the throttle.

4.  Optional: add logging.level.io.github.resilience4j=DEBUG temporarily to confirm retry attempts.
5.  ./mvnw dependency:tree | grep resilience4j — verify resilience4j-spring-boot2:1.7.1 resolved alongside resilience4j-retryand resilience4j-aop.
6.  Future-proof check: pretend to add a second provider — create a stub class FakeExchangeRateProvider implementsExchangeRateProvider. The compiler should be happy and the only thing needed to swap is making one provider @Primary or wiring a @ConditionalOnProperty. No service/controller/test changes required. (You don't have to actually add this — it's asanity check on the design.)

Risks & notes

- Public-API behavior change: error response body code goes from "sometimes the fixer code (104, etc.)" to "always 502 for provider failures". This is intentional — we're hiding fixer-specific codes from API consumers — but it's worth flagging incommit message.
- MockRestServiceServer is order-sensitive: 429-then-success test must register expectations in call order.
- Default RestTemplate error handler (DefaultResponseErrorHandler) throws HttpClientErrorException for 4xx, HttpServerErrorException for 5xx — AppConfig doesn't customize this, so the catch-block strategy holds.
- Cache stampede: two concurrent cache misses still produce two outbound calls (now potentially 6 with retry). Out of scope; existing concurrent-write handling in ExchangeRatesService already deduplicates the persist side.
- Retry log noise: every retry attempt logs WARN. Desirable for now (rate-limit visibility).
- Single provider today: Spring autowires ExchangeRateProvider to the only @Component implementation. When a second provideris added later, use @Primary or @ConditionalOnProperty(name="exchange-rates.provider", havingValue="fixer") to disambiguate. No need to add config plumbing now.
