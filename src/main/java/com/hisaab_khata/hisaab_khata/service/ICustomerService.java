package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerLedgerResponse;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataPaymentRequest;
import com.hisaab_khata.hisaab_khata.dto.khatadto.PendingKhataResponse;

import java.util.List;

public interface ICustomerService {

    CustomerResponse createCustomer(CustomerCreateRequest request);

    List<CustomerResponse> getAllCustomers();

    CustomerLedgerResponse getCustomerLedger(Long customerId);

    void recordKhataPayment(Long customerId, KhataPaymentRequest request);

    List<PendingKhataResponse> getPendingKhataSummary();
}

