package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Product;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductResponse;
import com.hisaab_khata.hisaab_khata.dto.productdto.ProductUpdateRequest;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;

import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductCreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProductUpdateRequest req, @MappingTarget Product product);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toResponse(Product product);
}
