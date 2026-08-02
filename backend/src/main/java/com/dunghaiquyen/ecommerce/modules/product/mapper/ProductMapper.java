package com.dunghaiquyen.ecommerce.modules.product.mapper;

import com.dunghaiquyen.ecommerce.modules.product.dto.ProductImageResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductVariantResponse;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductImage;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "availableQuantity", expression = "java(variant.getStatus() == com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus.ACTIVE ? Math.max(0, variant.getStockQuantity() - variant.getReservedQuantity()) : 0)")
    ProductVariantResponse toVariantResponse(ProductVariant variant);

    // ProductImage's boolean field is named "primary" (Lombok getter isPrimary(),
    // property name "primary" once the "is" prefix is stripped) but the DTO
    // record component is literally named "isPrimary" - names don't auto-match.
    @Mapping(target = "isPrimary", source = "primary")
    ProductImageResponse toImageResponse(ProductImage image);
}
