package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockItemResponse {
    private Long productId;
    private String productName;
    private Double quantity;
}

