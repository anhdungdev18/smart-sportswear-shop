package com.dunghaiquyen.ecommerce.modules.combo.dto;

import com.dunghaiquyen.ecommerce.modules.combo.entity.ComboStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ComboResponse(
        UUID id,
        String name,
        String description,
        BigDecimal discountAmount,
        ComboStatus status,
        List<ComboProductResponse> products,
        Instant createdAt,
        Instant updatedAt) {

    public record ComboProductResponse(UUID productId, String productName, int quantity) {
    }
}
