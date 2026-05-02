package com.shipmonk.testingday.modules.rates.controller;

import com.shipmonk.testingday.modules.rates.controller.ExchangeRatesController;
import com.shipmonk.testingday.modules.rates.dto.RatesResponse;
import com.shipmonk.testingday.modules.rates.exception.InvalidDateException;
import com.shipmonk.testingday.modules.rates.exception.ProviderException;
import com.shipmonk.testingday.modules.rates.exception.RatesNotFoundException;
import com.shipmonk.testingday.modules.rates.service.ExchangeRatesService;
import com.shipmonk.testingday.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExchangeRatesController.class)
@Import(GlobalExceptionHandler.class)
class ExchangeRatesControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ExchangeRatesService exchangeRatesService;

        @Test
        void happyPath_returnsRatesJsonInFixerShape() throws Exception {
                RatesResponse stub = new RatesResponse(
                                "2022-06-20", 1655769599L, "USD",
                                Map.of(
                                                "USD", BigDecimal.ONE,
                                                "EUR", new BigDecimal("0.952381"),
                                                "GBP", new BigDecimal("0.809524")));
                when(exchangeRatesService.getRatesForDay(LocalDate.of(2022, 6, 20))).thenReturn(stub);

                mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.historical").value(true))
                                .andExpect(jsonPath("$.date").value("2022-06-20"))
                                .andExpect(jsonPath("$.timestamp").value(1655769599L))
                                .andExpect(jsonPath("$.base").value("USD"))
                                .andExpect(jsonPath("$.rates.USD").value(1))
                                .andExpect(jsonPath("$.rates.EUR").value(0.952381))
                                .andExpect(jsonPath("$.rates.GBP").value(0.809524));
        }

        @Test
        void malformedDate_returns400_withErrorBody() throws Exception {
                mockMvc.perform(get("/api/v1/rates/not-a-date"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value(400))
                                .andExpect(jsonPath("$.error.info")
                                                .value(org.hamcrest.Matchers.containsString("Invalid date format")));
        }

        @Test
        void futureDate_returns400_fromService() throws Exception {
                when(exchangeRatesService.getRatesForDay(any()))
                                .thenThrow(new InvalidDateException(
                                                "Cannot retrieve rates for a future date: 2099-01-01"));

                mockMvc.perform(get("/api/v1/rates/2099-01-01"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value(400))
                                .andExpect(jsonPath("$.error.info")
                                                .value(org.hamcrest.Matchers.containsString("future")));
        }

        @Test
        void notFound_returns404() throws Exception {
                when(exchangeRatesService.getRatesForDay(any()))
                                .thenThrow(new RatesNotFoundException(
                                                "No exchange rates available for date: 1900-01-01"));

                mockMvc.perform(get("/api/v1/rates/1900-01-01"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value(404));
        }

        @Test
        void providerError_returns502() throws Exception {
                when(exchangeRatesService.getRatesForDay(any()))
                                .thenThrow(new ProviderException(
                                                "fixer.io returned error: Your monthly API request volume has been reached."));

                mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                                .andExpect(status().isBadGateway())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value(502))
                                .andExpect(jsonPath("$.error.info").value(
                                                org.hamcrest.Matchers.containsString("monthly API request volume")));
        }

        @Test
        void providerNetworkError_returns502() throws Exception {
                when(exchangeRatesService.getRatesForDay(any()))
                                .thenThrow(new ProviderException("Failed to reach fixer.io: timeout",
                                                new RuntimeException("timeout")));

                mockMvc.perform(get("/api/v1/rates/2022-06-20"))
                                .andExpect(status().isBadGateway())
                                .andExpect(jsonPath("$.error.code").value(502));
        }
}
