package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Product;
import com.hisaab_khata.hisaab_khata.domain.Purchase;
import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoice;
import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoiceLine;
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

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements IPurchaseService {

    @Autowired
    private final PurchaseRepository purchaseRepo;

    @Autowired
    private final PurchaseInvoiceRepository purchaseInvoiceRepo;

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
        stockService.increaseStockLegacy(product.getId(), qtyInBaseUnit, shopId);

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

        // Option A: serve from new flow (purchase_invoice) so GET /purchase/{id} works with draft-post id
        PurchaseInvoice invoice = purchaseInvoiceRepo.findByShop_IdAndId(shopId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase not found", "PURCHASE_NOT_FOUND"));

        return toPurchaseResponseFromInvoice(invoice);
    }

    /** Maps a posted purchase invoice to legacy PurchaseResponse shape for GET /purchase/{id}. */
    private static PurchaseResponse toPurchaseResponseFromInvoice(PurchaseInvoice invoice) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        PurchaseResponse.PurchaseResponseBuilder b = PurchaseResponse.builder()
                .id(invoice.getId())
                .createdAt(invoice.getPostedAt() != null
                        ? invoice.getPostedAt().format(fmt)
                        : (invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(fmt) : null));

        if (invoice.getSupplierParty() != null) {
            b.supplierId(invoice.getSupplierParty().getId());
            b.supplierName(invoice.getSupplierParty().getName());
        }
        b.paymentMode("CASH");

        List<PurchaseInvoiceLine> lines = invoice.getLines() != null
                ? invoice.getLines().stream().sorted(Comparator.comparing(PurchaseInvoiceLine::getLineNo)).toList()
                : List.of();
        if (!lines.isEmpty()) {
            PurchaseInvoiceLine first = lines.get(0);
            b.quantity(first.getQuantity() != null ? first.getQuantity().doubleValue() : null);
            b.unit(first.getUnit());
            b.costPrice(first.getUnitPrice() != null ? first.getUnitPrice().doubleValue() : null);
            if (first.getShopProduct() != null) {
                b.productId(first.getShopProduct().getId());
                b.productName(first.getShopProduct().getDisplayName() != null ? first.getShopProduct().getDisplayName() : first.getRawName());
            } else {
                b.productName(first.getRawName());
            }
        }
        return b.build();
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

