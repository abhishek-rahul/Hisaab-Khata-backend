package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.*;
import com.hisaab_khata.hisaab_khata.dto.sales.*;
import com.hisaab_khata.hisaab_khata.enums.DocStatus;
import com.hisaab_khata.hisaab_khata.enums.LedgerEntryType;
import com.hisaab_khata.hisaab_khata.enums.LedgerReferenceType;
import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ConflictException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.ISalesService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements ISalesService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceLineRepository salesInvoiceLineRepository;
    private final ShopProductRepository shopProductRepository;
    private final StockRepository stockRepository;
    private final PartyRepository partyRepository;
    private final PartyLedgerRepository partyLedgerRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final ShopRepository shopRepository;
    private final ShopContext shopContext;

    @Override
    @Transactional
    public SalesDraftResponse createDraft(SalesDraftRequest request) {
        Long shopId = shopContext.getCurrentShopId();
        if (!shopId.equals(request.getShopId())) {
            throw new BusinessValidationException("shop_id must match current shop", "SHOP_MISMATCH");
        }

        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal cashPaid = request.getCashPaidAmount() != null ? request.getCashPaidAmount() : BigDecimal.ZERO;
        BigDecimal upiPaid = request.getUpiPaidAmount() != null ? request.getUpiPaidAmount() : BigDecimal.ZERO;
        BigDecimal paidAmount = cashPaid.add(upiPaid);

        // Resolve customer if provided
        Party customerParty = null;
        if (request.getCustomerPartyId() != null) {
            customerParty = partyRepository.findByShopIdAndId(shopId, request.getCustomerPartyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer party not found", "PARTY_NOT_FOUND"));
        }

        // Build lines and compute gross
        BigDecimal grossAmount = BigDecimal.ZERO;
        List<SalesInvoiceLine> lines = new ArrayList<>();
        int lineNo = 1;
        for (SalesDraftLineItem item : request.getItems()) {
            ShopProduct sp = shopProductRepository.findByShop_IdAndId(shopId, item.getShopProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop product not found: " + item.getShopProductId(), "SHOP_PRODUCT_NOT_FOUND"));
            BigDecimal qty = item.getQuantityInBase();
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal lineAmount = qty.multiply(unitPrice).setScale(2, java.math.RoundingMode.HALF_UP);
            grossAmount = grossAmount.add(lineAmount);
            SalesInvoiceLine line = SalesInvoiceLine.builder()
                    .lineNo(lineNo++)
                    .shopProduct(sp)
                    .quantityInBase(qty)
                    .unitPrice(unitPrice)
                    .lineAmount(lineAmount)
                    .build();
            lines.add(line);
        }

        BigDecimal totalAmount = grossAmount.subtract(discountAmount).setScale(2, java.math.RoundingMode.HALF_UP);

        // paid_amount <= total_amount
        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new BusinessValidationException("paid_amount must be <= total_amount", "PAID_EXCEEDS_TOTAL");
        }
        // Walk-in: paid must equal total
        if (customerParty == null && paidAmount.compareTo(totalAmount) != 0) {
            throw new BusinessValidationException("Walk-in sale requires paid_amount equal to total_amount", "WALKIN_FULL_PAYMENT_REQUIRED");
        }

        PaymentMode paymentMode = derivePaymentMode(cashPaid, upiPaid);

        Shop shopRef = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found", "SHOP_NOT_FOUND"));
        SalesInvoice invoice = SalesInvoice.builder()
                .shop(shopRef)
                .customerParty(customerParty)
                .invoiceDate(LocalDate.now())
                .status(DocStatus.DRAFT)
                .grossAmount(grossAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .cashPaidAmount(cashPaid)
                .upiPaidAmount(upiPaid)
                .paidAmount(paidAmount)
                .paymentMode(paymentMode)
                .build();
        invoice = salesInvoiceRepository.save(invoice);
        for (SalesInvoiceLine line : lines) {
            line.setSalesInvoice(invoice);
            salesInvoiceLineRepository.save(line);
        }
        invoice.setLines(lines);
        return toResponse(invoice);
    }

    private static PaymentMode derivePaymentMode(BigDecimal cashPaid, BigDecimal upiPaid) {
        BigDecimal zero = BigDecimal.ZERO;
        boolean cash = cashPaid != null && cashPaid.compareTo(zero) > 0;
        boolean upi = upiPaid != null && upiPaid.compareTo(zero) > 0;
        if (!cash && !upi) return PaymentMode.NA;
        if (cash && upi) return PaymentMode.MIXED;
        if (cash) return PaymentMode.CASH;
        return PaymentMode.UPI;
    }

    private SalesDraftResponse toResponse(SalesInvoice inv) {
        List<SalesDraftLineResponse> lineResponses = new ArrayList<>();
        if (inv.getLines() != null) {
            for (SalesInvoiceLine line : inv.getLines()) {
                lineResponses.add(SalesDraftLineResponse.builder()
                        .id(line.getId())
                        .lineNo(line.getLineNo())
                        .shopProductId(line.getShopProduct() != null ? line.getShopProduct().getId() : null)
                        .shopProductDisplayName(line.getShopProduct() != null ? line.getShopProduct().getDisplayName() : null)
                        .quantityInBase(line.getQuantityInBase())
                        .unitPrice(line.getUnitPrice())
                        .lineAmount(line.getLineAmount())
                        .build());
            }
        }
        return SalesDraftResponse.builder()
                .id(inv.getId())
                .shopId(inv.getShop() != null ? inv.getShop().getId() : null)
                .customerPartyId(inv.getCustomerParty() != null ? inv.getCustomerParty().getId() : null)
                .customerPartyName(inv.getCustomerParty() != null ? inv.getCustomerParty().getName() : null)
                .invoiceDate(inv.getInvoiceDate())
                .status(inv.getStatus())
                .grossAmount(inv.getGrossAmount())
                .discountAmount(inv.getDiscountAmount())
                .totalAmount(inv.getTotalAmount())
                .paidAmount(inv.getPaidAmount())
                .cashPaidAmount(inv.getCashPaidAmount())
                .upiPaidAmount(inv.getUpiPaidAmount())
                .paymentMode(inv.getPaymentMode())
                .postedAt(inv.getPostedAt())
                .createdAt(inv.getCreatedAt())
                .lines(lineResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDraftResponse getById(Long id) {
        Long shopId = shopContext.getCurrentShopId();
        SalesInvoice inv = salesInvoiceRepository.findByShop_IdAndId(shopId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found", "SALES_INVOICE_NOT_FOUND"));
        return toResponse(inv);
    }

    @Override
    @Transactional
    public PostedSaleResponse post(Long id) {
        Long shopId = shopContext.getCurrentShopId();
        SalesInvoice inv = salesInvoiceRepository.findByShop_IdAndIdForUpdate(shopId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found", "SALES_INVOICE_NOT_FOUND"));

        if (inv.getStatus() == DocStatus.POSTED) {
            throw new ConflictException("Invoice already posted", "ALREADY_POSTED");
        }

        BigDecimal totalAmount = inv.getTotalAmount();
        BigDecimal paidAmount = inv.getPaidAmount();
        List<SalesInvoiceLine> lines = inv.getLines() != null ? inv.getLines() : salesInvoiceLineRepository.findBySalesInvoice_IdOrderByLineNoAsc(inv.getId());

        // 1) Decrement stock per line (negative allowed)
        for (SalesInvoiceLine line : lines) {
            ShopProduct sp = line.getShopProduct();
            if (sp == null || !sp.getShop().getId().equals(shopId)) continue;
            Optional<Stock> stockOpt = stockRepository.findByShopProduct_Id(sp.getId());
            Stock stock;
            if (stockOpt.isPresent()) {
                stock = stockOpt.get();
                stock.setQuantity(stock.getQuantity().subtract(line.getQuantityInBase()));
            } else {
                stock = Stock.builder()
                        .shopProduct(sp)
                        .quantity(line.getQuantityInBase().negate())
                        .build();
            }
            stockRepository.save(stock);
        }

        // 2) Ledger: if customer exists, SALE DR total_amount, PAYMENT_IN CR paid_amount
        if (inv.getCustomerParty() != null) {
            Party party = inv.getCustomerParty();
            PartyLedger saleEntry = PartyLedger.builder()
                    .shopId(shopId)
                    .party(party)
                    .entryType(LedgerEntryType.SALE)
                    .drAmount(totalAmount)
                    .crAmount(BigDecimal.ZERO)
                    .referenceType(LedgerReferenceType.SALE)
                    .referenceId(inv.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            partyLedgerRepository.save(saleEntry);
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                PartyLedger paymentEntry = PartyLedger.builder()
                        .shopId(shopId)
                        .party(party)
                        .entryType(LedgerEntryType.PAYMENT_IN)
                        .drAmount(BigDecimal.ZERO)
                        .crAmount(paidAmount)
                        .referenceType(LedgerReferenceType.PAYMENT)
                        .referenceId(inv.getId())
                        .createdAt(LocalDateTime.now())
                        .build();
                partyLedgerRepository.save(paymentEntry);
            }
        }

        // 3) Upsert daily_summary
        LocalDate day = inv.getInvoiceDate();
        DailySummary summary = dailySummaryRepository.findByShop_IdAndDay(shopId, day)
                .orElseGet(() -> DailySummary.builder()
                        .shop(inv.getShop())
                        .day(day)
                        .totalSales(BigDecimal.ZERO)
                        .cashIn(BigDecimal.ZERO)
                        .receivable(BigDecimal.ZERO)
                        .totalPurchase(BigDecimal.ZERO)
                        .cashOut(BigDecimal.ZERO)
                        .payable(BigDecimal.ZERO)
                        .build());
        summary.setTotalSales(summary.getTotalSales().add(totalAmount));
        summary.setCashIn(summary.getCashIn().add(paidAmount));
        if (inv.getCustomerParty() != null) {
            BigDecimal receivableDelta = totalAmount.subtract(paidAmount);
            summary.setReceivable(summary.getReceivable().add(receivableDelta));
        }
        dailySummaryRepository.save(summary);

        // 4) Update invoice status
        inv.setStatus(DocStatus.POSTED);
        inv.setPostedAt(LocalDateTime.now());
        salesInvoiceRepository.save(inv);

        return PostedSaleResponse.builder()
                .id(inv.getId())
                .status(DocStatus.POSTED.name())
                .postedAt(inv.getPostedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesDraftResponse> list(LocalDate from, LocalDate to) {
        Long shopId = shopContext.getCurrentShopId();
        if (from == null) from = LocalDate.now().minusYears(1);
        if (to == null) to = LocalDate.now();
        List<SalesInvoice> invoices = salesInvoiceRepository.findByShop_IdAndInvoiceDateBetweenOrderByInvoiceDateAscIdAsc(shopId, from, to);
        return invoices.stream().map(this::toResponse).toList();
    }
}
