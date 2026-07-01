package com.dunghaiquyen.ecommerce.modules.shipping.entity;

/** Exact set the V3 chk_shipments_status check constraint allows - do not add a value here without a migration to match. */
public enum ShipmentStatus {
    PENDING,
    READY_TO_SHIP,
    SHIPPING,
    DELIVERED,
    FAILED,
    RETURNED,
    CANCELLED
}
