package com.hisaab_khata.hisaab_khata.dto.khatadto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingKhataResponse {

    private Long customerId;
    private String customerName;

    private Double balance;
}

