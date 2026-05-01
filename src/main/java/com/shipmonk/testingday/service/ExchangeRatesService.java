package com.shipmonk.testingday.service;

import com.shipmonk.testingday.client.FixerClient;
import com.shipmonk.testingday.client.dto.FixerRatesResponse;
import com.shipmonk.testingday.dto.RatesResponse;
import com.shipmonk.testingday.entity.ExchangeRateSnapshot;
import com.shipmonk.testingday.exception.FixerApiException;
import com.shipmonk.testingday.exception.InvalidDateException;
import com.shipmonk.testingday.repository.ExchangeRateSnapshotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeRatesService {

    private static final String BASE_CURRENCY = "USD";

    private final ExchangeRateSnapshotRepository repository;
    private final FixerClient fixerClient;

    public ExchangeRatesService(ExchangeRateSnapshotRepository repository, FixerClient fixerClient) {
        this.repository = repository;
        this.fixerClient = fixerClient;
    }

    public RatesResponse getRatesForDay(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException("Cannot retrieve rates for a future date: " + date);
        }

        return repository.findByDate(date)
                .map(this::toResponse)
                .orElseGet(() -> fetchAndCache(date));
    }

    private RatesResponse fetchAndCache(LocalDate date) {
        FixerRatesResponse fixer = fixerClient.fetchHistoricalRates(date);
        Map<String, BigDecimal> usdBasedRates = rebaseToUsd(fixer.getRates());

        ExchangeRateSnapshot snapshot = new ExchangeRateSnapshot(
                date,
                BASE_CURRENCY,
                fixer.getTimestamp(),
                usdBasedRates,
                LocalDateTime.now()
        );

        try {
            repository.save(snapshot);
        } catch (DataIntegrityViolationException e) {
            // Another concurrent request already stored the same date — read it back
            return repository.findByDate(date)
                    .map(this::toResponse)
                    .orElseThrow(() -> new FixerApiException("Concurrent write conflict for date: " + date, e));
        }

        return toResponse(snapshot);
    }

    private Map<String, BigDecimal> rebaseToUsd(Map<String, BigDecimal> eurBasedRates) {
        BigDecimal usdRate = eurBasedRates.get(BASE_CURRENCY);
        if (usdRate == null || usdRate.compareTo(BigDecimal.ZERO) == 0) {
            throw new FixerApiException(0, "USD rate missing from fixer.io response — cannot rebase to USD");
        }

        Map<String, BigDecimal> usdRates = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : eurBasedRates.entrySet()) {
            usdRates.put(entry.getKey(), entry.getValue().divide(usdRate, 6, RoundingMode.HALF_UP));
        }
        usdRates.put(BASE_CURRENCY, BigDecimal.ONE);
        return usdRates;
    }

    private RatesResponse toResponse(ExchangeRateSnapshot snapshot) {
        return new RatesResponse(
                snapshot.getDate().toString(),
                snapshot.getTimestamp(),
                snapshot.getBase(),
                snapshot.getRates()
        );
    }
}
