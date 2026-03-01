package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.Party;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.partydto.CreatePartyRequest;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyLedgerResponse;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyResponse;
import com.hisaab_khata.hisaab_khata.enums.PartyType;
import com.hisaab_khata.hisaab_khata.exception.ConflictException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.PartyMapper;
import com.hisaab_khata.hisaab_khata.repository.PartyRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.IPartyService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyServiceImpl implements IPartyService {

    private final PartyRepository partyRepository;
    private final ShopRepository shopRepository;
    private final PartyMapper partyMapper;
    private final ShopContext shopContext;

    @Override
    public PartyResponse createParty(CreatePartyRequest request) {
        Long shopId = shopContext.getCurrentShopId();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found", "SHOP_NOT_FOUND"));

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (partyRepository.existsByShopIdAndPhone(shopId, request.getPhone().trim())) {
                throw new ConflictException(
                        "A party with this phone number already exists in this shop",
                        "PARTY_PHONE_DUPLICATE");
            }
        }

        Party party = partyMapper.toEntity(request);
        party.setShop(shop);
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            party.setPhone(request.getPhone().trim());
        } else {
            party.setPhone(null);
        }
        party = partyRepository.save(party);
        return partyMapper.toResponse(party);
    }

    @Override
    public List<PartyResponse> listParties(PartyType type) {
        Long shopId = shopContext.getCurrentShopId();

        List<Party> parties = type == null
                ? partyRepository.findByShopId(shopId)
                : partyRepository.findByShopIdAndType(shopId, type);
        return parties.stream().map(partyMapper::toResponse).toList();
    }

    @Override
    public PartyResponse getParty(Long partyId) {
        Long shopId = shopContext.getCurrentShopId();

        Party party = partyRepository.findByShopIdAndId(shopId, partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found", "PARTY_NOT_FOUND"));
        return partyMapper.toResponse(party);
    }

    @Override
    public PartyLedgerResponse getPartyLedger(Long partyId) {
        Long shopId = shopContext.getCurrentShopId();

        partyRepository.findByShopIdAndId(shopId, partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found", "PARTY_NOT_FOUND"));

        return PartyLedgerResponse.builder()
                .partyId(partyId)
                .entries(Collections.emptyList())
                .balance(BigDecimal.ZERO)
                .build();
    }
}
