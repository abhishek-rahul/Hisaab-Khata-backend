package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.partydto.CreatePartyRequest;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyLedgerResponse;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyResponse;
import com.hisaab_khata.hisaab_khata.enums.PartyType;
import com.hisaab_khata.hisaab_khata.service.IPartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parties")
@RequiredArgsConstructor
public class PartyController {

    private final IPartyService partyService;

    @PostMapping
    public ResponseEntity<SuccessResponse<PartyResponse>> create(@Valid @RequestBody CreatePartyRequest request) {
        PartyResponse res = partyService.createParty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<PartyResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<PartyResponse>>> list(
            @RequestParam(required = false) PartyType type) {
        List<PartyResponse> res = partyService.listParties(type);
        return ResponseEntity.ok(
                SuccessResponse.<List<PartyResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{partyId}")
    public ResponseEntity<SuccessResponse<PartyResponse>> get(@PathVariable Long partyId) {
        PartyResponse res = partyService.getParty(partyId);
        return ResponseEntity.ok(
                SuccessResponse.<PartyResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/{partyId}/ledger")
    public ResponseEntity<SuccessResponse<PartyLedgerResponse>> ledger(@PathVariable Long partyId) {
        PartyLedgerResponse res = partyService.getPartyLedger(partyId);
        return ResponseEntity.ok(
                SuccessResponse.<PartyLedgerResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}
