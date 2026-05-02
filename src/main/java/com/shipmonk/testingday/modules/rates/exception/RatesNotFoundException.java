package com.shipmonk.testingday.modules.rates.exception;

public class RatesNotFoundException extends ExchangeRateException {

    public RatesNotFoundException(String message) {
        super(message);
    }
}
