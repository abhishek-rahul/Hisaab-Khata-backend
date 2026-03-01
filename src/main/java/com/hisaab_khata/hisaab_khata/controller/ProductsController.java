package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.CreateManualProductRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.PatchShopProductRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ShopProductResponse;
import com.hisaab_khata.hisaab_khata.service.IShopProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 3: Category + MasterProduct + ShopProduct + Stock.
 * POST /products/manual, GET /products, PATCH /products/{shopProductId}.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final IShopProductService shopProductService;

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ShopProductResponse>> createManual(
            @Valid @RequestBody CreateManualProductRequest request) {
        ShopProductResponse res = shopProductService.createManual(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product created successfully", res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShopProductResponse>>> list() {
        List<ShopProductResponse> res = shopProductService.listProducts();
        return ResponseEntity.ok(ApiResponse.ok("OK", res));
    }

    @PatchMapping("/{shopProductId}")
    public ResponseEntity<ApiResponse<ShopProductResponse>> patch(
            @PathVariable Long shopProductId,
            @Valid @RequestBody PatchShopProductRequest request) {
        ShopProductResponse res = shopProductService.patchProduct(shopProductId, request);
        return ResponseEntity.ok(ApiResponse.ok("OK", res));
    }
}
