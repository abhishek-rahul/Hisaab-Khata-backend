package com.hisaab_khata.hisaab_khata.dto.saledto;


import lombok.*;

//import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleItemResponse {

    private Long productId;
    private String productName;

    private Double quantity;
    private String unit;

    private Double sellingPrice;
    private Double totalPrice;

    private Double profit;
}
