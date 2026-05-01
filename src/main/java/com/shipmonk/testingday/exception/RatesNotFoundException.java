package com.shipmonk.testingday.exception;

public class RatesNotFoundException extends ExchangeRateException {

    public RatesNotFoundException(String message) {
        super(message);
    }
}
