package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Stock;
import com.hisaab_khata.hisaab_khata.dto.reportdto.LowStockItemResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface DashboardMapper {

    @Mapping(target = "productId", source = "shopProduct.id")
    @Mapping(target = "productName", source = "shopProduct.displayName")
    LowStockItemResponse toLowStockItem(Stock stock);
}

