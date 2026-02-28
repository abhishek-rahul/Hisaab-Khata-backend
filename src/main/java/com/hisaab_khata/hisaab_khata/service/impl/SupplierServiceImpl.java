package com.hisaab_khata.hisaab_khata.service.impl;



import com.hisaab_khata.hisaab_khata.domain.Supplier;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierResponse;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
//import com.hisaab_khata.hisaab_khata.repository.PurchaseRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
//import com.hisaab_khata.hisaab_khata.repository.SupplierLedgerRepository;
import com.hisaab_khata.hisaab_khata.repository.SupplierRepository;
import com.hisaab_khata.hisaab_khata.service.ISupplierService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements ISupplierService {

    private final SupplierRepository supplierRepository;
    //private final SupplierLedgerRepository supplierLedgerRepository;
    //private final PurchaseRepository purchaseRepository;
    private final ShopContext shopContext;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierCreateRequest req) {

        Long shopId = shopContext.getCurrentShopId();



        // Check duplicate supplier
        supplierRepository.findByShop_IdAndName(shopId, req.getName())
                .ifPresent(s -> {
                    throw new BusinessValidationException(
                            "Supplier already exists",
                            "SUPPLIER_EXISTS"
                    );
                });

        Supplier supplier = Supplier.builder()
                .shop(shopRepository.getReferenceById(shopId))
                .name(req.getName())
                .phone(req.getPhone())
                .build();

        supplierRepository.save(supplier);

        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .phone(supplier.getPhone())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {

        Long shopId = shopContext.getCurrentShopId();

        return supplierRepository.findByShop_Id(shopId)
                .stream()
                .map(s -> SupplierResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .phone(s.getPhone())
                        .build())
                .collect(Collectors.toList());
    }

/*    @Override
    @Transactional(readOnly = true)
    public SupplierLedgerResponse getLedger(Long supplierId) {

        Long shopId = shopContext.getCurrentShopId();

        Supplier supplier = supplierRepository.findById(supplierId)
                .filter(s -> Objects.equals(s.getShopId(), shopId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found",
                        "SUPPLIER_NOT_FOUND"
                ));

        List<SupplierLedger> entries =
                supplierLedgerRepository.findBySupplierIdAndShopIdOrderByCreatedAtDesc(
                        supplierId, shopId
                );

        double totalGiven = entries.stream()
                .filter(e -> e.getType() == SupplierLedgerType.AMOUNT_GIVEN)
                .mapToDouble(SupplierLedger::getAmount)
                .sum();

        double totalPaid = entries.stream()
                .filter(e -> e.getType() == SupplierLedgerType.PAYMENT_RECEIVED)
                .mapToDouble(SupplierLedger::getAmount)
                .sum();

        double pending = totalGiven - totalPaid;

        return SupplierLedgerResponse.builder()
                .supplierId(supplier.getId())
                .supplierName(supplier.getName())
                .pendingAmount(pending)
                .entries(
                        entries.stream()
                                .map(e -> SupplierLedgerEntryDto.builder()
                                        .id(e.getId())
                                        .type(e.getType())
                                        .amount(e.getAmount())
                                        .note(e.getNote())
                                        .date(e.getCreatedAt())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    @Transactional
    public SupplierPaymentResponse recordPayment(Long supplierId, SupplierPaymentRequest req) {

        Long shopId = shopContext.getCurrentShopId();

        Supplier supplier = supplierRepository.findById(supplierId)
                .filter(s -> Objects.equals(s.getShopId(), shopId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found",
                        "SUPPLIER_NOT_FOUND"
                ));

        // Add ledger entry
        SupplierLedger ledger = SupplierLedger.builder()
                .shopId(shopId)
                .supplier(supplier)
                .type(SupplierLedgerType.PAYMENT_RECEIVED)
                .amount(req.getAmount())
                .note(req.getNote())
                .build();

        supplierLedgerRepository.save(ledger);

        // Recalculate pending amount
        List<SupplierLedger> entries =
                supplierLedgerRepository.findBySupplierIdAndShopIdOrderByCreatedAtDesc(supplierId, shopId);

        double totalGiven = entries.stream()
                .filter(e -> e.getType() == SupplierLedgerType.AMOUNT_GIVEN)
                .mapToDouble(SupplierLedger::getAmount)
                .sum();

        double totalPaid = entries.stream()
                .filter(e -> e.getType() == SupplierLedgerType.PAYMENT_RECEIVED)
                .mapToDouble(SupplierLedger::getAmount)
                .sum();

        double pending = totalGiven - totalPaid;

        return SupplierPaymentResponse.builder()
                .supplierId(supplierId)
                .amountPaid(req.getAmount())
                .pending(pending)
                .build();
    }*/
}

