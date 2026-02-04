package com.hisaab_khata.hisaab_khata.dto.productdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    private String name;
    private Long categoryId;

    private String unit;      // KG, Litre
    private String baseUnit;  // GRAM, ML
    private Integer conversion;

    private Double defaultSalePrice;
    private Double minStock;
}

