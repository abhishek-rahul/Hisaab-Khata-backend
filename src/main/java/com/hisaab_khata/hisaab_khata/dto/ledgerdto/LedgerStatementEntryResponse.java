package com.hisaab_khata.hisaab_khata.dto.ledgerdto;

import com.hisaab_khata.hisaab_khata.enums.LedgerEntryType;
import com.hisaab_khata.hisaab_khata.enums.LedgerReferenceType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerStatementEntryResponse {

    private Long id;
    private LocalDateTime createdAt;
    private LedgerEntryType entryType;
    private BigDecimal drAmount;
    private BigDecimal crAmount;
    private LedgerReferenceType referenceType;
    private Long referenceId;
    private String remarks;
    private BigDecimal runningBalance;
}
