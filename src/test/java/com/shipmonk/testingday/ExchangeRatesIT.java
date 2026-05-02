package com.shipmonk.testingday;

import com.shipmonk.testingday.rates.entity.ExchangeRateSnapshot;
import com.shipmonk.testingday.rates.repository.ExchangeRateSnapshotRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExchangeRatesIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExchangeRateSnapshotRepository repository;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    void cleanup() {
        repository.deleteAll();
        mockServer.reset();
    }

    @Test
    void cacheMiss_fetchesFromFixer_persistsRebasedRatesToDb_returns200() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        expectFixerCall(date, """
                {
                  "success": true,
                  "historical": true,
                  "date": "2022-06-20",
                  "timestamp": 1655769599,
                  "base": "EUR",
                  "rates": {
                    "USD": 1.05,
                    "GBP": 0.85,
                    "JPY": 140.0
                  }
                }
                """);

        mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.historical").value(true))
                .andExpect(jsonPath("$.date").value("2022-06-20"))
                .andExpect(jsonPath("$.timestamp").value(1655769599L))
                .andExpect(jsonPath("$.base").value("USD"))
                .andExpect(jsonPath("$.rates.USD").value(1))
                .andExpect(jsonPath("$.rates.GBP").value(0.809524))
                .andExpect(jsonPath("$.rates.JPY").value(133.333333));

        mockServer.verify();

        Optional<ExchangeRateSnapshot> stored = repository.findByDate(date);
        assertThat(stored).isPresent();
        ExchangeRateSnapshot snapshot = stored.get();
        assertThat(snapshot.getBase()).isEqualTo("USD");
        assertThat(snapshot.getTimestamp()).isEqualTo(1655769599L);
        assertThat(snapshot.getRates().get("USD")).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(snapshot.getRates().get("GBP")).isEqualByComparingTo(new BigDecimal("0.809524"));
        assertThat(snapshot.getRates().get("JPY")).isEqualByComparingTo(new BigDecimal("133.333333"));
    }

    @Test
    void cacheHit_secondCall_doesNotHitFixer_returnsSameBody() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        expectFixerCall(date, """
                {
                  "success": true,
                  "historical": true,
                  "date": "2022-06-20",
                  "timestamp": 1655769599,
                  "base": "EUR",
                  "rates": {
                    "USD": 1.05,
                    "GBP": 0.85
                  }
                }
                """);

        MvcResult first = mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isOk())
                .andReturn();

        // Second call MUST NOT hit fixer — mockServer has no further expectations,
        // so any outbound request would fail with "no further requests expected".
        MvcResult second = mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isOk())
                .andReturn();

        mockServer.verify();
        JSONAssert.assertEquals(
                first.getResponse().getContentAsString(),
                second.getResponse().getContentAsString(),
                JSONCompareMode.STRICT
        );
    }

    @Test
    void malformedDate_returns400_withErrorBody() throws Exception {
        mockMvc.perform(get("/api/v1/rates/not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(400))
                .andExpect(jsonPath("$.error.info").value(containsString("Invalid date format")));
    }

    @Test
    void futureDate_returns400_withInvalidDateExceptionMessage() throws Exception {
        String future = LocalDate.now().plusYears(1).toString();

        mockMvc.perform(get("/api/v1/rates/" + future))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(400))
                .andExpect(jsonPath("$.error.info").value(containsString("future")));

        assertThat(repository.count()).isZero();
    }

    @Test
    void fixerCode106_noRatesForDate_returns404_andDoesNotPersist() throws Exception {
        LocalDate date = LocalDate.of(1900, 1, 1);
        expectFixerCall(date, """
                {
                  "success": false,
                  "error": { "code": 106, "info": "no rates available" }
                }
                """);

        mockMvc.perform(get("/api/v1/rates/1900-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(404))
                .andExpect(jsonPath("$.error.info").value(containsString("1900-01-01")));

        assertThat(repository.findByDate(date)).isEmpty();
    }

    @Test
    void fixerCode104_quotaExceeded_returns502_doesNotRetry() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        // Single expectation — code 104 is permanent, retry must NOT fire.
        expectFixerCall(date, """
                {
                  "success": false,
                  "error": { "code": 104, "info": "Your monthly API request volume has been reached." }
                }
                """);

        mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(502))
                .andExpect(jsonPath("$.error.info").value(containsString("monthly API request volume")));

        mockServer.verify();
        assertThat(repository.findByDate(date)).isEmpty();
    }

    @Test
    void fixerServerError_exhaustsRetries_returns502() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        // 5xx is transient — retry fires up to max-attempts=3.
        mockServer.expect(ExpectedCount.times(3), requestTo(fixerUrl(date)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(502));

        mockServer.verify();
        assertThat(repository.findByDate(date)).isEmpty();
    }

    @Test
    void transient429ThenSuccess_retriesAndReturns200() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        // First attempt: rate-limited. Second attempt: success.
        mockServer.expect(ExpectedCount.once(), requestTo(fixerUrl(date)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        mockServer.expect(ExpectedCount.once(), requestTo(fixerUrl(date)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "historical": true,
                          "date": "2022-06-20",
                          "timestamp": 1655769599,
                          "base": "EUR",
                          "rates": { "USD": 1.05, "GBP": 0.85 }
                        }
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("USD"))
                .andExpect(jsonPath("$.rates.USD").value(1));

        mockServer.verify();
        assertThat(repository.findByDate(date)).isPresent();
    }

    @Test
    void transient429OnEveryAttempt_exhaustsRetriesAndReturns502() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        mockServer.expect(ExpectedCount.times(3), requestTo(fixerUrl(date)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value(502));

        mockServer.verify();
        assertThat(repository.findByDate(date)).isEmpty();
    }

    @Test
    void permanent400_doesNotRetry_returns502() throws Exception {
        LocalDate date = LocalDate.of(2022, 6, 20);
        // Single expectation — 4xx (other than 429) is permanent, retry must NOT fire.
        mockServer.expect(ExpectedCount.once(), requestTo(fixerUrl(date)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value(502));

        mockServer.verify();
        assertThat(repository.findByDate(date)).isEmpty();
    }

    private void expectFixerCall(LocalDate date, String responseBodyJson) {
        mockServer.expect(requestTo(fixerUrl(date)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBodyJson, MediaType.APPLICATION_JSON));
    }

    private static String fixerUrl(LocalDate date) {
        return "http://data.fixer.io/api/" + date + "?access_key=test-key";
    }
}
