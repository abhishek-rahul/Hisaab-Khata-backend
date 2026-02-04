package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.domain.Product;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductUpdateRequest;
import com.hisaab_khata.hisaab_khata.mapper.ProductMapper;
import com.hisaab_khata.hisaab_khata.repository.CategoryRepository;
import com.hisaab_khata.hisaab_khata.repository.ProductRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.IProductService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import com.hisaab_khata.hisaab_khata.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ProductMapper productMapper;
    private final ShopContext shopContext;

    @Autowired
    private final ValidationUtil validationUtil;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        Long shopId = shopContext.getCurrentShopId();

        Shop shop = shopRepository.getReferenceById(shopId);
        Category category = categoryRepository.getReferenceById(request.getCategoryId());

        // Multi-tenancy validation
        validationUtil.validateShopOwnership(shopId, category);

        Product product = productMapper.toEntity(request);
        product.setShop(shop);
        product.setCategory(category);

        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Long shopId = shopContext.getCurrentShopId();

        Product product = productRepository.getReferenceById(id);
        validationUtil.validateShopOwnership(shopId, product);

        productMapper.updateEntity(request, product);
        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse getProduct(Long id) {
        Long shopId = shopContext.getCurrentShopId();

        Product product = productRepository.getReferenceById(id);
        validationUtil.validateShopOwnership(shopId, product);

        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        Long shopId = shopContext.getCurrentShopId();
        return productRepository.findByShopId(shopId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteProduct(Long id) {
        Long shopId = shopContext.getCurrentShopId();

        Product product = productRepository.getReferenceById(id);
        validationUtil.validateShopOwnership(shopId, product);

        productRepository.delete(product);
    }
}

