package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Purchase;
import com.hisaab_khata.hisaab_khata.domain.Supplier;
import com.hisaab_khata.hisaab_khata.domain.SupplierLedger;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataPaymentRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierLedgerResponse;
import com.hisaab_khata.hisaab_khata.enums.LedgerType;
import com.hisaab_khata.hisaab_khata.exception.AccessDeniedException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.SupplierLedgerMapper;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.repository.SupplierLedgerRepository;
import com.hisaab_khata.hisaab_khata.repository.SupplierRepository;
import com.hisaab_khata.hisaab_khata.service.ISupplierLedgerService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
//import com.hisaab_khata.hisaab_khata.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierLedgerServiceImpl implements ISupplierLedgerService {

    @Autowired
    private final SupplierLedgerRepository ledgerRepo;

    @Autowired
    private final SupplierRepository supplierRepo;

    @Autowired
    private final ShopRepository shopRepo;
    private final SupplierLedgerMapper mapper;
    private final ShopContext shopContext;

    @Override
    public SupplierLedgerResponse getSupplierLedger(Long supplierId) {

        Long shopId = shopContext.getCurrentShopId();

        Supplier supplier = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found", "SUPPLIER_NOT_FOUND"));

        if (!supplier.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Supplier not in your shop", "ACCESS_DENIED");
        }

        List<SupplierLedger> entries = ledgerRepo.findBySupplierId(supplierId);

        double udhar = entries.stream()
                .filter(e -> e.getType() == LedgerType.UDHAR_LIYA)
                .mapToDouble(SupplierLedger::getAmount)
                .sum();

        double paid = entries.stream()
                .filter(e -> e.getType() == LedgerType.JAMA_KIYA)
                .mapToDouble(SupplierLedger::getAmount)
                .sum();

        return SupplierLedgerResponse.builder()
                .supplierId(supplierId)
                .supplierName(supplier.getName())
                .totalUdhar(udhar)
                .totalPaid(paid)
                .balance(udhar - paid)
                .transactions(
                        entries.stream()
                                .map(mapper::toEntry)
                                .toList()
                )
                .build();
    }

    @Override
    public void recordSupplierPayment(Long supplierId, KhataPaymentRequest req) {

        Long shopId = shopContext.getCurrentShopId();

        Supplier supplier = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found", "SUPPLIER_NOT_FOUND"));

        if (!supplier.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Supplier not in your shop", "ACCESS_DENIED");
        }

        SupplierLedger entry = SupplierLedger.builder()
                .supplier(supplier)
                .type(LedgerType.JAMA_KIYA)
                .amount(req.getAmount())
                .build();

        ledgerRepo.save(entry);
    }

    @Override
    public void recordSupplierUdhar(Long supplierId, Double amount, Long purchaseId) {

        Supplier supplier = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found", "SUPPLIER_NOT_FOUND"));

        SupplierLedger entry = SupplierLedger.builder()
                .supplier(supplier)
                .type(LedgerType.UDHAR_LIYA)
                .amount(amount)
                .purchase(purchaseId != null ? Purchase.builder().id(purchaseId).build() : null)
                .build();

        ledgerRepo.save(entry);
    }
}


