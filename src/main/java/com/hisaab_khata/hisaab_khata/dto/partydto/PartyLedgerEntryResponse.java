package com.hisaab_khata.hisaab_khata.dto.partydto;

import com.hisaab_khata.hisaab_khata.enums.LedgerEntryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Single ledger entry. Used in PartyLedgerResponse. Phase 2 returns empty list.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyLedgerEntryResponse {

    private Long id;
    private LedgerEntryType entryType;
    private BigDecimal drAmount;
    private BigDecimal crAmount;
    private String referenceType;
    private Long referenceId;
    private String remarks;
    private LocalDateTime createdAt;
}
