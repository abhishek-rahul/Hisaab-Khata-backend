package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.PartyLedger;
import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerBalanceResponse;
import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerStatementEntryResponse;
import com.hisaab_khata.hisaab_khata.dto.ledgerdto.LedgerStatementResponse;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.PartyLedgerRepository;
import com.hisaab_khata.hisaab_khata.repository.PartyRepository;
import com.hisaab_khata.hisaab_khata.service.ILedgerService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements ILedgerService {

    private final PartyLedgerRepository partyLedgerRepository;
    private final PartyRepository partyRepository;
    private final ShopContext shopContext;

    @Override
    @Transactional(readOnly = true)
    public LedgerBalanceResponse getPartyBalance(Long partyId) {
        Long shopId = shopContext.getCurrentShopId();
        partyRepository.findByShopIdAndId(shopId, partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found", "PARTY_NOT_FOUND"));

        BigDecimal balance = partyLedgerRepository.sumDrMinusCrBefore(shopId, partyId, LocalDateTime.now().plusYears(100));
        if (balance == null) balance = BigDecimal.ZERO;

        return LedgerBalanceResponse.builder()
                .partyId(partyId)
                .balance(balance)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LedgerStatementResponse getPartyStatement(Long partyId, LocalDate from, LocalDate to, int limit, int offset) {
        Long shopId = shopContext.getCurrentShopId();
        partyRepository.findByShopIdAndId(shopId, partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found", "PARTY_NOT_FOUND"));

        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toEnd = to.atTime(LocalTime.MAX);

        BigDecimal openingBalance = partyLedgerRepository.sumDrMinusCrBefore(shopId, partyId, fromStart);
        if (openingBalance == null) openingBalance = BigDecimal.ZERO;

        var page = partyLedgerRepository.findByShopIdAndParty_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAscIdAsc(
                shopId, partyId, fromStart, toEnd, PageRequest.of(offset / limit, limit));

        List<LedgerStatementEntryResponse> entries = new ArrayList<>();
        BigDecimal running = openingBalance;
        for (PartyLedger e : page.getContent()) {
            BigDecimal delta = e.getDrAmount().subtract(e.getCrAmount());
            running = running.add(delta);
            entries.add(LedgerStatementEntryResponse.builder()
                    .id(e.getId())
                    .createdAt(e.getCreatedAt())
                    .entryType(e.getEntryType())
                    .drAmount(e.getDrAmount())
                    .crAmount(e.getCrAmount())
                    .referenceType(e.getReferenceType())
                    .referenceId(e.getReferenceId())
                    .remarks(e.getRemarks())
                    .runningBalance(running)
                    .build());
        }

        BigDecimal closingBalance = running;

        return LedgerStatementResponse.builder()
                .partyId(partyId)
                .from(from)
                .to(to)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .entries(entries)
                .build();
    }
}
