package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface CategoryMapper {

    @Mapping(target = "scope", source = "scope", qualifiedByName = "scopeToString")
    CategoryResponse toResponse(Category category);

    @Named("scopeToString")
    default String scopeToString(com.hisaab_khata.hisaab_khata.enums.CategoryScope scope) {
        return scope == null ? null : scope.name();
    }
}

