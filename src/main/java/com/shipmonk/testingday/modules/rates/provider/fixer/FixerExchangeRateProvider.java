package com.shipmonk.testingday.modules.rates.provider.fixer;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.shipmonk.testingday.modules.rates.exception.ProviderException;
import com.shipmonk.testingday.modules.rates.exception.RatesNotFoundException;
import com.shipmonk.testingday.modules.rates.exception.TransientProviderException;
import com.shipmonk.testingday.modules.rates.provider.ExchangeRateProvider;
import com.shipmonk.testingday.modules.rates.provider.ExchangeRates;
import com.shipmonk.testingday.modules.rates.provider.fixer.dto.FixerRatesResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class FixerExchangeRateProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(FixerExchangeRateProvider.class);

    private static final String BASE_CURRENCY = "USD";
    private static final int FIXER_NO_RATES_FOR_DATE = 106;

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public FixerExchangeRateProvider(RestTemplate restTemplate,
            @Value("${fixer.api.base-url}") String baseUrl,
            @Value("${fixer.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    @Retry(name = "fixer")
    public ExchangeRates fetchRates(LocalDate date) {
        log.info("Calling fixer.io for date={}", date);
        FixerRatesResponse response = callFixer(date);

        if (response == null) {
            throw new ProviderException("Empty response from fixer.io");
        }

        if (!response.success()) {
            int code = response.error() != null ? response.error().code() : 0;
            String info = response.error() != null ? response.error().info() : "Unknown error";

            if (code == FIXER_NO_RATES_FOR_DATE) {
                throw new RatesNotFoundException("No exchange rates available for date: " + date);
            }
            throw new ProviderException("fixer.io returned error: " + info);
        }

        if (response.rates() == null || response.rates().isEmpty()) {
            throw new RatesNotFoundException("No exchange rates available for date: " + date);
        }

        Map<String, BigDecimal> usdRates = rebaseToUsd(response.rates());
        long timestamp = response.timestamp() != null ? response.timestamp() : 0L;
        log.info("Received rates from fixer.io for date={}, timestamp={}", date, timestamp);
        return new ExchangeRates(date, BASE_CURRENCY, timestamp, usdRates);
    }

    private FixerRatesResponse callFixer(LocalDate date) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/{date}")
                .queryParam("access_key", apiKey)
                .buildAndExpand(date.toString())
                .toUriString();

        try {
            return restTemplate.getForObject(url, FixerRatesResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("fixer.io rate limited (429) for date={}", date);
                throw new TransientProviderException("fixer.io rate limited (429): " + e.getMessage(), e);
            }
            log.error("fixer.io client error {} for date={}", e.getStatusCode(), date);
            throw new ProviderException("fixer.io client error " + e.getStatusCode() + ": " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.warn("fixer.io server error {} for date={}", e.getStatusCode(), date);
            throw new TransientProviderException("fixer.io server error " + e.getStatusCode() + ": " + e.getMessage(),
                    e);
        } catch (ResourceAccessException e) {
            log.warn("fixer.io network error for date={}: {}", date, e.getMessage());
            throw new TransientProviderException("fixer.io network error: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Failed to reach fixer.io for date={}", date, e);
            throw new ProviderException("Failed to reach fixer.io: " + e.getMessage(), e);
        }
    }

    private Map<String, BigDecimal> rebaseToUsd(Map<String, BigDecimal> sourceRates) {
        BigDecimal usdRate = sourceRates.get(BASE_CURRENCY);
        if (usdRate == null || usdRate.compareTo(BigDecimal.ZERO) == 0) {
            throw new ProviderException("USD rate missing from fixer.io response — cannot rebase to USD");
        }

        Map<String, BigDecimal> usdRates = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : sourceRates.entrySet()) {
            usdRates.put(entry.getKey(), entry.getValue().divide(usdRate, 6, RoundingMode.HALF_UP));
        }
        usdRates.put(BASE_CURRENCY, BigDecimal.ONE);
        return usdRates;
    }
}
