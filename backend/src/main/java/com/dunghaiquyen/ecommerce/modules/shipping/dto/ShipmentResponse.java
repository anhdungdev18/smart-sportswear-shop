package com.dunghaiquyen.ecommerce.modules.shipping.dto;

import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        UUID shippingMethodId,
        String shippingMethodName,
        String shipmentCode,
        String carrierName,
        String trackingNumber,
        ShipmentStatus status,
        BigDecimal shippingFee,
        String receiverName,
        String receiverPhone,
        String province,
        String district,
        String ward,
        String addressLine,
        Instant shippedAt,
        Instant deliveredAt,
        LocalDate estimatedDeliveryDateFrom,
        LocalDate estimatedDeliveryDateTo,
        String note,
        Instant createdAt,
        Instant updatedAt) {
}
