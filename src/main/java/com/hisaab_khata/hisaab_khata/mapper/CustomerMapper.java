package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Customer;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    Customer toEntity(CustomerCreateRequest req);

    CustomerResponse toResponse(Customer customer);
}

