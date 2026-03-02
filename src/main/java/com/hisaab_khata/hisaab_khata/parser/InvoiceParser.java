package com.hisaab_khata.hisaab_khata.parser;

/**
 * Pluggable invoice parser. Implementations may be mock (Phase 4D) or real OCR/PDF later.
 */
public interface InvoiceParser {
    ParsedInvoice parse(byte[] pdfBytes);
}
