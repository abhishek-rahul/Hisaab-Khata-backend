package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.mapper.CategoryMapper;
import com.hisaab_khata.hisaab_khata.repository.CategoryRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.ICategoryService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final ShopRepository shopRepository;

    private final CategoryMapper categoryMapper;
    private final ShopContext shopContext;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Long shopId = shopContext.getCurrentShopId();
        Shop shop = shopRepository.getReferenceById(shopId);

        Category category = Category.builder()
                .name(request.getName())
                .shop(shop)
                .build();

        categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        Long shopId = shopContext.getCurrentShopId();
        return categoryRepository.findByShopId(shopId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
