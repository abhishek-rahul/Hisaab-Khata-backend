package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.*;
import com.hisaab_khata.hisaab_khata.dto.productdto.CreateManualProductRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.PatchShopProductRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ShopProductResponse;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.IShopProductService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopProductServiceImpl implements IShopProductService {

    private final ShopContext shopContext;
    private final ShopRepository shopRepository;
    private final MasterProductRepository masterProductRepository;
    private final ShopProductRepository shopProductRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional
    public ShopProductResponse createManual(CreateManualProductRequest request) {
        Long shopId = shopContext.getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found", "SHOP_NOT_FOUND"));

        MasterProduct master = masterProductRepository.findByNormalizedNameIgnoreCase(request.getNormalizedName().trim())
                .orElseGet(() -> {
                    MasterProduct m = MasterProduct.builder()
                            .canonicalName(request.getCanonicalName().trim())
                            .normalizedName(request.getNormalizedName().trim().toLowerCase())
                            .baseUnit(request.getBaseUnit())
                            .build();
                    return masterProductRepository.save(m);
                });

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getShop() != null && c.getShop().getId().equals(shopId))
                    .orElse(null);
        }

        BigDecimal minStock = request.getMinStockInBase() != null ? request.getMinStockInBase() : BigDecimal.ZERO;

        ShopProduct shopProduct = ShopProduct.builder()
                .shop(shop)
                .masterProduct(master)
                .category(category)
                .displayName(request.getDisplayName().trim())
                .displayUnit(request.getDisplayUnit().trim())
                .conversionToBase(request.getConversionToBase())
                .sellingPrice(request.getSellingPrice())
                .minStockInBase(minStock)
                .isActive(true)
                .build();
        shopProduct = shopProductRepository.save(shopProduct);

        Stock stock = Stock.builder()
                .shopProduct(shopProduct)
                .quantity(BigDecimal.ZERO)
                .build();
        stockRepository.save(stock);

        return toResponse(shopProduct, BigDecimal.ZERO);
    }

    @Override
    public List<ShopProductResponse> listProducts() {
        Long shopId = shopContext.getCurrentShopId();
        List<ShopProduct> list = shopProductRepository.findByShop_Id(shopId);
        List<Stock> stocks = stockRepository.findByShopProduct_Shop_Id(shopId);
        return list.stream()
                .map(sp -> {
                    BigDecimal qty = stocks.stream()
                            .filter(s -> s.getShopProduct().getId().equals(sp.getId()))
                            .findFirst()
                            .map(Stock::getQuantity)
                            .orElse(BigDecimal.ZERO);
                    return toResponse(sp, qty);
                })
                .toList();
    }

    @Override
    public ShopProductResponse patchProduct(Long shopProductId, PatchShopProductRequest request) {
        Long shopId = shopContext.getCurrentShopId();
        ShopProduct sp = shopProductRepository.findByShop_IdAndId(shopId, shopProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop product not found", "SHOP_PRODUCT_NOT_FOUND"));
        if (request.getSellingPrice() != null) {
            sp.setSellingPrice(request.getSellingPrice());
        }
        if (request.getMinStockInBase() != null) {
            sp.setMinStockInBase(request.getMinStockInBase());
        }
        if (request.getIsActive() != null) {
            sp.setIsActive(request.getIsActive());
        }
        sp = shopProductRepository.save(sp);
        BigDecimal qty = stockRepository.findByShopProduct_Id(sp.getId())
                .map(Stock::getQuantity)
                .orElse(BigDecimal.ZERO);
        return toResponse(sp, qty);
    }

    private ShopProductResponse toResponse(ShopProduct sp, BigDecimal stockQty) {
        return ShopProductResponse.builder()
                .id(sp.getId())
                .masterProductId(sp.getMasterProduct().getId())
                .canonicalName(sp.getMasterProduct().getCanonicalName())
                .normalizedName(sp.getMasterProduct().getNormalizedName())
                .baseUnit(sp.getMasterProduct().getBaseUnit())
                .categoryId(sp.getCategory() != null ? sp.getCategory().getId() : null)
                .categoryName(sp.getCategory() != null ? sp.getCategory().getName() : null)
                .displayName(sp.getDisplayName())
                .displayUnit(sp.getDisplayUnit())
                .conversionToBase(sp.getConversionToBase())
                .sellingPrice(sp.getSellingPrice())
                .minStockInBase(sp.getMinStockInBase())
                .isActive(sp.getIsActive())
                .stockQty(stockQty)
                .build();
    }
}
