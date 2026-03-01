package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.ShopProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {

    List<ShopProduct> findByShop_Id(Long shopId);

    Optional<ShopProduct> findByShop_IdAndId(Long shopId, Long id);
}
