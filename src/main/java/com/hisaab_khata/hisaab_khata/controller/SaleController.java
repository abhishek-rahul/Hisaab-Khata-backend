package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleResponse;
import com.hisaab_khata.hisaab_khata.service.ISaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleController {

    @Autowired
    private final ISaleService saleService;

    @PostMapping
    public ResponseEntity<SuccessResponse<SaleResponse>> create(
            @RequestBody SaleCreateRequest request
    ) {
        SaleResponse res = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<SaleResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<SaleResponse>>> list() {
        List<SaleResponse> res = saleService.getAllSales();
        return ResponseEntity.ok(
                SuccessResponse.<List<SaleResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<SaleResponse>> get(
            @PathVariable Long id
    ) {
        SaleResponse res = saleService.getSale(id);
        return ResponseEntity.ok(
                SuccessResponse.<SaleResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

