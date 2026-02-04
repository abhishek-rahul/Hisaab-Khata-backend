package com.hisaab_khata.hisaab_khata.dto.supplierdto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierLedgerResponse {

    private Long supplierId;
    private String supplierName;

    private Double totalUdhar;
    private Double totalPaid;

    private Double balance;

    private List<SupplierLedgerEntryResponse> transactions;
}

