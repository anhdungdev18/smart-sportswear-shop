package com.dunghaiquyen.ecommerce.modules.order.dto;

/** Snapshot of the delivery address as captured at checkout time (Order.addressSnapshotJson). */
public record ShippingAddressResponse(
        String receiverName,
        String phone,
        String province,
        String district,
        String ward,
        String addressLine) {
}
