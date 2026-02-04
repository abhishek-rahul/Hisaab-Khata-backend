package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.SupplierLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierLedgerRepository extends JpaRepository<SupplierLedger, Long> {

    List<SupplierLedger> findBySupplierId(Long supplierId);
}

