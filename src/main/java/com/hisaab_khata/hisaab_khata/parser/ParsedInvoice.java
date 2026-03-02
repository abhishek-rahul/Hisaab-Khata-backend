package com.hisaab_khata.hisaab_khata.parser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * In-memory result of parsing an invoice (e.g. PDF). Used by InvoiceParser.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedInvoice {
    private String docKind;
    private String parserKey;
    private List<ParsedLineItem> lines;
}
