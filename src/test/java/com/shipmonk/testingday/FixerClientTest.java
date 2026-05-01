package com.shipmonk.testingday;

import com.shipmonk.testingday.client.FixerClient;
import com.shipmonk.testingday.client.dto.FixerRatesResponse;
import com.shipmonk.testingday.exception.FixerApiException;
import com.shipmonk.testingday.exception.RatesNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FixerClientTest {

    private static final String BASE_URL = "http://data.fixer.io/api";
    private static final String API_KEY = "test-key-123";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private FixerClient client;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new FixerClient(restTemplate, BASE_URL, API_KEY);
    }

    @Test
    void successResponse_returnsParsedRates_andCallsCorrectUrl() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        String body = """
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
                """;

        server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        FixerRatesResponse response = client.fetchHistoricalRates(date);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDate()).isEqualTo("2022-06-20");
        assertThat(response.getTimestamp()).isEqualTo(1655769599L);
        assertThat(response.getRates()).containsKeys("USD", "GBP");
        server.verify();
    }

    @Test
    void fixerCode106_throwsRatesNotFoundException() {
        LocalDate date = LocalDate.of(1900, 1, 1);
        String body = """
                {
                  "success": false,
                  "error": { "code": 106, "info": "no rates available" }
                }
                """;

        server.expect(requestTo("http://data.fixer.io/api/1900-01-01?access_key=test-key-123"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchHistoricalRates(date))
                .isInstanceOf(RatesNotFoundException.class)
                .hasMessageContaining("1900-01-01");
    }

    @Test
    void fixerCode104_quotaReached_throwsFixerApiException() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        String body = """
                {
                  "success": false,
                  "error": { "code": 104, "info": "Your monthly API request volume has been reached." }
                }
                """;

        server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchHistoricalRates(date))
                .isInstanceOf(FixerApiException.class)
                .satisfies(ex -> {
                    FixerApiException fe = (FixerApiException) ex;
                    assertThat(fe.getFixerCode()).isEqualTo(104);
                    assertThat(fe.getMessage()).contains("monthly API request volume");
                });
    }

    @Test
    void emptyRatesMap_throwsRatesNotFoundException() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        String body = """
                {
                  "success": true,
                  "historical": true,
                  "date": "2022-06-20",
                  "timestamp": 1655769599,
                  "base": "EUR",
                  "rates": {}
                }
                """;

        server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchHistoricalRates(date))
                .isInstanceOf(RatesNotFoundException.class);
    }

    @Test
    void serverError_wrappedInFixerApiException() {
        LocalDate date = LocalDate.of(2022, 6, 20);

        server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchHistoricalRates(date))
                .isInstanceOf(FixerApiException.class)
                .hasMessageContaining("Failed to reach fixer.io");
    }
}
