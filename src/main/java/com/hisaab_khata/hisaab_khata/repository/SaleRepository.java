package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByShopId(Long shopId);

    // For listing latest sales
    List<Sale> findByShopIdOrderByCreatedAtDesc(Long shopId);


    List<Sale> findByShopIdAndCreatedAtBetween(Long shopId, LocalDateTime start, LocalDateTime end);

    @Query("""
                SELECT COALESCE(SUM(s.totalAmount), 0)
                FROM Sale s
                WHERE s.shopId = :shopId
                  AND s.createdAt >= :start
                  AND s.createdAt < :end
            """)
    Double getTodayTotalSales(
            @Param("shopId") Long shopId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
