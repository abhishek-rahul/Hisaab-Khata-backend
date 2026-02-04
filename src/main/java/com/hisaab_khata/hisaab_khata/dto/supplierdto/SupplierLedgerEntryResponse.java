package com.hisaab_khata.hisaab_khata.dto.supplierdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierLedgerEntryResponse {

    private String type;     // UDHAR_LIYA / JAMA_KIYA
    private Double amount;
    private String date;
}

