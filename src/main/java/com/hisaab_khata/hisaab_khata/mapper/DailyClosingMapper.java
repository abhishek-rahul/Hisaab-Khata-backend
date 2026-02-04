package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.DailyClosing;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyClosingResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface DailyClosingMapper {

    @Mapping(target = "totalProfit", source = "profit")
    @Mapping(target = "upiCollected", source = "upiReceived")
    DailyClosingResponse toResponse(DailyClosing closing);
}


