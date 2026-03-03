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
public class ReviewLineResponse {
    private Long lineId;
    private Integer lineNo;
    private String rawName;
    private String normalizedName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private String resolutionStatus;       // UNRESOLVED, RESOLVED
    private Long resolvedShopProductId;    // set when RESOLVED
    private Long suggestedMasterProductId; // set when REVIEW_REQUIRED
    private BigDecimal suggestedConfidence;
    private String resolutionOutcome;     // AUTO_MATCH, REVIEW_REQUIRED, NEW_CANDIDATE (derived)
}
