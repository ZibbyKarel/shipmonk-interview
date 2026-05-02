package com.shipmonk.testingday.rates.provider;

import com.shipmonk.testingday.rates.exception.ProviderException;
import com.shipmonk.testingday.rates.exception.RatesNotFoundException;
import com.shipmonk.testingday.rates.exception.TransientProviderException;

import java.time.LocalDate;

public interface ExchangeRateProvider {

    /**
     * Fetch rates for the given date with USD as the base currency.
     * Implementations are responsible for any provider-specific rebasing.
     *
     * @throws RatesNotFoundException     when the provider has no rates for that date
     * @throws TransientProviderException for retryable failures (rate limit, 5xx, IO)
     * @throws ProviderException          for permanent provider failures (auth, quota, malformed response)
     */
    ExchangeRates fetchRates(LocalDate date);
}
