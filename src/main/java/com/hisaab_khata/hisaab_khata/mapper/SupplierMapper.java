package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Supplier;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    Supplier toEntity(SupplierCreateRequest req);

    SupplierResponse toResponse(Supplier supplier);
}

