package com.hisaab_khata.hisaab_khata.dto.stockdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private Long productId;
    private String productName;

    private Double quantity;     // always base unit
    private String baseUnit;
}

