package com.hisaab_khata.hisaab_khata.dto.partydto;

import lombok.*;

import java.util.List;

/**
 * Phase 2: entries empty, balance 0. Later phases will populate entries and compute balance.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyLedgerResponse {

    private Long partyId;
    private List<PartyLedgerEntryResponse> entries;
    private java.math.BigDecimal balance;
}
