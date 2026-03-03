package com.hisaab_khata.hisaab_khata.dto.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDraftRequest {

    @NotNull(message = "shop_id is required")
    private Long shopId;

    private Long customerPartyId;  // nullable for walk-in

    @NotNull(message = "discount_amount is required")
    @DecimalMin(value = "0", message = "discount_amount must be >= 0")
    private BigDecimal discountAmount;

    @NotNull(message = "cash_paid_amount is required")
    @DecimalMin(value = "0", message = "cash_paid_amount must be >= 0")
    private BigDecimal cashPaidAmount;

    @NotNull(message = "upi_paid_amount is required")
    @DecimalMin(value = "0", message = "upi_paid_amount must be >= 0")
    private BigDecimal upiPaidAmount;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<SalesDraftLineItem> items;
}
