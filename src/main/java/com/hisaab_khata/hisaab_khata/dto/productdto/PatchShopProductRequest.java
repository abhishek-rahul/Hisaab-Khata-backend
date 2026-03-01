package com.hisaab_khata.hisaab_khata.dto.productdto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchShopProductRequest {

    private BigDecimal sellingPrice;

    @DecimalMin(value = "0", message = "minStockInBase must be >= 0")
    private BigDecimal minStockInBase;

    private Boolean isActive;
}
