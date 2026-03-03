package com.hisaab_khata.hisaab_khata.dto.purchasedraft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostedInvoiceSummary {
    private Long draftId;
    private String status;
    private LocalDateTime postedAt;
    private BigDecimal totalAmount;
    private Long supplierPartyId;
    private String invoiceNo;
}
