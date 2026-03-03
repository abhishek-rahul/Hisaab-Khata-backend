package com.hisaab_khata.hisaab_khata.dto.ledgerdto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerStatementResponse {

    private Long partyId;
    private LocalDate from;
    private LocalDate to;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private List<LedgerStatementEntryResponse> entries;
}
