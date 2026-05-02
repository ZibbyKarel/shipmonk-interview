package com.shipmonk.testingday.modules.rates.exception;

public class ProviderException extends ExchangeRateException {

  public ProviderException(String message) {
    super(message);
  }

  public ProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
