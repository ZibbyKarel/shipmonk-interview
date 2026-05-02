package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.dto.RatesResponse;
import com.shipmonk.testingday.rates.entity.ExchangeRateSnapshot;
import com.shipmonk.testingday.rates.exception.InvalidDateException;
import com.shipmonk.testingday.rates.exception.ProviderException;
import com.shipmonk.testingday.rates.provider.ExchangeRateProvider;
import com.shipmonk.testingday.rates.provider.ExchangeRates;
import com.shipmonk.testingday.rates.repository.ExchangeRateSnapshotRepository;
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
    private ExchangeRateProvider provider;

    @InjectMocks
    private ExchangeRatesService service;

    @Test
    void cacheHit_returnsFromDb_withoutCallingProvider() {
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
        assertThat(response.base()).isEqualTo("USD");
        assertThat(response.date()).isEqualTo("2022-06-20");
        assertThat(response.timestamp()).isEqualTo(1655769599L);
        assertThat(response.rates()).containsExactlyInAnyOrderEntriesOf(cachedRates);
        verifyNoInteractions(provider);
        verify(repository, never()).save(any());
    }

    @Test
    void cacheMiss_fetchesFromProvider_persistsAndReturns() {
        LocalDate date = LocalDate.of(2022, 6, 20);
        Map<String, BigDecimal> usdRates = Map.of(
                "USD", BigDecimal.ONE,
                "GBP", new BigDecimal("0.809524"),
                "JPY", new BigDecimal("133.333333")
        );
        when(repository.findByDate(date)).thenReturn(Optional.empty());
        when(provider.fetchRates(date)).thenReturn(new ExchangeRates(date, "USD", 1655769599L, usdRates));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RatesResponse response = service.getRatesForDay(date);

        assertThat(response.base()).isEqualTo("USD");
        assertThat(response.timestamp()).isEqualTo(1655769599L);
        assertThat(response.rates()).containsExactlyInAnyOrderEntriesOf(usdRates);

        ArgumentCaptor<ExchangeRateSnapshot> captor = ArgumentCaptor.forClass(ExchangeRateSnapshot.class);
        verify(repository).save(captor.capture());
        ExchangeRateSnapshot saved = captor.getValue();
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getBase()).isEqualTo("USD");
        assertThat(saved.getTimestamp()).isEqualTo(1655769599L);
        assertThat(saved.getRates()).containsExactlyInAnyOrderEntriesOf(usdRates);
    }

    @Test
    void futureDate_throwsInvalidDateException_withoutHittingDbOrProvider() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> service.getRatesForDay(futureDate))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining("future");

        verifyNoInteractions(repository);
        verifyNoInteractions(provider);
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

        when(provider.fetchRates(date)).thenReturn(new ExchangeRates(
                date, "USD", 1L, Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("0.95"))
        ));
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

        RatesResponse response = service.getRatesForDay(date);

        assertThat(response.timestamp()).isEqualTo(999L);
        assertThat(response.rates().get("EUR")).isEqualByComparingTo(new BigDecimal("0.95"));
        verify(repository, times(2)).findByDate(date);
    }

    @Test
    void concurrentWrite_butSecondLookupAlsoEmpty_throwsProviderException() {
        LocalDate date = LocalDate.of(2022, 6, 20);

        when(repository.findByDate(date)).thenReturn(Optional.empty());
        when(provider.fetchRates(date)).thenReturn(new ExchangeRates(
                date, "USD", 1L, Map.of("USD", BigDecimal.ONE)
        ));
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.getRatesForDay(date))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("Concurrent write conflict");
    }
}
