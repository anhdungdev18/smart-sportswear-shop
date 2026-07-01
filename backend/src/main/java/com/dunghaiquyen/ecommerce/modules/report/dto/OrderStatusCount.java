package com.dunghaiquyen.ecommerce.modules.report.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;

/** One row of the "orders by status" breakdown - built via a JPQL constructor expression. */
public record OrderStatusCount(OrderStatus status, Long count) {
}
