package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterProductRepository extends JpaRepository<MasterProduct, Long> {

    Optional<MasterProduct> findByNormalizedNameIgnoreCase(String normalizedName);

    /** Best-effort match: masters whose normalized_name is contained in the given string (for MEDIUM confidence). */
    @Query(value = "SELECT * FROM master_product m WHERE LOWER(:norm) LIKE '%' || LOWER(m.normalized_name) || '%' ORDER BY LENGTH(m.normalized_name) DESC", nativeQuery = true)
    List<MasterProduct> findBestMatchByNormalizedNameContainedIn(@Param("norm") String normalizedName);
}
