package com.dunghaiquyen.ecommerce.modules.order.entity;

public enum OrderStatus {
    PENDING_CONFIRMATION,
    CANCELLATION_REQUESTED,
    CANCELLATION_APPROVED,
    CONFIRMED,
    PACKING,
    SHIPPING,
    DELIVERED,
    CANCELLED
}
