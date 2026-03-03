package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.SalesInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesInvoiceLineRepository extends JpaRepository<SalesInvoiceLine, Long> {

    List<SalesInvoiceLine> findBySalesInvoice_IdOrderByLineNoAsc(Long salesInvoiceId);
}
