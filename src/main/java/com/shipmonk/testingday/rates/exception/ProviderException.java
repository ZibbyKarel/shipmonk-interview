package com.shipmonk.testingday.rates.exception;

public class ProviderException extends ExchangeRateException {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
