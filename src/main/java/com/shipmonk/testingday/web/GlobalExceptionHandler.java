package com.shipmonk.testingday.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.shipmonk.testingday.modules.rates.exception.InvalidDateException;
import com.shipmonk.testingday.modules.rates.exception.ProviderException;
import com.shipmonk.testingday.modules.rates.exception.RatesNotFoundException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(InvalidDateException.class)
  public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDateException ex) {
    log.warn("Invalid date request: {}", ex.getMessage());
    return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
  }

  @ExceptionHandler(RatesNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(RatesNotFoundException ex) {
    log.warn("Rates not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, ex.getMessage()));
  }

  @ExceptionHandler(ProviderException.class)
  public ResponseEntity<ErrorResponse> handleProvider(ProviderException ex) {
    log.error("Provider error — upstream fixer.io call failed", ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse(502, ex.getMessage()));
  }

  @ExceptionHandler(CallNotPermittedException.class)
  public ResponseEntity<ErrorResponse> handleCircuitOpen(CallNotPermittedException ex) {
    log.warn("Circuit breaker OPEN — fixer.io call rejected: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, "30")
        .body(new ErrorResponse(503, "Upstream rate provider temporarily unavailable"));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("Type mismatch for value={}", ex.getValue());
    return ResponseEntity.badRequest().body(
        new ErrorResponse(400, "Invalid date format — expected YYYY-MM-DD, got: " + ex.getValue()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Internal server error"));
  }
}
