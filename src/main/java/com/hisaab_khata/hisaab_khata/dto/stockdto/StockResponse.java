package com.hisaab_khata.hisaab_khata.dto.stockdto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private Long shopProductId;
    private String displayName;
    private String baseUnit;     // from master_product
    private BigDecimal quantity; // in base unit; negative allowed
}

