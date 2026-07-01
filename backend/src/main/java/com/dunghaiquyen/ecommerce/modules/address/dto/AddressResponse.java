package com.dunghaiquyen.ecommerce.modules.address.dto;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String receiverName,
        String phone,
        String province,
        String district,
        String ward,
        String addressLine,
        boolean isDefault,
        Instant createdAt) {
}
