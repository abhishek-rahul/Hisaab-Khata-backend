package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerBalanceResponse;
import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerStatementResponse;

import java.time.LocalDate;

public interface ILedgerService {

    LedgerBalanceResponse getPartyBalance(Long partyId);

    LedgerStatementResponse getPartyStatement(Long partyId, LocalDate from, LocalDate to, int limit, int offset);
}
