package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.enums.CategoryScope;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ConflictException;
import com.hisaab_khata.hisaab_khata.mapper.CategoryMapper;
import com.hisaab_khata.hisaab_khata.repository.CategoryRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.ICategoryService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final CategoryMapper categoryMapper;
    private final ShopContext shopContext;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Long shopId = shopContext.getCurrentShopId();
        Shop shop = shopRepository.getReferenceById(shopId);
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isBlank()) {
            throw new BusinessValidationException("Category name is required", "CATEGORY_NAME_REQUIRED");
        }
        if (categoryRepository.existsByShop_IdAndScopeAndName(shopId, CategoryScope.SHOP, name)) {
            throw new ConflictException("Category with this name already exists", "CATEGORY_NAME_DUPLICATE");
        }
        Category category = Category.builder()
                .name(name)
                .shop(shop)
                .scope(CategoryScope.SHOP)
                .build();
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories(CategoryScope scope) {
        Long shopId = shopContext.getCurrentShopId();
        CategoryScope filter = scope != null ? scope : CategoryScope.SHOP;
        return categoryRepository.findByShop_IdAndScope(shopId, filter)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
