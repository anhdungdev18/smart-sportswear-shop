package com.dunghaiquyen.ecommerce.modules.order.mapper;

import com.dunghaiquyen.ecommerce.modules.order.dto.OrderItemResponse;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "productName", source = "productNameSnapshot")
    @Mapping(target = "sku", source = "skuSnapshot")
    @Mapping(target = "size", source = "sizeSnapshot")
    @Mapping(target = "color", source = "colorSnapshot")
    @Mapping(target = "unitPrice", source = "unitPriceSnapshot")
    OrderItemResponse toItemResponse(OrderItem item);
}
