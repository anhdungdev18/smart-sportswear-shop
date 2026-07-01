package com.dunghaiquyen.ecommerce.modules.audit.dto;

import java.util.UUID;

public record AdminAuditLogListQuery(Integer page, Integer limit, UUID actorUserId, String entityType, String entityId, String action) {
}
