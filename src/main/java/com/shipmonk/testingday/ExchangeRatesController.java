package com.shipmonk.testingday;

import com.shipmonk.testingday.dto.RatesResponse;
import com.shipmonk.testingday.exception.InvalidDateException;
import com.shipmonk.testingday.service.ExchangeRatesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(path = "/api/v1/rates")
public class ExchangeRatesController {

    private final ExchangeRatesService exchangeRatesService;

    public ExchangeRatesController(ExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{day}")
    public ResponseEntity<RatesResponse> getRates(@PathVariable("day") String day) {
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
