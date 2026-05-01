package com.shipmonk.testingday.repository;

import com.shipmonk.testingday.entity.ExchangeRateSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateSnapshotRepository extends JpaRepository<ExchangeRateSnapshot, Long> {

    Optional<ExchangeRateSnapshot> findByDate(LocalDate date);
}
