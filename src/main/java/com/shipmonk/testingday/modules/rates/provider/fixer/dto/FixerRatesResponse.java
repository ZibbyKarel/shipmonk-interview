package com.shipmonk.testingday.modules.rates.provider.fixer.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixerRatesResponse(boolean success, FixerErrorDetail error, Boolean historical,
    String date, Long timestamp, String base, Map<String, BigDecimal> rates) {
}
