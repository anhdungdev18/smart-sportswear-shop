package com.dunghaiquyen.ecommerce.modules.audit.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        String actorName,
        String action,
        String entityType,
        String entityId,
        Map<String, Object> beforeJson,
        Map<String, Object> afterJson,
        String ipAddress,
        String userAgent,
        Instant createdAt) {
}
