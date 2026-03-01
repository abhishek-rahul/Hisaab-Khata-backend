package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IStockService {

    List<StockResponse> getAllStock();

    StockResponse getStockByShopProductId(Long shopProductId);

    void increaseStock(Long shopProductId, BigDecimal quantityInBaseUnit);

    void decreaseStock(Long shopProductId, BigDecimal quantityInBaseUnit);

    /** @deprecated Phase 3 uses shopProductId. Throws if called. */
    void increaseStockLegacy(Long productId, double quantityInBaseUnit, Long shopId);

    /** @deprecated Phase 3 uses shopProductId. Throws if called. */
    void decreaseStockLegacy(Long productId, double quantityInBaseUnit);
}
