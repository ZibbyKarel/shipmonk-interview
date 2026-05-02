package com.shipmonk.testingday.dto;

import java.math.BigDecimal;
import java.util.Map;

public record RatesResponse(String date, long timestamp, String base, Map<String, BigDecimal> rates) {
    public boolean isSuccess() {
        return true;
    }

    public boolean isHistorical() {
        return true;
    }
}
