package com.dunghaiquyen.ecommerce.modules.order.dto;

/** API_SPEC_PHASE1.md 7.4 - reason is optional, no dedicated column exists for it (see OrderService). */
public record CancelOrderRequest(String reason) {
}
