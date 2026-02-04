package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerLedgerResponse;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataPaymentRequest;
import com.hisaab_khata.hisaab_khata.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    @Autowired
    private final ICustomerService customerService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CustomerResponse>> create(
            @RequestBody CustomerCreateRequest request
    ) {
        CustomerResponse res = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<CustomerResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<CustomerResponse>>> list() {
        List<CustomerResponse> res = customerService.getAllCustomers();
        return ResponseEntity.ok(
                SuccessResponse.<List<CustomerResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{id}/ledger")
    public ResponseEntity<SuccessResponse<CustomerLedgerResponse>> ledger(
            @PathVariable Long id
    ) {
        CustomerLedgerResponse res = customerService.getCustomerLedger(id);
        return ResponseEntity.ok(
                SuccessResponse.<CustomerLedgerResponse>builder()
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
        customerService.recordKhataPayment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}

