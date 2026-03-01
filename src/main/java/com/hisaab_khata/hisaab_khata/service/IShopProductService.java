package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.productdto.CreateManualProductRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.PatchShopProductRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ShopProductResponse;

import java.util.List;

public interface IShopProductService {

    ShopProductResponse createManual(CreateManualProductRequest request);

    List<ShopProductResponse> listProducts();

    ShopProductResponse patchProduct(Long shopProductId, PatchShopProductRequest request);
}
