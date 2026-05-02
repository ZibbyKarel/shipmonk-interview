package com.shipmonk.testingday.provider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExchangeRates(LocalDate date, String base, long timestamp, Map<String, BigDecimal> rates) {}
