package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnReason;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReturnResponse(
        UUID id,
        UUID orderId,
        String orderCode,
        UUID userId,
        String returnCode,
        ReturnStatus status,
        ReturnReason reason,
        String description,
        Instant requestedAt,
        Instant approvedAt,
        Instant receivedAt,
        Instant resolvedAt,
        List<ReturnItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {
}
