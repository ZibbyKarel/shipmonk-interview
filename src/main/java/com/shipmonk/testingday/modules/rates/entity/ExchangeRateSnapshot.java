package com.shipmonk.testingday.modules.rates.entity;

import javax.persistence.*;

import com.shipmonk.testingday.modules.rates.converter.RatesMapConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "exchange_rate_snapshot", uniqueConstraints = @UniqueConstraint(columnNames = "rate_date"))
public class ExchangeRateSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "base_currency", nullable = false)
    private String base;

    @Column(name = "rates_timestamp", nullable = false)
    private Long timestamp;

    @Convert(converter = RatesMapConverter.class)
    @Column(name = "rates", columnDefinition = "TEXT", nullable = false)
    private Map<String, BigDecimal> rates;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public ExchangeRateSnapshot() {
    }

    public ExchangeRateSnapshot(LocalDate date, String base, Long timestamp,
            Map<String, BigDecimal> rates, LocalDateTime fetchedAt) {
        this.date = date;
        this.base = base;
        this.timestamp = timestamp;
        this.rates = rates;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getBase() {
        return base;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
}
