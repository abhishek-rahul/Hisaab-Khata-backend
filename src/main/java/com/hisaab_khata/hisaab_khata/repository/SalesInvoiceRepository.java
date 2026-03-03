package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.SalesInvoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    Optional<SalesInvoice> findByShop_IdAndId(Long shopId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SalesInvoice s WHERE s.shop.id = :shopId AND s.id = :id")
    Optional<SalesInvoice> findByShop_IdAndIdForUpdate(@Param("shopId") Long shopId, @Param("id") Long id);

    List<SalesInvoice> findByShop_IdAndInvoiceDateBetweenOrderByInvoiceDateAscIdAsc(
            Long shopId, LocalDate from, LocalDate to);
}
