package com.dunghaiquyen.ecommerce.modules.brand.mapper;

import com.dunghaiquyen.ecommerce.modules.brand.dto.BrandResponse;
import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    BrandResponse toResponse(Brand brand);
}
