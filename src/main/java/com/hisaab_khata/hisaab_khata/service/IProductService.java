package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.productdto.ProductCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductUpdateRequest;

import java.util.List;

public interface IProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    ProductResponse getProduct(Long id);

    List<ProductResponse> getAllProducts();

    void deleteProduct(Long id);
}

