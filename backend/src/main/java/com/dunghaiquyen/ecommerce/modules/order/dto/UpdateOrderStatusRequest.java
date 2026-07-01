package com.dunghaiquyen.ecommerce.modules.order.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull(message = "Status is required") OrderStatus status, String note) {
}
