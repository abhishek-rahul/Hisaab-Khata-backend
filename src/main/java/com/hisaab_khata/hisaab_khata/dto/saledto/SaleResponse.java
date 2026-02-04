package com.hisaab_khata.hisaab_khata.dto.saledto;

import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponse {

    private Long id;

    private Long customerId;
    private String customerName;   // null for walk-in

    private Double totalAmount;

    private Double cashPaid;
    private Double upiPaid;
    private Double udharAmount;

    private Double roundOff;

    private PaymentMode paymentMode;

    private Double totalProfit;

    private List<SaleItemResponse> items;
}


