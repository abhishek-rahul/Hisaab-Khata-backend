package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Purchase;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface PurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    Purchase toEntity(PurchaseCreateRequest req);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd HH:mm")
    PurchaseResponse toResponse(Purchase purchase);
}

