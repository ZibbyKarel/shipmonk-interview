package com.shipmonk.testingday.modules.rates.exception;

public class InvalidDateException extends ExchangeRateException {

  public InvalidDateException(String message) {
    super(message);
  }
}
