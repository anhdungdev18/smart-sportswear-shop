package com.dunghaiquyen.ecommerce.modules.shipping.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShipmentStatus;
import java.util.UUID;

/**
 * Every field optional/partial-update (null = leave unchanged) - mirrors
 * UpdateOrderStatusRequest's "admin sends only what changed" shape. shipping
 * method, tracking number, carrier name and status can each be updated
 * independently of the others in a single PATCH.
 */
public record UpdateShipmentRequest(
        UUID shippingMethodId,
        @NullOrNotBlank String trackingNumber,
        @NullOrNotBlank String carrierName,
        ShipmentStatus status,
        String note) {
}
