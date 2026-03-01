package com.hisaab_khata.hisaab_khata.dto.productdto;

import com.hisaab_khata.hisaab_khata.enums.BaseUnit;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductResponse {

    private Long id;
    private Long masterProductId;
    private String canonicalName;
    private String normalizedName;
    private BaseUnit baseUnit;

    private Long categoryId;
    private String categoryName;

    private String displayName;
    private String displayUnit;
    private BigDecimal conversionToBase;
    private BigDecimal sellingPrice;
    private BigDecimal minStockInBase;
    private Boolean isActive;

    private BigDecimal stockQty; // in base unit
}
