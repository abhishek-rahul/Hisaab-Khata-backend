package com.hisaab_khata.hisaab_khata.dto.productdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;

    private Long categoryId;
    private String categoryName;

    private String unit;
    private String baseUnit;
    private Integer conversion;

    private Double defaultSalePrice;
    private Double minStock;
}

