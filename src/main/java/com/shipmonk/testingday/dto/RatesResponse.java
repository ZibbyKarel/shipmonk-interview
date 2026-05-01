package com.shipmonk.testingday.dto;

import java.math.BigDecimal;
import java.util.Map;

public class RatesResponse {

    private final boolean success = true;
    private final boolean historical = true;
    private final String date;
    private final long timestamp;
    private final String base;
    private final Map<String, BigDecimal> rates;

    public RatesResponse(String date, long timestamp, String base, Map<String, BigDecimal> rates) {
        this.date = date;
        this.timestamp = timestamp;
        this.base = base;
        this.rates = rates;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isHistorical() {
        return historical;
    }

    public String getDate() {
        return date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBase() {
        return base;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }
}
