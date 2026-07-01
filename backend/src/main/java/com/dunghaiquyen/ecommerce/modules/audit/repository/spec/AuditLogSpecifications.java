package com.dunghaiquyen.ecommerce.modules.audit.repository.spec;

import com.dunghaiquyen.ecommerce.modules.audit.entity.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> byActor(UUID actorUserId) {
        return (root, query, cb) -> cb.equal(root.get("actorUser").get("id"), actorUserId);
    }

    public static Specification<AuditLog> byEntityType(String entityType) {
        return (root, query, cb) -> cb.equal(root.get("entityType"), entityType);
    }

    public static Specification<AuditLog> byEntityId(String entityId) {
        return (root, query, cb) -> cb.equal(root.get("entityId"), entityId);
    }

    public static Specification<AuditLog> byAction(String action) {
        return (root, query, cb) -> cb.equal(root.get("action"), action);
    }
}
