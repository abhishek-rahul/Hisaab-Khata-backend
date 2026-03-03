package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {

    Optional<DailySummary> findByShop_IdAndDay(Long shopId, LocalDate day);

    List<DailySummary> findByShop_IdAndDayBetweenOrderByDayAsc(Long shopId, LocalDate from, LocalDate to);
}
