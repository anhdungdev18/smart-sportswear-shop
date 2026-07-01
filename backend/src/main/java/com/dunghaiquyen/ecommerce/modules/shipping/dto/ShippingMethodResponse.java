package com.dunghaiquyen.ecommerce.modules.shipping.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ShippingMethodResponse(
        UUID id,
        String name,
        String code,
        String description,
        String provider,
        BigDecimal baseFee,
        Integer estimatedDaysMin,
        Integer estimatedDaysMax) {
}
