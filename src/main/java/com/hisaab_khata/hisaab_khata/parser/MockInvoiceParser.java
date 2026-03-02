package com.hisaab_khata.hisaab_khata.parser;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mock parser for Phase 4D. Returns 3 sample lines regardless of PDF content.
 * Replace with real OCR/PDF parser later.
 */
@Component
public class MockInvoiceParser implements InvoiceParser {

    public static final String PARSER_KEY = "MOCK:v0";

    @Override
    public ParsedInvoice parse(byte[] pdfBytes) {
        List<ParsedLineItem> lines = List.of(
                ParsedLineItem.builder()
                        .lineNo(1)
                        .rawName("Basmati Rice 5 Kg")
                        .quantity(new BigDecimal("10"))
                        .unit("PCS")
                        .unitPrice(new BigDecimal("450.00"))
                        .lineAmount(new BigDecimal("4500.00"))
                        .parseConfidence(new BigDecimal("0.95"))
                        .build(),
                ParsedLineItem.builder()
                        .lineNo(2)
                        .rawName("Refined Oil 1 Ltr")
                        .quantity(new BigDecimal("20"))
                        .unit("PCS")
                        .unitPrice(new BigDecimal("180.50"))
                        .lineAmount(new BigDecimal("3610.00"))
                        .parseConfidence(new BigDecimal("0.92"))
                        .build(),
                ParsedLineItem.builder()
                        .lineNo(3)
                        .rawName("Sugar 1 Kg")
                        .quantity(new BigDecimal("50"))
                        .unit("PCS")
                        .unitPrice(new BigDecimal("48.00"))
                        .lineAmount(new BigDecimal("2400.00"))
                        .parseConfidence(new BigDecimal("0.98"))
                        .build()
        );
        return ParsedInvoice.builder()
                .docKind("PDF")
                .parserKey(PARSER_KEY)
                .lines(lines)
                .build();
    }
}
