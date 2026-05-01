package com.shipmonk.testingday;

import com.shipmonk.testingday.client.FixerClient;
import com.shipmonk.testingday.client.dto.FixerRatesResponse;
import com.shipmonk.testingday.dto.RatesResponse;
import com.shipmonk.testingday.entity.ExchangeRateSnapshot;
import com.shipmonk.testingday.exception.FixerApiException;
import com.shipmonk.testingday.exception.InvalidDateException;
import com.shipmonk.testingday.repository.ExchangeRateSnapshotRepository;
import com.shipmonk.testingday.service.ExchangeRatesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRatesServiceTest {

    @Mock
    private ExchangeRateSnapshotRepository repository;

    @Mock
    private FixerClient fixerClient;

    @InjectMocks
    private ExchangeRatesService service;

    @Test
    void cacheHit_returnsFromDb_withoutCallingFixer() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        Map<String, BigDecimal> cachedRates = Map.of(
                "USD", BigDecimal.ONE,
                "EUR", new BigDecimal("0.934000"),
                "GBP", new BigDecimal("0.812000")
        );
        ExchangeRateSnapshot cached = new ExchangeRateSnapshot(
                date, "USD", 1655769599L, cachedRates, LocalDateTime.now()
        );
        when(repository.findByDate(date)).thenReturn(Optional.of(cached));

        RatesResponse response = service.getRatesForDay(date);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isHistorical()).isTrue();
        assertThat(response.getBase()).isEqualTo("USD");
        assertThat(response.getDate()).isEqualTo("2022-06-20");
        assertThat(response.getTimestamp()).isEqualTo(1655769599L);
        assertThat(response.getRates()).containsExactlyInAnyOrderEntriesOf(cachedRates);
        verifyNoInteractions(fixerClient);
        verify(repository, never()).save(any());
    }

    @Test
    void cacheMiss_fetchesFromFixer_rebasesEurToUsd_andPersists() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        when(repository.findByDate(date)).thenReturn(Optional.empty());

        // Fixer returns EUR-based rates: USD=1.05, GBP=0.85, JPY=140.0
        FixerRatesResponse fixerResponse = newFixerResponse(date, 1655769599L, Map.of(
                "USD", new BigDecimal("1.05"),
                "GBP", new BigDecimal("0.85"),
                "JPY", new BigDecimal("140.0")
        ));
        when(fixerClient.fetchHistoricalRates(date)).thenReturn(fixerResponse);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RatesResponse response = service.getRatesForDay(date);

        assertThat(response.getBase()).isEqualTo("USD");
        // USD/USD = 1.0
        assertThat(response.getRates().get("USD")).isEqualByComparingTo(BigDecimal.ONE);
        // GBP_USD = 0.85 / 1.05 → 0.809524 (HALF_UP, scale 6)
        assertThat(response.getRates().get("GBP")).isEqualByComparingTo(new BigDecimal("0.809524"));
        // JPY_USD = 140.0 / 1.05 → 133.333333 (HALF_UP, scale 6)
        assertThat(response.getRates().get("JPY")).isEqualByComparingTo(new BigDecimal("133.333333"));

        ArgumentCaptor<ExchangeRateSnapshot> captor = ArgumentCaptor.forClass(ExchangeRateSnapshot.class);
        verify(repository).save(captor.capture());
        ExchangeRateSnapshot saved = captor.getValue();
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getBase()).isEqualTo("USD");
        assertThat(saved.getTimestamp()).isEqualTo(1655769599L);
        assertThat(saved.getRates().get("USD")).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void futureDate_throwsInvalidDateException_withoutHittingDbOrFixer() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> service.getRatesForDay(futureDate))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining("future");

        verifyNoInteractions(repository);
        verifyNoInteractions(fixerClient);
    }

    @Test
    void usdMissingFromFixerResponse_throwsFixerApiException() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        when(repository.findByDate(date)).thenReturn(Optional.empty());

        // No USD in rates - rebasing must fail cleanly
        FixerRatesResponse fixerResponse = newFixerResponse(date, 1L, Map.of(
                "GBP", new BigDecimal("0.85")
        ));
        when(fixerClient.fetchHistoricalRates(date)).thenReturn(fixerResponse);

        assertThatThrownBy(() -> service.getRatesForDay(date))
                .isInstanceOf(FixerApiException.class)
                .hasMessageContaining("USD rate missing");

        verify(repository, never()).save(any());
    }

    @Test
    void concurrentWrite_violatesUniqueConstraint_returnsValueWrittenByOtherRequest() {
        LocalDate date = LocalDate.of(2022, 6, 20);

        Map<String, BigDecimal> otherRequestRates = Map.of(
                "USD", BigDecimal.ONE,
                "EUR", new BigDecimal("0.95")
        );
        ExchangeRateSnapshot writtenByOther = new ExchangeRateSnapshot(
                date, "USD", 999L, otherRequestRates, LocalDateTime.now()
        );

        // First lookup: empty (cache miss), second lookup (after constraint violation): present
        when(repository.findByDate(date))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(writtenByOther));

        FixerRatesResponse fixerResponse = newFixerResponse(date, 1L, Map.of(
                "USD", new BigDecimal("1.0"),
                "EUR", new BigDecimal("0.95")
        ));
        when(fixerClient.fetchHistoricalRates(date)).thenReturn(fixerResponse);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

        RatesResponse response = service.getRatesForDay(date);

        assertThat(response.getTimestamp()).isEqualTo(999L);
        assertThat(response.getRates().get("EUR")).isEqualByComparingTo(new BigDecimal("0.95"));
        verify(repository, times(2)).findByDate(date);
    }

    private FixerRatesResponse newFixerResponse(LocalDate date, long timestamp,
                                                Map<String, BigDecimal> eurBasedRates) {
        FixerRatesResponse r = new FixerRatesResponse();
        r.setSuccess(true);
        r.setHistorical(true);
        r.setDate(date.toString());
        r.setTimestamp(timestamp);
        r.setBase("EUR");
        r.setRates(new HashMap<>(eurBasedRates));
        return r;
    }
}
