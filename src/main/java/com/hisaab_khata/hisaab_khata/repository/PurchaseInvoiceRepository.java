package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Optional<PurchaseInvoice> findByShop_IdAndId(Long shopId, Long id);

    Optional<PurchaseInvoice> findByShop_IdAndPurchaseUpload_Id(Long shopId, Long purchaseUploadId);
}
