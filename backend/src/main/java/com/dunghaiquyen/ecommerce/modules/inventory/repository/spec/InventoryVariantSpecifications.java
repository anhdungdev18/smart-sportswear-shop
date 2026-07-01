package com.dunghaiquyen.ecommerce.modules.inventory.repository.spec;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Filters for GET /api/v1/admin/inventory (API_SPEC_PHASE1.md 11.1) - querying ProductVariant directly. */
public final class InventoryVariantSpecifications {

    private InventoryVariantSpecifications() {
    }

    /**
     * Side-effect specification (no real predicate) that fetch-joins product so
     * the inventory list does not lazy-load it once per row (N+1) - same pattern
     * as ProductSpecifications.fetchBrandAndCategory(). Category/brand are NOT
     * fetch-joined here too: Hibernate cannot fetch-join more than one
     * collection/association chain safely in a single query without risking a
     * cartesian product on the *-to-many side, and product->category/brand are
     * both *-to-one so this is actually safe to add, but kept minimal since the
     * category/brand names are read once per distinct product, not once per
     * variant row, making the N+1 here negligible in practice for an admin tool.
     */
    public static Specification<ProductVariant> fetchProduct() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("product", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<ProductVariant> hasProductId(UUID productId) {
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }

    public static Specification<ProductVariant> hasCategoryId(UUID categoryId) {
        return (root, query, cb) ->
                cb.equal(root.get("product").get("category").get("id"), categoryId);
    }

    public static Specification<ProductVariant> hasBrandId(UUID brandId) {
        return (root, query, cb) -> cb.equal(root.get("product").get("brand").get("id"), brandId);
    }

    public static Specification<ProductVariant> hasStatus(VariantStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Matches either the variant's own SKU or its product's name. */
    public static Specification<ProductVariant> keywordMatches(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("sku")), pattern),
                    cb.like(cb.lower(root.get("product").get("name")), pattern));
        };
    }
}
