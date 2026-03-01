package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.*;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleItemRequest;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleResponse;
import com.hisaab_khata.hisaab_khata.enums.LedgerType;
import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.SaleMapper;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.ISaleService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleServiceImpl implements ISaleService {

    @Autowired
    private final SaleRepository saleRepository;

    @Autowired
    private final SaleItemRepository saleItemRepository;

    @Autowired
    private final PurchaseRepository purchaseRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ShopProductRepository shopProductRepository;

    @Autowired
    private final StockRepository stockRepository;

    @Autowired
    private final CustomerRepository customerRepository;

    @Autowired
    private final CustomerLedgerRepository customerLedgerRepository;

    private final SaleMapper saleMapper;
    private final ShopContext shopContext;

    @Override
    @Transactional
    public SaleResponse createSale(SaleCreateRequest req) {

        Long shopId = shopContext.getCurrentShopId();

        // -------------------
        // Validate Udhar
        // -------------------
        Customer customer = null;
        if (req.getUdharAmount() > 0) {
            if (req.getCustomerId() == null)
                throw new BusinessValidationException("Customer required for udhar", "UDHAR_CUSTOMER_REQUIRED");

            customer = customerRepository.findById(req.getCustomerId())
                    .filter(c -> Objects.equals(c.getShop().getId(), shopId))
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found", "CUSTOMER_NOT_FOUND"));
        }

        // -------------------
        // Build sale items + compute totalAmount
        // -------------------
        Double totalAmount = 0d;
        List<SaleItem> saleItems = new ArrayList<>();

        for (SaleItemRequest itemReq : req.getItems()) {
            if (itemReq.getShopProductId() == null) {
                throw new BusinessValidationException("Phase 3: shopProductId required in each sale item", "SHOP_PRODUCT_ID_REQUIRED");
            }
            ShopProduct shopProduct = shopProductRepository.findByShop_IdAndId(shopId, itemReq.getShopProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop product not found", "SHOP_PRODUCT_NOT_FOUND"));

            Double qtyBase = itemReq.getQuantity() * shopProduct.getConversionToBase().doubleValue();
            Double totalPrice = itemReq.getSellingPrice() * itemReq.getQuantity();
            totalAmount += totalPrice;

            SaleItem si = SaleItem.builder()
                    .shopId(shopId)
                    .shopProduct(shopProduct)
                    .quantity(itemReq.getQuantity())
                    .quantityBase(qtyBase)
                    .unit(itemReq.getUnit())
                    .sellingPrice(itemReq.getSellingPrice())
                    .totalPrice(totalPrice)
                    .build();

            saleItems.add(si);
        }

        // -------------------
        // Payment rule validation
        // -------------------
        Double expected = req.getCashPaid() + req.getUpiPaid() + req.getUdharAmount() - req.getRoundOff();
        if (!expected.equals(totalAmount)) {
            throw new BusinessValidationException(
                    "Payment split mismatch.",
                    "PAYMENT_SPLIT_INVALID"
            );
        }

        // -------------------
        // Determine payment mode
        // -------------------
        PaymentMode mode;
        boolean cash = req.getCashPaid() > 0;
        boolean upi = req.getUpiPaid() > 0;
        boolean udhar = req.getUdharAmount() > 0;

        int count = (cash ? 1 : 0) + (upi ? 1 : 0) + (udhar ? 1 : 0);

        if (count > 1) mode = PaymentMode.MIXED;
        else if (cash) mode = PaymentMode.CASH;
        else if (upi) mode = PaymentMode.UPI;
        else mode = PaymentMode.KHATA;

        // -------------------
        // Stock validation
        // -------------------
        for (SaleItem si : saleItems) {
            Stock stock = stockRepository.findByShopProduct_Id(si.getShopProduct().getId())
                    .orElseThrow(() -> new BusinessValidationException(
                            "Stock not found for: " + si.getShopProduct().getDisplayName(),
                            "STOCK_NOT_FOUND"));
            if (stock.getQuantity().compareTo(java.math.BigDecimal.valueOf(si.getQuantityBase())) < 0) {
                throw new BusinessValidationException(
                        "Insufficient stock for: " + si.getShopProduct().getDisplayName(),
                        "STOCK_INSUFFICIENT"
                );
            }
        }

        // -------------------
        // Compute profit per item
        // -------------------
        Double totalProfit = 0d;

        for (SaleItem si : saleItems) {
            Double costPrice = purchaseRepository
                    .findByShopIdAndShopProductId(shopId, si.getShopProduct().getId())
                    .stream()
                    .map(Purchase::getCostPrice)
                    .findFirst()
                    .orElse(0D);

            Double sellingPrice = si.getSellingPrice();
            Double quantity = si.getQuantity();
            Double profit = (sellingPrice - costPrice) * quantity;
            si.setProfit(profit);
            totalProfit = totalProfit + profit;
        }

        // -------------------
        // Create sale record
        // -------------------
        Sale sale = Sale.builder()
                .shopId(shopId)
                .customer(customer)
                .totalAmount(totalAmount)
                .cashPaid(req.getCashPaid())
                .upiPaid(req.getUpiPaid())
                .udharAmount(req.getUdharAmount())
                .roundOff(req.getRoundOff())
                .paymentMode(mode)
                .profit(totalProfit)
                .build();

        sale = saleRepository.save(sale);

        // -------------------
        // Save items & update stock
        // -------------------
        for (SaleItem si : saleItems) {
            Stock stock = stockRepository.findByShopProduct_Id(si.getShopProduct().getId())
                    .orElseThrow(() -> new BusinessValidationException("Stock not found", "STOCK_NOT_FOUND"));
            stock.setQuantity(stock.getQuantity().subtract(java.math.BigDecimal.valueOf(si.getQuantityBase())));
            stockRepository.save(stock);

            si.setSale(sale);
            saleItemRepository.save(si);
        }

        sale.setItems(saleItems);

        // -------------------
        // Ledger update (Udhar)
        // -------------------
        if (req.getUdharAmount() > 0) {
            CustomerLedger ledger = CustomerLedger.builder()
                    .shopId(shopId)
                    .customer(customer)
                    .type(LedgerType.UDHAR_DIYA)
                    .amount(req.getUdharAmount())
                    .build();

            customerLedgerRepository.save(ledger);
        }

        // -------------------
        // Return
        // -------------------
        return saleMapper.toResponse(sale);
    }

    @Override
    //@LogMethodParam(logResponse = true)
    public SaleResponse getSale(Long saleId) {

        Long shopId = shopContext.getCurrentShopId();

        Sale sale = saleRepository.findById(saleId)
                .filter(s -> Objects.equals(s.getShopId(), shopId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found",
                        "SALE_NOT_FOUND"
                ));

        return saleMapper.toResponse(sale);
    }

    @Override
    public List<SaleResponse> getAllSales() {
        return null;
    }


}


