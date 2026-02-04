package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Product;
import com.hisaab_khata.hisaab_khata.domain.Purchase;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.domain.Supplier;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseResponse;
import com.hisaab_khata.hisaab_khata.exception.AccessDeniedException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.PurchaseMapper;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.IPurchaseService;
import com.hisaab_khata.hisaab_khata.service.IStockService;
import com.hisaab_khata.hisaab_khata.service.ISupplierLedgerService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import com.hisaab_khata.hisaab_khata.util.UnitConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements IPurchaseService {

    @Autowired
    private final PurchaseRepository purchaseRepo;

    @Autowired
    private final ProductRepository productRepo;

    @Autowired
    private final SupplierRepository supplierRepo;

    @Autowired
    private final ShopRepository shopRepo;
    
    private final PurchaseMapper mapper;

    @Autowired
    private final IStockService stockService;

    @Autowired
    private final ISupplierLedgerService supplierLedgerService;
    private final UnitConverter converter;
    private final ShopContext shopContext;

    @Override
    @Transactional
    public PurchaseResponse createPurchase(PurchaseCreateRequest req) {

        Long shopId = shopContext.getCurrentShopId();

        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shop not found", "SHOP_NOT_FOUND"));

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found", "PRODUCT_NOT_FOUND"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Product not in your shop", "ACCESS_DENIED");
        }

        Supplier supplier = null;
        if (req.getSupplierId() != null) {
            supplier = supplierRepo.findById(req.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found", "SUPPLIER_NOT_FOUND"));
            if (!supplier.getShop().getId().equals(shopId)) {
                throw new AccessDeniedException("Supplier not in your shop", "ACCESS_DENIED");
            }
        }

        Purchase purchase = mapper.toEntity(req);
        purchase.setShop(shop);
        purchase.setProduct(product);
        purchase.setSupplier(supplier);

        purchaseRepo.save(purchase);

        // STOCK UPDATE
        double qtyInBaseUnit = converter.toBaseUnit(
                req.getQuantity(),
                req.getUnit(),
                product
        );
        stockService.increaseStock(product.getId(),  qtyInBaseUnit, shopId);

        // LEDGER ENTRY FOR CREDIT PURCHASE
        if ("CREDIT".equalsIgnoreCase(req.getPaymentMode()) && supplier != null) {
            double total = req.getCostPrice() * req.getQuantity();
            supplierLedgerService.recordSupplierUdhar(
                    supplier.getId(),
                    total,
                    purchase.getId()
            );
        }

        return mapper.toResponse(purchase);
    }

    @Override
    public PurchaseResponse getPurchase(Long id) {
        Long shopId = shopContext.getCurrentShopId();

        Purchase p = purchaseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase not found", "PURCHASE_NOT_FOUND"));

        if (!p.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Purchase not in your shop", "ACCESS_DENIED");
        }

        return mapper.toResponse(p);
    }

    @Override
    public List<PurchaseResponse> getAllPurchases() {
        Long shopId = shopContext.getCurrentShopId();
        return purchaseRepo.findByShopId(shopId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updatePurchase(Long id, Long newSupplierId) {

        Long shopId = shopContext.getCurrentShopId();

        Purchase purchase = purchaseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase not found", "PURCHASE_NOT_FOUND"));

        if (!purchase.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Purchase not in your shop", "ACCESS_DENIED");
        }

        Supplier supplier = supplierRepo.findById(newSupplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found", "SUPPLIER_NOT_FOUND"));

        if (!supplier.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Supplier not in your shop", "ACCESS_DENIED");
        }

        purchase.setSupplier(supplier);
        purchaseRepo.save(purchase);
    }
}

