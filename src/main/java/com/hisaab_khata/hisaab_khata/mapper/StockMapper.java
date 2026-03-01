package com.hisaab_khata.hisaab_khata.mapper;

import com.hisaab_khata.hisaab_khata.domain.Stock;
import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface StockMapper {

    @Mapping(target = "shopProductId", source = "shopProduct.id")
    @Mapping(target = "displayName", source = "shopProduct.displayName")
    @Mapping(target = "baseUnit", source = "shopProduct.masterProduct.baseUnit")
    StockResponse toResponse(Stock stock);
}

