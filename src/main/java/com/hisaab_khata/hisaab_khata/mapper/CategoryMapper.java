package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.dto.productdto.CategoryResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}

