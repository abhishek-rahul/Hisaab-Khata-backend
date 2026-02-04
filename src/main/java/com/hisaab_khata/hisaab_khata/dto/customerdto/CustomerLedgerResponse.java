package com.hisaab_khata.hisaab_khata.dto.customerdto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLedgerResponse {

    private Long customerId;
    private String customerName;

    private Double totalUdhar;
    private Double totalPaid;

    private Double balance;

    private List<CustomerLedgerEntryResponse> transactions;
}

