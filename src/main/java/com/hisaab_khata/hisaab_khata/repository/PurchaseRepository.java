package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByShopId(Long shopId);

    List<Purchase> findByShopIdAndProductId(Long shopId, Long productId);

    List<Purchase> findByShopIdAndShopProductId(Long shopId, Long shopProductId);
}

