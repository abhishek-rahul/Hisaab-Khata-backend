package com.hisaab_khata.hisaab_khata.dto.productdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductUpdateRequest {

    private String name;
    private Double defaultSalePrice;
    private Double minStock;
}

