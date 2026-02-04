package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductUpdateRequest;
import com.hisaab_khata.hisaab_khata.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private final IProductService productService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ProductResponse>> create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse res = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<ProductResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse res = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                SuccessResponse.<ProductResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<ProductResponse>>> list() {
        List<ProductResponse> res = productService.getAllProducts();
        return ResponseEntity.ok(
                SuccessResponse.<List<ProductResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductResponse>> get(@PathVariable Long id) {
        ProductResponse res = productService.getProduct(id);
        return ResponseEntity.ok(
                SuccessResponse.<ProductResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}

