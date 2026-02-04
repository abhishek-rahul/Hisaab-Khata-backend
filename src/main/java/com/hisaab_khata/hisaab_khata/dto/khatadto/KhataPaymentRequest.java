package com.hisaab_khata.hisaab_khata.dto.khatadto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhataPaymentRequest {
    private Double amount;
    private String mode; // CASH / UPI
}

