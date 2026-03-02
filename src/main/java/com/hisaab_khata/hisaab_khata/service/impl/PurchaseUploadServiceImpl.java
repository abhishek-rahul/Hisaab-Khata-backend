package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.Party;
import com.hisaab_khata.hisaab_khata.domain.PurchaseUpload;
import com.hisaab_khata.hisaab_khata.domain.PurchaseUploadLine;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.dto.purchaseupload.ParsedInvoiceResponse;
import com.hisaab_khata.hisaab_khata.dto.purchaseupload.ParsedLineResponse;
import com.hisaab_khata.hisaab_khata.enums.PurchaseUploadDocKind;
import com.hisaab_khata.hisaab_khata.enums.PurchaseUploadStatus;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.normalizer.Normalizer;
import com.hisaab_khata.hisaab_khata.parser.InvoiceParser;
import com.hisaab_khata.hisaab_khata.parser.ParsedInvoice;
import com.hisaab_khata.hisaab_khata.parser.ParsedLineItem;
import com.hisaab_khata.hisaab_khata.repository.PartyRepository;
import com.hisaab_khata.hisaab_khata.repository.PurchaseUploadRepository;
import com.hisaab_khata.hisaab_khata.service.IPurchaseUploadService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseUploadServiceImpl implements IPurchaseUploadService {

    private final PurchaseUploadRepository purchaseUploadRepository;
    private final PartyRepository partyRepository;
    private final ShopRepository shopRepository;
    private final ShopContext shopContext;
    private final InvoiceParser invoiceParser;
    private final Normalizer normalizer;

    @Override
    @Transactional
    public ParsedInvoiceResponse upload(byte[] fileBytes, String originalFileName, Long supplierPartyId) {
        Long shopId = shopContext.getCurrentShopId();
        if (supplierPartyId != null) {
            partyRepository.findByShopIdAndId(shopId, supplierPartyId)
                    .orElseThrow(() -> new com.hisaab_khata.hisaab_khata.exception.BusinessValidationException(
                            "Supplier party not found", "SUPPLIER_NOT_FOUND"));
        }

        ParsedInvoice parsed = invoiceParser.parse(fileBytes);
        Shop shop = shopRepository.getReferenceById(shopId);

        PurchaseUpload upload = PurchaseUpload.builder()
                .shop(shop)
                .docKind(PurchaseUploadDocKind.valueOf(parsed.getDocKind()))
                .parserKey(parsed.getParserKey())
                .status(PurchaseUploadStatus.PARSED)
                .originalFileName(originalFileName)
                .build();
        if (supplierPartyId != null) {
            upload.setSupplierParty(partyRepository.getReferenceById(supplierPartyId));
        }

        List<PurchaseUploadLine> lines = new ArrayList<>();
        for (ParsedLineItem item : parsed.getLines()) {
            String normalizedName = normalizer.normalize(item.getRawName());
            PurchaseUploadLine line = PurchaseUploadLine.builder()
                    .purchaseUpload(upload)
                    .lineNo(item.getLineNo())
                    .rawName(item.getRawName())
                    .normalizedName(normalizedName)
                    .quantity(item.getQuantity())
                    .unit(item.getUnit())
                    .unitPrice(item.getUnitPrice())
                    .lineAmount(item.getLineAmount())
                    .parseConfidence(item.getParseConfidence())
                    .build();
            lines.add(line);
        }
        upload.setLines(lines);
        PurchaseUpload saved = purchaseUploadRepository.save(upload);

        return toResponse(saved);
    }

    @Override
    public ParsedInvoiceResponse getByUploadId(Long uploadId) {
        Long shopId = shopContext.getCurrentShopId();
        PurchaseUpload upload = purchaseUploadRepository.findByShop_IdAndId(shopId, uploadId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload not found", "UPLOAD_NOT_FOUND"));
        return toResponse(upload);
    }

    private ParsedInvoiceResponse toResponse(PurchaseUpload upload) {
        List<ParsedLineResponse> lineResponses = upload.getLines().stream()
                .sorted(Comparator.comparing(PurchaseUploadLine::getLineNo))
                .map(line -> ParsedLineResponse.builder()
                        .lineNo(line.getLineNo())
                        .rawName(line.getRawName())
                        .normalizedName(line.getNormalizedName())
                        .quantity(line.getQuantity())
                        .unit(line.getUnit())
                        .unitPrice(line.getUnitPrice())
                        .lineAmount(line.getLineAmount())
                        .parseConfidence(line.getParseConfidence())
                        .build())
                .toList();
        return ParsedInvoiceResponse.builder()
                .uploadId(upload.getId())
                .docKind(upload.getDocKind().name())
                .parserKey(upload.getParserKey())
                .lines(lineResponses)
                .build();
    }
}
