package com.dunghaiquyen.ecommerce.modules.order.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;

/** Customer's own order list query (API_SPEC_PHASE1.md 7.2): page, limit, status only. */
public record OrderListQuery(Integer page, Integer limit, OrderStatus status) {
}
