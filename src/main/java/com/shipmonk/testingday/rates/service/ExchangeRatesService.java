package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.dto.RatesResponse;
import com.shipmonk.testingday.rates.entity.ExchangeRateSnapshot;
import com.shipmonk.testingday.rates.exception.InvalidDateException;
import com.shipmonk.testingday.rates.exception.ProviderException;
import com.shipmonk.testingday.rates.provider.ExchangeRateProvider;
import com.shipmonk.testingday.rates.provider.ExchangeRates;
import com.shipmonk.testingday.rates.repository.ExchangeRateSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ExchangeRatesService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRatesService.class);

    private final ExchangeRateSnapshotRepository repository;
    private final ExchangeRateProvider provider;

    public ExchangeRatesService(ExchangeRateSnapshotRepository repository, ExchangeRateProvider provider) {
        this.repository = repository;
        this.provider = provider;
    }

    public RatesResponse getRatesForDay(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException("Cannot retrieve rates for a future date: " + date);
        }

        return repository.findByDate(date)
                .map(snapshot -> {
                    log.debug("Cache hit for date={}", date);
                    return toResponse(snapshot);
                })
                .orElseGet(() -> {
                    log.info("Cache miss for date={}, fetching from provider", date);
                    return fetchAndCache(date);
                });
    }

    private RatesResponse fetchAndCache(LocalDate date) {
        ExchangeRates rates = provider.fetchRates(date);

        ExchangeRateSnapshot snapshot = new ExchangeRateSnapshot(
                date,
                rates.base(),
                rates.timestamp(),
                rates.rates(),
                LocalDateTime.now()
        );

        try {
            repository.save(snapshot);
            log.info("Cached rates for date={}", date);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent write conflict for date={}, re-reading from cache", date);
            return repository.findByDate(date)
                    .map(this::toResponse)
                    .orElseThrow(() -> new ProviderException("Concurrent write conflict for date: " + date, e));
        }

        return toResponse(snapshot);
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
