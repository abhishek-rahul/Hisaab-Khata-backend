package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import com.hisaab_khata.hisaab_khata.service.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final IStockService stockService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockResponse>>> list() {
        List<StockResponse> res = stockService.getAllStock();
        return ResponseEntity.ok(ApiResponse.ok("OK", res));
    }

    @GetMapping("/{shopProductId}")
    public ResponseEntity<ApiResponse<StockResponse>> getByShopProduct(
            @PathVariable Long shopProductId) {
        StockResponse res = stockService.getStockByShopProductId(shopProductId);
        return ResponseEntity.ok(ApiResponse.ok("OK", res));
    }
}

