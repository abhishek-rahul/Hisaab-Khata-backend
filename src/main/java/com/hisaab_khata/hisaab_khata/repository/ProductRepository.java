package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopId(Long shopId);


    List<Product> findByShopIdAndCategoryId(Long shopId, Long categoryId);

    List<Product> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name);
}

