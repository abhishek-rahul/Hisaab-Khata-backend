package com.hisaab_khata.hisaab_khata.mapper;

import com.hisaab_khata.hisaab_khata.domain.Party;
import com.hisaab_khata.hisaab_khata.dto.partydto.CreatePartyRequest;
import com.hisaab_khata.hisaab_khata.dto.partydto.PartyResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface PartyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    Party toEntity(CreatePartyRequest req);

    PartyResponse toResponse(Party party);
}
