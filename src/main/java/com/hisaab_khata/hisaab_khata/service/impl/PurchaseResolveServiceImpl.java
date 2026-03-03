package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.*;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftReviewResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.ResolveLineRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.ReviewLineResponse;
import com.hisaab_khata.hisaab_khata.enums.DocStatus;
import com.hisaab_khata.hisaab_khata.enums.MappingSource;
import com.hisaab_khata.hisaab_khata.enums.ResolutionStatus;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.IPurchaseResolveService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseResolveServiceImpl implements IPurchaseResolveService {

    private static final BigDecimal THRESHOLD_HIGH = new BigDecimal("0.92");
    private static final BigDecimal THRESHOLD_LOW = new BigDecimal("0.60");

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final SupplierProductMappingRepository supplierProductMappingRepository;
    private final MasterProductRepository masterProductRepository;
    private final ShopProductRepository shopProductRepository;
    private final ShopRepository shopRepository;
    private final PartyRepository partyRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final ShopContext shopContext;

    @Override
    @Transactional
    public DraftReviewResponse runAutoResolve(Long draftId) {
        Long shopId = shopContext.getCurrentShopId();
        PurchaseInvoice draft = purchaseInvoiceRepository.findByShop_IdAndId(shopId, draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found", "DRAFT_NOT_FOUND"));
        if (draft.getStatus() != DocStatus.DRAFT) {
            throw new BusinessValidationException("Draft must be in DRAFT status", "DRAFT_NOT_EDITABLE");
        }
        if (draft.getSupplierParty() == null) {
            throw new BusinessValidationException("Supplier must be set before resolve", "SUPPLIER_REQUIRED");
        }
        Long supplierPartyId = draft.getSupplierParty().getId();

        List<PurchaseInvoiceLine> lines = draft.getLines().stream()
                .sorted(Comparator.comparing(PurchaseInvoiceLine::getLineNo))
                .toList();

        for (PurchaseInvoiceLine line : lines) {
            line.setSuggestedMasterProduct(null);
            line.setSuggestedConfidence(null);
            // 1) Exact mapping lookup
            Optional<SupplierProductMapping> mapping = supplierProductMappingRepository
                    .findByShop_IdAndSupplierParty_IdAndNormalizedName(shopId, supplierPartyId, line.getNormalizedName());
            if (mapping.isPresent()) {
                line.setShopProduct(mapping.get().getShopProduct());
                line.setResolutionStatus(ResolutionStatus.RESOLVED);
                continue;
            }
            // 2) Master product match with confidence
            Optional<MasterProduct> exact = masterProductRepository.findByNormalizedNameIgnoreCase(line.getNormalizedName());
            if (exact.isPresent()) {
                BigDecimal conf = BigDecimal.ONE;
                if (conf.compareTo(THRESHOLD_HIGH) >= 0) {
                    ShopProduct sp = ensureShopProductForMaster(shopId, exact.get().getId(), null);
                    line.setShopProduct(sp);
                    line.setResolutionStatus(ResolutionStatus.RESOLVED);
                    upsertMapping(shopId, supplierPartyId, line.getNormalizedName(), sp.getId(), MappingSource.SYSTEM, conf);
                } else {
                    line.setSuggestedMasterProduct(exact.get());
                    line.setSuggestedConfidence(conf);
                }
                continue;
            }
            List<MasterProduct> containing = masterProductRepository.findBestMatchByNormalizedNameContainedIn(line.getNormalizedName());
            if (!containing.isEmpty()) {
                MasterProduct best = containing.get(0);
                BigDecimal conf = confidenceForContaining(line.getNormalizedName(), best.getNormalizedName());
                if (conf.compareTo(THRESHOLD_HIGH) >= 0) {
                    ShopProduct sp = ensureShopProductForMaster(shopId, best.getId(), null);
                    line.setShopProduct(sp);
                    line.setResolutionStatus(ResolutionStatus.RESOLVED);
                    upsertMapping(shopId, supplierPartyId, line.getNormalizedName(), sp.getId(), MappingSource.SYSTEM, conf);
                } else if (conf.compareTo(THRESHOLD_LOW) >= 0) {
                    line.setSuggestedMasterProduct(best);
                    line.setSuggestedConfidence(conf);
                }
            }
        }
        purchaseInvoiceRepository.save(draft);
        return toReviewResponse(draft);
    }

    @Override
    public DraftReviewResponse getReview(Long draftId) {
        Long shopId = shopContext.getCurrentShopId();
        PurchaseInvoice draft = purchaseInvoiceRepository.findByShop_IdAndId(shopId, draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found", "DRAFT_NOT_FOUND"));
        return toReviewResponse(draft);
    }

    @Override
    @Transactional
    public DraftReviewResponse resolveLine(Long draftId, Long lineId, ResolveLineRequest request) {
        Long shopId = shopContext.getCurrentShopId();
        PurchaseInvoice draft = purchaseInvoiceRepository.findByShop_IdAndId(shopId, draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found", "DRAFT_NOT_FOUND"));
        if (draft.getStatus() != DocStatus.DRAFT) {
            throw new BusinessValidationException("Draft must be in DRAFT status", "DRAFT_NOT_EDITABLE");
        }
        if (draft.getSupplierParty() == null) {
            throw new BusinessValidationException("Supplier must be set", "SUPPLIER_REQUIRED");
        }
        PurchaseInvoiceLine line = purchaseInvoiceLineRepository.findByPurchaseInvoice_IdAndId(draft.getId(), lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Line not found", "LINE_NOT_FOUND"));

        ShopProduct resolved = null;
        BigDecimal confidence = new BigDecimal("1.00");

        switch (request.getAction()) {
            case ACCEPT_SUGGESTION -> {
                if (line.getSuggestedMasterProduct() == null) {
                    throw new BusinessValidationException("No suggestion to accept", "NO_SUGGESTION");
                }
                resolved = ensureShopProductForMaster(shopId, line.getSuggestedMasterProduct().getId(), null);
                confidence = line.getSuggestedConfidence() != null ? line.getSuggestedConfidence() : new BigDecimal("0.95");
            }
            case CHOOSE_EXISTING -> {
                if (request.getShopProductId() == null) {
                    throw new BusinessValidationException("shopProductId required", "VALIDATION_FAILED");
                }
                resolved = shopProductRepository.findByShop_IdAndId(shopId, request.getShopProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Shop product not found", "SHOP_PRODUCT_NOT_FOUND"));
            }
            case CREATE_NEW_PRODUCT -> {
                if (request.getCanonicalName() == null || request.getBaseUnit() == null) {
                    throw new BusinessValidationException("canonicalName and baseUnit required", "VALIDATION_FAILED");
                }
                String norm = request.getCanonicalName().trim().toLowerCase().replaceAll("\\s+", " ");
                MasterProduct master = masterProductRepository.findByNormalizedNameIgnoreCase(norm)
                        .orElseGet(() -> masterProductRepository.save(MasterProduct.builder()
                                .canonicalName(request.getCanonicalName().trim())
                                .normalizedName(norm)
                                .baseUnit(request.getBaseUnit())
                                .build()));
                Long categoryId = request.getCategoryId();
                if (categoryId != null) {
                    categoryRepository.findByShop_IdAndId(shopId, categoryId)
                            .orElseThrow(() -> new BusinessValidationException("Category not found", "CATEGORY_NOT_FOUND"));
                }
                resolved = ensureShopProductForMaster(shopId, master.getId(), categoryId);
            }
        }

        if (resolved != null) {
            line.setShopProduct(resolved);
            line.setResolutionStatus(ResolutionStatus.RESOLVED);
            line.setSuggestedMasterProduct(null);
            line.setSuggestedConfidence(null);
            upsertMapping(shopId, draft.getSupplierParty().getId(), line.getNormalizedName(), resolved.getId(), MappingSource.USER, confidence);
        }
        purchaseInvoiceRepository.save(draft);
        return toReviewResponse(draft);
    }

    private ShopProduct ensureShopProductForMaster(Long shopId, Long masterProductId, Long categoryId) {
        Shop shop = shopRepository.getReferenceById(shopId);
        MasterProduct master = masterProductRepository.getReferenceById(masterProductId);
        Optional<ShopProduct> existing = shopProductRepository.findByShop_IdAndMasterProduct_Id(shopId, masterProductId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findByShop_IdAndId(shopId, categoryId).orElse(null);
        }
        String displayUnit = master.getBaseUnit().name();
        ShopProduct sp = ShopProduct.builder()
                .shop(shop)
                .masterProduct(master)
                .category(category)
                .displayName(master.getCanonicalName())
                .displayUnit(displayUnit)
                .conversionToBase(BigDecimal.ONE)
                .minStockInBase(BigDecimal.ZERO)
                .isActive(true)
                .build();
        sp = shopProductRepository.save(sp);
        if (stockRepository.findByShopProduct_Id(sp.getId()).isEmpty()) {
            stockRepository.save(Stock.builder().shopProduct(sp).quantity(BigDecimal.ZERO).build());
        }
        return sp;
    }

    private void upsertMapping(Long shopId, Long supplierPartyId, String normalizedName, Long shopProductId, MappingSource source, BigDecimal confidence) {
        Shop shop = shopRepository.getReferenceById(shopId);
        Party party = partyRepository.getReferenceById(supplierPartyId);
        ShopProduct sp = shopProductRepository.getReferenceById(shopProductId);
        Optional<SupplierProductMapping> existing = supplierProductMappingRepository
                .findByShop_IdAndSupplierParty_IdAndNormalizedName(shopId, supplierPartyId, normalizedName);
        SupplierProductMapping m;
        if (existing.isPresent()) {
            m = existing.get();
            m.setShopProduct(sp);
            m.setSource(source);
            m.setConfidenceScore(confidence);
        } else {
            m = SupplierProductMapping.builder()
                    .shop(shop)
                    .supplierParty(party)
                    .normalizedName(normalizedName)
                    .shopProduct(sp)
                    .source(source)
                    .confidenceScore(confidence)
                    .build();
        }
        supplierProductMappingRepository.save(m);
    }

    private BigDecimal confidenceForContaining(String lineNorm, String masterNorm) {
        if (masterNorm == null || masterNorm.isEmpty()) return THRESHOLD_LOW;
        if (lineNorm == null) return BigDecimal.ZERO;
        String ln = lineNorm.toLowerCase();
        String mn = masterNorm.toLowerCase();
        if (ln.equals(mn)) return BigDecimal.ONE;
        if (ln.contains(mn)) {
            return BigDecimal.valueOf(mn.length()).divide(BigDecimal.valueOf(Math.max(ln.length(), 1)), 4, RoundingMode.HALF_UP);
        }
        return THRESHOLD_LOW;
    }

    private DraftReviewResponse toReviewResponse(PurchaseInvoice draft) {
        List<ReviewLineResponse> lineResponses = new ArrayList<>();
        boolean anyUnresolved = false;
        boolean allResolved = true;
        for (PurchaseInvoiceLine line : draft.getLines().stream().sorted(Comparator.comparing(PurchaseInvoiceLine::getLineNo)).toList()) {
            String outcome = resolutionOutcome(line);
            if (ResolutionStatus.UNRESOLVED.equals(line.getResolutionStatus()) || (line.getSuggestedMasterProduct() != null && line.getShopProduct() == null)) {
                anyUnresolved = true;
            }
            if (line.getResolutionStatus() != ResolutionStatus.RESOLVED) {
                allResolved = false;
            }
            lineResponses.add(ReviewLineResponse.builder()
                    .lineId(line.getId())
                    .lineNo(line.getLineNo())
                    .rawName(line.getRawName())
                    .normalizedName(line.getNormalizedName())
                    .quantity(line.getQuantity())
                    .unit(line.getUnit())
                    .unitPrice(line.getUnitPrice())
                    .lineAmount(line.getLineAmount())
                    .resolutionStatus(line.getResolutionStatus().name())
                    .resolvedShopProductId(line.getShopProduct() != null ? line.getShopProduct().getId() : null)
                    .suggestedMasterProductId(line.getSuggestedMasterProduct() != null ? line.getSuggestedMasterProduct().getId() : null)
                    .suggestedConfidence(line.getSuggestedConfidence())
                    .resolutionOutcome(outcome)
                    .build());
        }
        return DraftReviewResponse.builder()
                .draftId(draft.getId())
                .uploadId(draft.getPurchaseUpload().getId())
                .status(draft.getStatus().name())
                .version(draft.getVersion())
                .totalAmount(draft.getTotalAmount())
                .supplierPartyId(draft.getSupplierParty() != null ? draft.getSupplierParty().getId() : null)
                .invoiceNo(draft.getInvoiceNo())
                .invoiceDate(draft.getInvoiceDate())
                .notes(draft.getNotes())
                .lines(lineResponses)
                .needsReview(anyUnresolved || lineResponses.stream().anyMatch(l -> "REVIEW_REQUIRED".equals(l.getResolutionOutcome())))
                .readyToPost(allResolved && !draft.getLines().isEmpty())
                .build();
    }

    private String resolutionOutcome(PurchaseInvoiceLine line) {
        if (line.getResolutionStatus() == ResolutionStatus.RESOLVED && line.getShopProduct() != null) {
            return "RESOLVED";
        }
        if (line.getSuggestedMasterProduct() != null && line.getSuggestedConfidence() != null) {
            if (line.getSuggestedConfidence().compareTo(THRESHOLD_HIGH) >= 0) return "AUTO_MATCH";
            if (line.getSuggestedConfidence().compareTo(THRESHOLD_LOW) >= 0) return "REVIEW_REQUIRED";
        }
        return "NEW_CANDIDATE";
    }
}
