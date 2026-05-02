package com.shipmonk.testingday.rates.provider.fixer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixerRatesResponse(
        boolean success,
        FixerErrorDetail error,
        Boolean historical,
        String date,
        Long timestamp,
        String base,
        Map<String, BigDecimal> rates
) {}
