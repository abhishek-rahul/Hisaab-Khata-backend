package com.hisaab_khata.hisaab_khata.dto.sales;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDraftLineItem {

    @NotNull(message = "shop_product_id is required")
    private Long shopProductId;

    @NotNull(message = "quantity_in_base is required")
    @DecimalMin(value = "0.000001", message = "quantity_in_base must be positive")
    private BigDecimal quantityInBase;

    @NotNull(message = "unit_price is required")
    @DecimalMin(value = "0", message = "unit_price must be >= 0")
    private BigDecimal unitPrice;
}
