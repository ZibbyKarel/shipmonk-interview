package com.shipmonk.testingday.service;

import com.shipmonk.testingday.dto.RatesResponse;
import com.shipmonk.testingday.entity.ExchangeRateSnapshot;
import com.shipmonk.testingday.exception.InvalidDateException;
import com.shipmonk.testingday.exception.ProviderException;
import com.shipmonk.testingday.provider.ExchangeRateProvider;
import com.shipmonk.testingday.provider.ExchangeRates;
import com.shipmonk.testingday.repository.ExchangeRateSnapshotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ExchangeRatesService {

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
                .map(this::toResponse)
                .orElseGet(() -> fetchAndCache(date));
    }

    private RatesResponse fetchAndCache(LocalDate date) {
        ExchangeRates rates = provider.fetchRates(date);

        ExchangeRateSnapshot snapshot = new ExchangeRateSnapshot(
                date,
                rates.getBase(),
                rates.getTimestamp(),
                rates.getRates(),
                LocalDateTime.now()
        );

        try {
            repository.save(snapshot);
        } catch (DataIntegrityViolationException e) {
            // Another concurrent request already stored the same date — read it back
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
