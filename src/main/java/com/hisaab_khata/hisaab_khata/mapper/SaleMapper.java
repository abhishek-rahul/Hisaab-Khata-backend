package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Sale;
//import com.hisaab_khata.hisaab_khata.dto.saledto.SaleCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleResponse;
//import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {SaleItemMapper.class}
)
public interface SaleMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")

    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "cashPaid", source = "cashPaid")
    @Mapping(target = "upiPaid", source = "upiPaid")
    @Mapping(target = "udharAmount", source = "udharAmount")
    @Mapping(target = "roundOff", source = "roundOff")

    @Mapping(target = "paymentMode", source = "paymentMode")
    @Mapping(target = "totalProfit", source = "profit")

    @Mapping(target = "items", source = "items")
    SaleResponse toResponse(Sale sale);
}


