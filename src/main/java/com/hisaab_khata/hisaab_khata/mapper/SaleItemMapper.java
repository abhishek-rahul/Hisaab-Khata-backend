package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.SaleItem;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleItemRequest;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleItemResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SaleItemMapper {

    // Converts SaleItem entity → SaleItemResponse DTO
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    SaleItemResponse toResponse(SaleItem entity);

    // Partial mapping from request → entity (service fills computed fields)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shopId", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "product", ignore = true)          // service loads product
    @Mapping(target = "quantityBase", ignore = true)     // computed in service
    @Mapping(target = "totalPrice", ignore = true)       // computed in service
    @Mapping(target = "profit", ignore = true)           // computed in service
    SaleItem toEntity(SaleItemRequest request);
}

