package com.shipmonk.testingday.rates.exception;

public class RatesNotFoundException extends ExchangeRateException {

    public RatesNotFoundException(String message) {
        super(message);
    }
}
