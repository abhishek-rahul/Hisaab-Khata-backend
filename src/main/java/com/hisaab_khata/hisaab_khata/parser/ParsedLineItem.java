package com.hisaab_khata.hisaab_khata.parser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * In-memory representation of one parsed invoice line (before normalization).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedLineItem {
    private int lineNo;
    private String rawName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private BigDecimal parseConfidence;
}
