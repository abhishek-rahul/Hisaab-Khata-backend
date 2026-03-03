package com.hisaab_khata.hisaab_khata.dto.sales;

import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import com.hisaab_khata.hisaab_khata.enums.DocStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDraftResponse {

    private Long id;
    private Long shopId;
    private Long customerPartyId;  // null for walk-in
    private String customerPartyName;
    private LocalDate invoiceDate;
    private DocStatus status;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal cashPaidAmount;
    private BigDecimal upiPaidAmount;
    private PaymentMode paymentMode;
    private LocalDateTime postedAt;
    private LocalDateTime createdAt;
    private List<SalesDraftLineResponse> lines;
}
