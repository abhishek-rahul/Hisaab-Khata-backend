package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;

import java.util.List;

public interface IStockService {

    List<StockResponse> getAllStock();

    StockResponse getStockByProductId(Long productId);

    void increaseStock(Long productId, Double quantityInBaseUnit, Long shopId);

    void decreaseStock(Long productId, Double quantityInBaseUnit);
}
