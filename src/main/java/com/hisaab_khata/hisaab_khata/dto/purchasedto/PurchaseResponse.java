package com.hisaab_khata.hisaab_khata.dto.purchasedto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    private Long id;

    private Long productId;
    private String productName;

    private Long supplierId;
    private String supplierName;

    private Double quantity;
    private String unit;

    private Double costPrice;
    private String paymentMode;

    private String createdAt;
}

