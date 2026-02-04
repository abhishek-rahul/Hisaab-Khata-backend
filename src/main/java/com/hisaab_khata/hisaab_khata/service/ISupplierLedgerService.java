package com.hisaab_khata.hisaab_khata.service;


import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataPaymentRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierLedgerResponse;

public interface ISupplierLedgerService {

    SupplierLedgerResponse getSupplierLedger(Long supplierId);

    void recordSupplierPayment(Long supplierId, KhataPaymentRequest request);

    void recordSupplierUdhar(Long supplierId, Double amount, Long purchaseId);
}

