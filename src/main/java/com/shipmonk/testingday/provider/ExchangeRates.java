package com.shipmonk.testingday.provider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class ExchangeRates {

    private final LocalDate date;
    private final String base;
    private final long timestamp;
    private final Map<String, BigDecimal> rates;

    public ExchangeRates(LocalDate date, String base, long timestamp, Map<String, BigDecimal> rates) {
        this.date = date;
        this.base = base;
        this.timestamp = timestamp;
        this.rates = rates;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getBase() {
        return base;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }
}
