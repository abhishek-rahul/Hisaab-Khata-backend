package com.hisaab_khata.hisaab_khata.dto.purchasedraft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftReviewResponse {
    private Long draftId;
    private Long uploadId;
    private String status;
    private Integer version;
    private BigDecimal totalAmount;
    private Long supplierPartyId;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String notes;
    private List<ReviewLineResponse> lines;
    private Boolean needsReview;   // any line UNRESOLVED or REVIEW_REQUIRED
    private Boolean readyToPost;   // all lines RESOLVED
}
