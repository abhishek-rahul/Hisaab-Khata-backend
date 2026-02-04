package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByShopId(Long shopId);

    List<Customer> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name);
}

