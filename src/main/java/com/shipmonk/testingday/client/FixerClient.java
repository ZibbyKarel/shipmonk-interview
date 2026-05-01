package com.shipmonk.testingday.client;

import com.shipmonk.testingday.client.dto.FixerRatesResponse;
import com.shipmonk.testingday.exception.FixerApiException;
import com.shipmonk.testingday.exception.RatesNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@Component
public class FixerClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public FixerClient(RestTemplate restTemplate,
                       @Value("${fixer.api.base-url}") String baseUrl,
                       @Value("${fixer.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public FixerRatesResponse fetchHistoricalRates(LocalDate date) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/{date}")
                .queryParam("access_key", apiKey)
                .buildAndExpand(date.toString())
                .toUriString();

        FixerRatesResponse response;
        try {
            response = restTemplate.getForObject(url, FixerRatesResponse.class);
        } catch (RestClientException e) {
            throw new FixerApiException("Failed to reach fixer.io: " + e.getMessage(), e);
        }

        if (response == null) {
            throw new FixerApiException("Empty response from fixer.io", null);
        }

        if (!response.isSuccess()) {
            int code = response.getError() != null ? response.getError().getCode() : 0;
            String info = response.getError() != null ? response.getError().getInfo() : "Unknown error";

            // fixer code 106 = no rates available for this date
            if (code == 106) {
                throw new RatesNotFoundException("No exchange rates available for date: " + date);
            }
            throw new FixerApiException(code, info);
        }

        if (response.getRates() == null || response.getRates().isEmpty()) {
            throw new RatesNotFoundException("No exchange rates available for date: " + date);
        }

        return response;
    }
}
