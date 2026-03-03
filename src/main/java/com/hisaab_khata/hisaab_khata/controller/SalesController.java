package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.sales.PostedSaleResponse;
import com.hisaab_khata.hisaab_khata.dto.sales.SalesDraftRequest;
import com.hisaab_khata.hisaab_khata.dto.sales.SalesDraftResponse;
import com.hisaab_khata.hisaab_khata.service.ISalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 5: Sales draft and post. Path base /sales (plural).
 */
@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final ISalesService salesService;

    @PostMapping("/draft")
    public ResponseEntity<ApiResponse<SalesDraftResponse>> createDraft(@RequestBody @Valid SalesDraftRequest request) {
        SalesDraftResponse response = salesService.createDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Draft created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesDraftResponse>> getById(@PathVariable Long id) {
        SalesDraftResponse response = salesService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<PostedSaleResponse>> post(@PathVariable Long id) {
        PostedSaleResponse response = salesService.post(id);
        return ResponseEntity.ok(ApiResponse.ok("Invoice posted", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesDraftResponse>>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<SalesDraftResponse> response = salesService.list(from, to);
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }
}
