package com.shipmonk.testingday.modules.rates.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shipmonk.testingday.modules.rates.entity.ExchangeRateSnapshot;

public interface ExchangeRateSnapshotRepository extends JpaRepository<ExchangeRateSnapshot, Long> {

  Optional<ExchangeRateSnapshot> findByDate(LocalDate date);
}
