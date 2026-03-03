package com.hisaab_khata.hisaab_khata.dto.sales;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDraftLineResponse {

    private Long id;
    private Integer lineNo;
    private Long shopProductId;
    private String shopProductDisplayName;
    private BigDecimal quantityInBase;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
}
