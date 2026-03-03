package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.*;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.PostedInvoiceSummary;
import com.hisaab_khata.hisaab_khata.enums.DocStatus;
import com.hisaab_khata.hisaab_khata.enums.LedgerEntryType;
import com.hisaab_khata.hisaab_khata.enums.LedgerReferenceType;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ConflictException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.IPurchasePostService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchasePostServiceImpl implements IPurchasePostService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ShopProductRepository shopProductRepository;
    private final StockRepository stockRepository;
    private final PartyLedgerRepository partyLedgerRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final ShopContext shopContext;

    @Override
    @Transactional
    public PostedInvoiceSummary postDraft(Long draftId) {
        Long shopId = shopContext.getCurrentShopId();
        PurchaseInvoice invoice = purchaseInvoiceRepository.findByShop_IdAndIdForUpdate(shopId, draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found", "DRAFT_NOT_FOUND"));

        if (invoice.getStatus() == DocStatus.POSTED) {
            throw new ConflictException("Invoice already posted", "ALREADY_POSTED");
        }
        if (invoice.getSupplierParty() == null) {
            throw new BusinessValidationException("Supplier must be set before post", "SUPPLIER_REQUIRED");
        }

        List<PurchaseInvoiceLine> lines = invoice.getLines().stream()
                .sorted(Comparator.comparing(PurchaseInvoiceLine::getLineNo))
                .toList();

        for (PurchaseInvoiceLine line : lines) {
            if (line.getShopProduct() == null) {
                throw new BusinessValidationException("All lines must be resolved before post", "UNRESOLVED_LINES");
            }
        }

        // Recompute total from lines (server-side)
        BigDecimal total = lines.stream()
                .map(PurchaseInvoiceLine::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setTotalAmount(total);

        // Stock update: for each line, add quantity to stock (in base unit)
        for (PurchaseInvoiceLine line : lines) {
            ShopProduct sp = line.getShopProduct();
            if (!sp.getShop().getId().equals(shopId)) {
                throw new BusinessValidationException("Shop product does not belong to shop", "INVALID_SHOP_PRODUCT");
            }
            BigDecimal qtyBase = quantityInBaseUnit(line, sp);
            Optional<Stock> stockOpt = stockRepository.findByShopProduct_Id(sp.getId());
            Stock stock;
            if (stockOpt.isPresent()) {
                stock = stockOpt.get();
                stock.setQuantity(stock.getQuantity().add(qtyBase));
            } else {
                stock = Stock.builder()
                        .shopProduct(sp)
                        .quantity(qtyBase)
                        .build();
            }
            stockRepository.save(stock);
        }

        // Ledger: one aggregated row (CR = total, DR = 0)
        PartyLedger ledgerEntry = PartyLedger.builder()
                .shopId(shopId)
                .party(invoice.getSupplierParty())
                .entryType(LedgerEntryType.PURCHASE)
                .drAmount(BigDecimal.ZERO)
                .crAmount(total)
                .referenceType(LedgerReferenceType.PURCHASE)
                .referenceId(invoice.getId())
                .createdAt(LocalDateTime.now())
                .build();
        partyLedgerRepository.save(ledgerEntry);

        // Upsert daily_summary (total_purchase, payable; cashOut += 0 for now)
        LocalDate day = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : LocalDate.now();
        DailySummary summary = dailySummaryRepository.findByShop_IdAndDay(shopId, day)
                .orElseGet(() -> DailySummary.builder()
                        .shop(invoice.getShop())
                        .day(day)
                        .totalSales(BigDecimal.ZERO)
                        .cashIn(BigDecimal.ZERO)
                        .receivable(BigDecimal.ZERO)
                        .totalPurchase(BigDecimal.ZERO)
                        .cashOut(BigDecimal.ZERO)
                        .payable(BigDecimal.ZERO)
                        .build());
        summary.setTotalPurchase(summary.getTotalPurchase() != null ? summary.getTotalPurchase().add(total) : total);
        summary.setPayable(summary.getPayable() != null ? summary.getPayable().add(total) : total);
        dailySummaryRepository.save(summary);

        // Update invoice to POSTED
        invoice.setStatus(DocStatus.POSTED);
        invoice.setPostedAt(LocalDateTime.now());
        purchaseInvoiceRepository.save(invoice);

        return PostedInvoiceSummary.builder()
                .draftId(invoice.getId())
                .status(invoice.getStatus().name())
                .postedAt(invoice.getPostedAt())
                .totalAmount(invoice.getTotalAmount())
                .supplierPartyId(invoice.getSupplierParty().getId())
                .invoiceNo(invoice.getInvoiceNo())
                .build();
    }

    private BigDecimal quantityInBaseUnit(PurchaseInvoiceLine line, ShopProduct sp) {
        String lineUnit = line.getUnit() != null ? line.getUnit().trim() : "";
        String displayUnit = sp.getDisplayUnit() != null ? sp.getDisplayUnit().trim() : "";
        String baseUnitName = sp.getMasterProduct() != null ? sp.getMasterProduct().getBaseUnit().name() : "";
        if (lineUnit.equalsIgnoreCase(displayUnit)) {
            return line.getQuantity().multiply(sp.getConversionToBase());
        }
        if (lineUnit.equalsIgnoreCase(baseUnitName)) {
            return line.getQuantity();
        }
        throw new BusinessValidationException(
                "Unit mismatch for line: '" + line.getRawName() + "' (unit '" + lineUnit + "' not matching product)",
                "UNIT_MISMATCH");
    }
}
