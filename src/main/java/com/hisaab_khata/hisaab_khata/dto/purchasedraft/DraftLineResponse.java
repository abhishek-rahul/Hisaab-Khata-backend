package com.hisaab_khata.hisaab_khata.dto.purchasedraft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftLineResponse {
    private Integer lineNo;
    private String rawName;
    private String normalizedName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private String resolutionStatus;  // UNRESOLVED, RESOLVED (4B)
    private Long shopProductId;      // null until resolved (4B)
}
