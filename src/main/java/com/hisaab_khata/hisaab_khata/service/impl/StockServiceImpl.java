package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.Stock;
import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.StockMapper;
import com.hisaab_khata.hisaab_khata.repository.ShopProductRepository;
import com.hisaab_khata.hisaab_khata.repository.StockRepository;
import com.hisaab_khata.hisaab_khata.service.IStockService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements IStockService {

    private final StockRepository stockRepository;
    private final ShopProductRepository shopProductRepository;
    private final StockMapper stockMapper;
    private final ShopContext shopContext;

    @Override
    public List<StockResponse> getAllStock() {
        Long shopId = shopContext.getCurrentShopId();
        return stockRepository.findByShopProduct_Shop_Id(shopId)
                .stream()
                .map(stockMapper::toResponse)
                .toList();
    }

    @Override
    public StockResponse getStockByShopProductId(Long shopProductId) {
        Long shopId = shopContext.getCurrentShopId();
        shopProductRepository.findByShop_IdAndId(shopId, shopProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop product not found", "SHOP_PRODUCT_NOT_FOUND"));
        Stock stock = stockRepository.findByShopProduct_Id(shopProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found", "STOCK_NOT_FOUND"));
        return stockMapper.toResponse(stock);
    }

    @Override
    public void increaseStock(Long shopProductId, BigDecimal qtyInBase) {
        Stock stock = stockRepository.findByShopProduct_Id(shopProductId)
                .orElseGet(() -> {
                    var sp = shopProductRepository.getReferenceById(shopProductId);
                    return Stock.builder().shopProduct(sp).quantity(BigDecimal.ZERO).build();
                });
        stock.setQuantity(stock.getQuantity().add(qtyInBase));
        stockRepository.save(stock);
    }

    @Override
    public void decreaseStock(Long shopProductId, BigDecimal qtyInBase) {
        Stock stock = stockRepository.findByShopProduct_Id(shopProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found", "STOCK_NOT_FOUND"));
        stock.setQuantity(stock.getQuantity().subtract(qtyInBase));
        stockRepository.save(stock);
    }

    @Override
    @Deprecated
    public void increaseStockLegacy(Long productId, double quantityInBaseUnit, Long shopId) {
        throw new UnsupportedOperationException("Phase 3: use shopProductId and increaseStock(shopProductId, qty)");
    }

    @Override
    @Deprecated
    public void decreaseStockLegacy(Long productId, double quantityInBaseUnit) {
        throw new UnsupportedOperationException("Phase 3: use shopProductId and decreaseStock(shopProductId, qty)");
    }
}

