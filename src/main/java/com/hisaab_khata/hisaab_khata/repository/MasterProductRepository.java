package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterProductRepository extends JpaRepository<MasterProduct, Long> {

    Optional<MasterProduct> findByNormalizedNameIgnoreCase(String normalizedName);
}
