package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.CustomerLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {

    List<CustomerLedger> findByCustomerId(Long customerId);

    List<CustomerLedger> findByCustomerIdAndShopIdOrderByCreatedAtDesc(Long customerId, Long shopId);

    List<CustomerLedger> findByShopId(Long shopId);
}

