package com.hisaab_khata.hisaab_khata.dto.productdto;

import com.hisaab_khata.hisaab_khata.enums.BaseUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateManualProductRequest {

    @NotBlank(message = "canonicalName is required")
    private String canonicalName;

    @NotBlank(message = "normalizedName is required")
    private String normalizedName;

    @NotNull(message = "baseUnit is required")
    private BaseUnit baseUnit;

    private Long categoryId;

    @NotBlank(message = "displayName is required")
    private String displayName;

    @NotBlank(message = "displayUnit is required")
    private String displayUnit;

    @NotNull(message = "conversionToBase is required")
    @DecimalMin(value = "0.000001", message = "conversionToBase must be positive")
    private BigDecimal conversionToBase;

    private BigDecimal sellingPrice;

    private BigDecimal minStockInBase;
}
