package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseResponse;
import com.hisaab_khata.hisaab_khata.service.IPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final IPurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<SuccessResponse<PurchaseResponse>> create(
            @RequestBody PurchaseCreateRequest request
    ) {
        PurchaseResponse res = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<PurchaseResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<PurchaseResponse>>> list() {
        List<PurchaseResponse> res = purchaseService.getAllPurchases();
        return ResponseEntity.ok(
                SuccessResponse.<List<PurchaseResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> get(
            @PathVariable Long id
    ) {
        PurchaseResponse res = purchaseService.getPurchase(id);
        return ResponseEntity.ok(
                SuccessResponse.<PurchaseResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> update(
            @PathVariable Long id,
            @RequestParam Long supplierId
    ) {
        purchaseService.updatePurchase(id, supplierId);
        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}

