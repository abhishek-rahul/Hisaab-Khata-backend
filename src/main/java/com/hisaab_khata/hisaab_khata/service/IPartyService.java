package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.partydto.CreatePartyRequest;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyLedgerResponse;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyResponse;
import com.hisaab_khata.hisaab_khata.enums.PartyType;

import java.util.List;

public interface IPartyService {

    PartyResponse createParty(CreatePartyRequest request);

    List<PartyResponse> listParties(PartyType type);

    PartyResponse getParty(Long partyId);

    PartyLedgerResponse getPartyLedger(Long partyId);
}
