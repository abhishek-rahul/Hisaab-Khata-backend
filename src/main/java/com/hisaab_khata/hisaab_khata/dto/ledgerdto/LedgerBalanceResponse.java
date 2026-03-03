package com.hisaab_khata.hisaab_khata.dto.ledgerdto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerBalanceResponse {

    private Long partyId;
    private BigDecimal balance;
}
