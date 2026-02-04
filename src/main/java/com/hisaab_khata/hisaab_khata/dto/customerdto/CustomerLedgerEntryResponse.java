package com.hisaab_khata.hisaab_khata.dto.customerdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLedgerEntryResponse {

    private String type;   // UDHAR_DIYA / JAMA_HUA
    private Double amount;
    private String date;
}

