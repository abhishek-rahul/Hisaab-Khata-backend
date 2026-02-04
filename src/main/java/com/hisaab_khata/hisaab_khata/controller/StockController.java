package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import com.hisaab_khata.hisaab_khata.service.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    @Autowired
    private final IStockService stockService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<StockResponse>>> allStock() {
        List<StockResponse> res = stockService.getAllStock();
        return ResponseEntity.ok(
                SuccessResponse.<List<StockResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<SuccessResponse<StockResponse>> getStock(
            @PathVariable Long productId
    ) {
        StockResponse res = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(
                SuccessResponse.<StockResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

