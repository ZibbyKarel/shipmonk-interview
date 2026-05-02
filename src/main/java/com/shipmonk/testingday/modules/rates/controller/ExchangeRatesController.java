package com.shipmonk.testingday.modules.rates.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.shipmonk.testingday.modules.rates.dto.RatesResponse;
import com.shipmonk.testingday.modules.rates.exception.InvalidDateException;
import com.shipmonk.testingday.modules.rates.service.ExchangeRatesService;

@RestController
@RequestMapping(path = "/api/v1/rates")
public class ExchangeRatesController {

  private static final Logger log = LoggerFactory.getLogger(ExchangeRatesController.class);

  private final ExchangeRatesService exchangeRatesService;

  public ExchangeRatesController(ExchangeRatesService exchangeRatesService) {
    this.exchangeRatesService = exchangeRatesService;
  }

  @RequestMapping(method = RequestMethod.GET, path = "/{day}")
  public ResponseEntity<RatesResponse> getRates(@PathVariable("day") String day) {
    log.info("Received request for date={}", day);
    LocalDate date;
    try {
      date = LocalDate.parse(day);
    } catch (DateTimeParseException e) {
      throw new InvalidDateException("Invalid date format — expected YYYY-MM-DD, got: " + day);
    }

    RatesResponse response = exchangeRatesService.getRatesForDay(date);
    return ResponseEntity.ok(response);
  }
}
