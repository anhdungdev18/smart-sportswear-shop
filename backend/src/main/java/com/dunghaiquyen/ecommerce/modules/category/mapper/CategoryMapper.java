package com.dunghaiquyen.ecommerce.modules.category.mapper;

import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryResponse;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
