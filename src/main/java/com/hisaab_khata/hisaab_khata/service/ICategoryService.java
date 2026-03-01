package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.enums.CategoryScope;

import java.util.List;

public interface ICategoryService {

    CategoryResponse createCategory(CategoryCreateRequest request);

    List<CategoryResponse> getAllCategories(CategoryScope scope);
}

