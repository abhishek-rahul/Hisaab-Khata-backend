package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoice;
import com.hisaab_khata.hisaab_khata.domain.PurchaseInvoiceLine;
import com.hisaab_khata.hisaab_khata.domain.PurchaseUpload;
import com.hisaab_khata.hisaab_khata.domain.PurchaseUploadLine;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.CreateDraftRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftLineResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.SaveDraftLineRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.SaveDraftRequest;
import com.hisaab_khata.hisaab_khata.enums.DocStatus;
import com.hisaab_khata.hisaab_khata.enums.ResolutionStatus;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ConflictException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.normalizer.Normalizer;
import com.hisaab_khata.hisaab_khata.repository.PartyRepository;
import com.hisaab_khata.hisaab_khata.repository.PurchaseInvoiceRepository;
import com.hisaab_khata.hisaab_khata.repository.PurchaseUploadRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.IPurchaseDraftService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseDraftServiceImpl implements IPurchaseDraftService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseUploadRepository purchaseUploadRepository;
    private final ShopRepository shopRepository;
    private final PartyRepository partyRepository;
    private final ShopContext shopContext;
    private final Normalizer normalizer;

    @Override
    @Transactional
    public DraftResponse createDraft(CreateDraftRequest request) {
        if (request.getUploadId() == null) {
            throw new BusinessValidationException("uploadId is required", "VALIDATION_FAILED");
        }
        Long shopId = shopContext.getCurrentShopId();
        PurchaseUpload upload = purchaseUploadRepository.findByShop_IdAndId(shopId, request.getUploadId())
                .orElseThrow(() -> new ResourceNotFoundException("Upload not found", "UPLOAD_NOT_FOUND"));

        // Idempotent: if draft already exists for this upload, return it
        var existing = purchaseInvoiceRepository.findByShop_IdAndPurchaseUpload_Id(shopId, request.getUploadId());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        if (request.getSupplierPartyId() != null) {
            partyRepository.findByShopIdAndId(shopId, request.getSupplierPartyId())
                    .orElseThrow(() -> new BusinessValidationException("Supplier party not found", "SUPPLIER_NOT_FOUND"));
        }

        Shop shop = shopRepository.getReferenceById(shopId);
        PurchaseInvoice draft = PurchaseInvoice.builder()
                .shop(shop)
                .purchaseUpload(upload)
                .invoiceNo(request.getInvoiceNo())
                .invoiceDate(request.getInvoiceDate())
                .notes(request.getNotes())
                .status(DocStatus.DRAFT)
                .version(0)
                .totalAmount(BigDecimal.ZERO)
                .build();
        if (request.getSupplierPartyId() != null) {
            draft.setSupplierParty(partyRepository.getReferenceById(request.getSupplierPartyId()));
        }

        List<PurchaseUploadLine> uploadLines = upload.getLines().stream()
                .sorted(Comparator.comparing(PurchaseUploadLine::getLineNo))
                .toList();
        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseInvoiceLine> lines = new ArrayList<>();
        for (PurchaseUploadLine ul : uploadLines) {
            PurchaseInvoiceLine line = PurchaseInvoiceLine.builder()
                    .purchaseInvoice(draft)
                    .lineNo(ul.getLineNo())
                    .rawName(ul.getRawName())
                    .normalizedName(ul.getNormalizedName())
                    .quantity(ul.getQuantity())
                    .unit(ul.getUnit())
                    .unitPrice(ul.getUnitPrice())
                    .lineAmount(ul.getLineAmount())
                    .resolutionStatus(ResolutionStatus.UNRESOLVED)
                    .build();
            lines.add(line);
            total = total.add(ul.getLineAmount());
        }
        draft.setLines(lines);
        draft.setTotalAmount(total);
        PurchaseInvoice saved = purchaseInvoiceRepository.save(draft);
        return toResponse(saved);
    }

    @Override
    public DraftResponse getDraft(Long draftId) {
        Long shopId = shopContext.getCurrentShopId();
        PurchaseInvoice draft = purchaseInvoiceRepository.findByShop_IdAndId(shopId, draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found", "DRAFT_NOT_FOUND"));
        return toResponse(draft);
    }

    @Override
    @Transactional
    public DraftResponse saveDraft(Long draftId, SaveDraftRequest request) {
        if (request == null || request.getVersion() == null) {
            throw new BusinessValidationException("version is required", "VALIDATION_FAILED");
        }
        Long shopId = shopContext.getCurrentShopId();
        PurchaseInvoice draft = purchaseInvoiceRepository.findByShop_IdAndId(shopId, draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found", "DRAFT_NOT_FOUND"));

        if (draft.getStatus() != DocStatus.DRAFT) {
            throw new ConflictException("Draft can only be saved when status is DRAFT", "DRAFT_NOT_EDITABLE");
        }
        if (!draft.getVersion().equals(request.getVersion())) {
            throw new ConflictException("Version mismatch; draft was modified by another request", "VERSION_CONFLICT");
        }

        if (request.getSupplierPartyId() != null) {
            partyRepository.findByShopIdAndId(shopId, request.getSupplierPartyId())
                    .orElseThrow(() -> new BusinessValidationException("Supplier party not found", "SUPPLIER_NOT_FOUND"));
        }

        draft.setInvoiceNo(request.getInvoiceNo());
        draft.setInvoiceDate(request.getInvoiceDate());
        draft.setNotes(request.getNotes());
        if (request.getSupplierPartyId() != null) {
            draft.setSupplierParty(partyRepository.getReferenceById(request.getSupplierPartyId()));
        } else {
            draft.setSupplierParty(null);
        }

        draft.getLines().clear();
        BigDecimal total = BigDecimal.ZERO;
        if (request.getLines() != null) {
            for (SaveDraftLineRequest lineReq : request.getLines()) {
                String normalizedName = normalizer.normalize(lineReq.getRawName());
                BigDecimal lineAmount = lineReq.getLineAmount() != null
                        ? lineReq.getLineAmount()
                        : (lineReq.getUnitPrice() != null && lineReq.getQuantity() != null
                                ? lineReq.getUnitPrice().multiply(lineReq.getQuantity())
                                : BigDecimal.ZERO);
                PurchaseInvoiceLine line = PurchaseInvoiceLine.builder()
                        .purchaseInvoice(draft)
                        .lineNo(lineReq.getLineNo())
                        .rawName(lineReq.getRawName())
                        .normalizedName(normalizedName)
                        .quantity(lineReq.getQuantity())
                        .unit(lineReq.getUnit())
                        .unitPrice(lineReq.getUnitPrice())
                        .lineAmount(lineAmount)
                        .resolutionStatus(ResolutionStatus.UNRESOLVED)
                        .build();
                draft.getLines().add(line);
                total = total.add(lineAmount);
            }
        }
        draft.setTotalAmount(total);
        draft.setVersion(draft.getVersion() + 1);
        PurchaseInvoice saved = purchaseInvoiceRepository.save(draft);
        return toResponse(saved);
    }

    private DraftResponse toResponse(PurchaseInvoice draft) {
        List<DraftLineResponse> lineResponses = draft.getLines().stream()
                .sorted(Comparator.comparing(PurchaseInvoiceLine::getLineNo))
                .map(line -> DraftLineResponse.builder()
                        .lineNo(line.getLineNo())
                        .rawName(line.getRawName())
                        .normalizedName(line.getNormalizedName())
                        .quantity(line.getQuantity())
                        .unit(line.getUnit())
                        .unitPrice(line.getUnitPrice())
                        .lineAmount(line.getLineAmount())
                        .resolutionStatus(line.getResolutionStatus().name())
                        .shopProductId(line.getShopProduct() != null ? line.getShopProduct().getId() : null)
                        .build())
                .toList();
        return DraftResponse.builder()
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
                .build();
    }
}
