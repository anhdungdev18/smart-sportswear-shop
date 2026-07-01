package com.dunghaiquyen.ecommerce.modules.inventory.repository.spec;

import com.dunghaiquyen.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dunghaiquyen.ecommerce.modules.inventory.entity.InventoryTransactionType;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Filters for GET /api/v1/admin/inventory/transactions (API_SPEC_PHASE1.md 11.5). */
public final class InventoryTransactionSpecifications {

    private InventoryTransactionSpecifications() {
    }

    /** Fetch-joins variant and createdBy so the history list does not lazy-load either per row. */
    public static Specification<InventoryTransaction> fetchVariantAndCreatedBy() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("variant", JoinType.LEFT);
                root.fetch("createdBy", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<InventoryTransaction> hasVariantId(UUID variantId) {
        return (root, query, cb) -> cb.equal(root.get("variant").get("id"), variantId);
    }

    public static Specification<InventoryTransaction> hasType(InventoryTransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<InventoryTransaction> createdFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<InventoryTransaction> createdTo(Instant to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
