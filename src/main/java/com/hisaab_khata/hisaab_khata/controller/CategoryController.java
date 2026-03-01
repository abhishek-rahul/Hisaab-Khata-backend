package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.enums.CategoryScope;
import com.hisaab_khata.hisaab_khata.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Category created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(
            @RequestParam(required = false) CategoryScope scope) {
        List<CategoryResponse> response = categoryService.getAllCategories(scope);
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }
}

