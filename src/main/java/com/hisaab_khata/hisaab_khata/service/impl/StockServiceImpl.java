package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Product;
import com.hisaab_khata.hisaab_khata.domain.Stock;
import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import com.hisaab_khata.hisaab_khata.mapper.StockMapper;
import com.hisaab_khata.hisaab_khata.repository.ProductRepository;
import com.hisaab_khata.hisaab_khata.repository.StockRepository;
import com.hisaab_khata.hisaab_khata.service.IStockService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import com.hisaab_khata.hisaab_khata.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements IStockService {

    private final StockRepository stockRepository;

    private final ProductRepository productRepository;
    private final StockMapper stockMapper;
    private final ShopContext shopContext;
    private final ValidationUtil validationUtil;

    @Override
    public List<StockResponse> getAllStock() {
        Long shopId = shopContext.getCurrentShopId();

        return productRepository.findByShopId(shopId)
                .stream()
                .map(p -> stockRepository.findByProduct_Id(p.getId())
                        .orElse(Stock.builder()
                                .product(p)
                                .quantity(0D)
                                .build()))
                .map(stockMapper::toResponse)
                .toList();
    }

    @Override
    public StockResponse getStockByProductId(Long productId) {
        Long shopId = shopContext.getCurrentShopId();
        Product product = productRepository.getReferenceById(productId);

        validationUtil.validateShopOwnership(shopId, product);

        Stock stock = stockRepository.findByProduct_Id(productId)
                .orElse(Stock.builder().product(product).quantity(0D).build());

        return stockMapper.toResponse(stock);
    }

    @Override
    public void increaseStock(Long productId, Double qtyInBase, Long shopId) {
        Stock stock = stockRepository.findByProduct_Id(productId)
                .orElse(Stock.builder()
                        .product(productRepository.getReferenceById(productId))
                        .quantity(0D)
                        .shopId(shopId)
                        .build());

        stock.setQuantity(stock.getQuantity() + qtyInBase);
        stockRepository.save(stock);
    }

    @Override
    public void decreaseStock(Long productId, Double qtyInBase) {
        Stock stock = stockRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Insufficient stock"));

        if (stock.getQuantity() < qtyInBase) {
            throw new RuntimeException("Insufficient stock");
        }

        stock.setQuantity(stock.getQuantity() - qtyInBase);
        stockRepository.save(stock);
    }
}

