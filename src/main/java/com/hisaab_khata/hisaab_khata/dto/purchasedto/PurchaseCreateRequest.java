package com.hisaab_khata.hisaab_khata.dto.purchasedto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseCreateRequest {

    private Long productId;
    private Long supplierId;

    private Double quantity;
    private String unit;

    private Double costPrice;

    private String paymentMode; // CASH / CREDIT
}

