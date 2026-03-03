package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Optional<PurchaseInvoice> findByShop_IdAndId(Long shopId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PurchaseInvoice p WHERE p.shop.id = :shopId AND p.id = :id")
    Optional<PurchaseInvoice> findByShop_IdAndIdForUpdate(@Param("shopId") Long shopId, @Param("id") Long id);

    Optional<PurchaseInvoice> findByShop_IdAndPurchaseUpload_Id(Long shopId, Long purchaseUploadId);
}
