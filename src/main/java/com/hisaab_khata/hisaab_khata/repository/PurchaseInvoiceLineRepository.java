package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseInvoiceLineRepository extends JpaRepository<PurchaseInvoiceLine, Long> {

    Optional<PurchaseInvoiceLine> findByPurchaseInvoice_IdAndId(Long purchaseInvoiceId, Long lineId);
}
