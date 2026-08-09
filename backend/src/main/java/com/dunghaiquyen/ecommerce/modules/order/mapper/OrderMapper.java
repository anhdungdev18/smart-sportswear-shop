package com.dunghaiquyen.ecommerce.modules.order.mapper;

import com.dunghaiquyen.ecommerce.modules.order.dto.OrderItemResponse;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "productId", source = "item.product.id")
    @Mapping(target = "variantId", source = "item.variant.id")
    @Mapping(target = "productName", source = "item.productNameSnapshot")
    @Mapping(target = "sku", source = "item.skuSnapshot")
    @Mapping(target = "size", source = "item.sizeSnapshot")
    @Mapping(target = "color", source = "item.colorSnapshot")
    @Mapping(target = "unitPrice", source = "item.unitPriceSnapshot")
    OrderItemResponse toItemResponse(OrderItem item, String thumbnail);
}
