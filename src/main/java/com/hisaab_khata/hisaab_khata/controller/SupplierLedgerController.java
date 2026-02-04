package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataPaymentRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierLedgerResponse;
import com.hisaab_khata.hisaab_khata.service.ISupplierLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierLedgerController {

    @Autowired
    private final ISupplierLedgerService ledgerService;

    @GetMapping("/{id}/ledger")
    public ResponseEntity<SuccessResponse<SupplierLedgerResponse>> ledger(
            @PathVariable Long id
    ) {
        SupplierLedgerResponse res = ledgerService.getSupplierLedger(id);
        return ResponseEntity.ok(
                SuccessResponse.<SupplierLedgerResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<SuccessResponse<Void>> payment(
            @PathVariable Long id,
            @RequestBody KhataPaymentRequest request
    ) {
        ledgerService.recordSupplierPayment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}

