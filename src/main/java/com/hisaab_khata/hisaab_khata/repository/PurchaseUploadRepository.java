package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.PurchaseUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseUploadRepository extends JpaRepository<PurchaseUpload, Long> {

    Optional<PurchaseUpload> findByShop_IdAndId(Long shopId, Long id);
}
