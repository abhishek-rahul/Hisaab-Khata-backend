package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.SupplierProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierProductMappingRepository extends JpaRepository<SupplierProductMapping, Long> {

    Optional<SupplierProductMapping> findByShop_IdAndSupplierParty_IdAndNormalizedName(
            Long shopId, Long supplierPartyId, String normalizedName);
}
