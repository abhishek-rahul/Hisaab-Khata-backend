package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerBalanceResponse;
import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerStatementResponse;
import com.hisaab_khata.hisaab_khata.service.ILedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Phase 6: Ledger (Khata) read APIs – balance and statement from party_ledger.
 */
@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final ILedgerService ledgerService;

    @GetMapping("/parties/{partyId}/balance")
    public ResponseEntity<SuccessResponse<LedgerBalanceResponse>> getBalance(@PathVariable Long partyId) {
        LedgerBalanceResponse res = ledgerService.getPartyBalance(partyId);
        return ResponseEntity.ok(
                SuccessResponse.<LedgerBalanceResponse>builder()
                        .success(true)
                        .data(res)
                        .build());
    }

    @GetMapping("/parties/{partyId}/statement")
    public ResponseEntity<SuccessResponse<LedgerStatementResponse>> getStatement(
            @PathVariable Long partyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        LedgerStatementResponse res = ledgerService.getPartyStatement(partyId, from, to, limit, offset);
        return ResponseEntity.ok(
                SuccessResponse.<LedgerStatementResponse>builder()
                        .success(true)
                        .data(res)
                        .build());
    }
}
