package com.shipmonk.testingday.exception;

public class FixerApiException extends ExchangeRateException {

    private final int fixerCode;

    public FixerApiException(int fixerCode, String message) {
        super(message);
        this.fixerCode = fixerCode;
    }

    public FixerApiException(String message, Throwable cause) {
        super(message, cause);
        this.fixerCode = 0;
    }

    public int getFixerCode() {
        return fixerCode;
    }
}
