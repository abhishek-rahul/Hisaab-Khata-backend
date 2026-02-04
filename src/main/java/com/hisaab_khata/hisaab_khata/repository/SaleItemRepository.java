package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    // Fetch all items for sale, but only for current shop
    List<SaleItem> findBySaleIdAndShopId(Long saleId, Long shopId);

    // For reporting
    List<SaleItem> findByShopId(Long shopId);
}

