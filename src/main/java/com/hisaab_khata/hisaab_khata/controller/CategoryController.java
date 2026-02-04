package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    @Autowired
    private final ICategoryService categoryService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<CategoryResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<CategoryResponse>>> list() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(
                SuccessResponse.<List<CategoryResponse>>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }
}

