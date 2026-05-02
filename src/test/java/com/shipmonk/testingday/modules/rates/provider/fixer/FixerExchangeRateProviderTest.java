package com.shipmonk.testingday.modules.rates.provider.fixer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.shipmonk.testingday.modules.rates.exception.ProviderException;
import com.shipmonk.testingday.modules.rates.exception.RatesNotFoundException;
import com.shipmonk.testingday.modules.rates.exception.TransientProviderException;
import com.shipmonk.testingday.modules.rates.provider.ExchangeRates;
import com.shipmonk.testingday.modules.rates.provider.fixer.FixerExchangeRateProvider;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FixerExchangeRateProviderTest {

        private static final String BASE_URL = "http://data.fixer.io/api";
        private static final String API_KEY = "test-key-123";

        private RestTemplate restTemplate;
        private MockRestServiceServer server;
        private FixerExchangeRateProvider provider;

        @BeforeEach
        void setup() {
                restTemplate = new RestTemplate();
                server = MockRestServiceServer.createServer(restTemplate);
                provider = new FixerExchangeRateProvider(restTemplate, BASE_URL, API_KEY);
        }

        @Test
        void successResponse_rebasesEurToUsd_andReturnsExchangeRates() {
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
                                    "GBP": 0.85,
                                    "JPY": 140.0
                                  }
                                }
                                """;

                server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

                ExchangeRates rates = provider.fetchRates(date);

                assertThat(rates.date()).isEqualTo(date);
                assertThat(rates.base()).isEqualTo("USD");
                assertThat(rates.timestamp()).isEqualTo(1655769599L);
                // USD/USD = 1.0
                assertThat(rates.rates().get("USD")).isEqualByComparingTo(BigDecimal.ONE);
                // GBP_USD = 0.85 / 1.05 → 0.809524 (HALF_UP, scale 6)
                assertThat(rates.rates().get("GBP")).isEqualByComparingTo(new BigDecimal("0.809524"));
                // JPY_USD = 140.0 / 1.05 → 133.333333 (HALF_UP, scale 6)
                assertThat(rates.rates().get("JPY")).isEqualByComparingTo(new BigDecimal("133.333333"));
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

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(RatesNotFoundException.class)
                                .hasMessageContaining("1900-01-01");
        }

        @Test
        void fixerCode104_quotaReached_throwsPermanentProviderException() {
                LocalDate date = LocalDate.of(2022, 6, 20);
                String body = """
                                {
                                  "success": false,
                                  "error": { "code": 104, "info": "Your monthly API request volume has been reached." }
                                }
                                """;

                server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(ProviderException.class)
                                .isNotInstanceOf(TransientProviderException.class)
                                .hasMessageContaining("monthly API request volume");
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

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(RatesNotFoundException.class);
        }

        @Test
        void usdMissingFromRates_throwsPermanentProviderException() {
                LocalDate date = LocalDate.of(2022, 6, 20);
                String body = """
                                {
                                  "success": true,
                                  "historical": true,
                                  "date": "2022-06-20",
                                  "timestamp": 1655769599,
                                  "base": "EUR",
                                  "rates": { "GBP": 0.85 }
                                }
                                """;

                server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(ProviderException.class)
                                .isNotInstanceOf(TransientProviderException.class)
                                .hasMessageContaining("USD rate missing");
        }

        @Test
        void httpServerError_throwsTransientProviderException() {
                LocalDate date = LocalDate.of(2022, 6, 20);

                server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                                .andRespond(withServerError());

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(TransientProviderException.class)
                                .hasMessageContaining("fixer.io server error");
        }

        @Test
        void httpClientError429_throwsTransientProviderException() {
                LocalDate date = LocalDate.of(2022, 6, 20);

                server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(TransientProviderException.class)
                                .hasMessageContaining("rate limited");
        }

        @Test
        void httpClientError400_throwsPermanentProviderException() {
                LocalDate date = LocalDate.of(2022, 6, 20);

                server.expect(requestTo("http://data.fixer.io/api/2022-06-20?access_key=test-key-123"))
                                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

                assertThatThrownBy(() -> provider.fetchRates(date))
                                .isInstanceOf(ProviderException.class)
                                .isNotInstanceOf(TransientProviderException.class)
                                .hasMessageContaining("client error");
        }
}
