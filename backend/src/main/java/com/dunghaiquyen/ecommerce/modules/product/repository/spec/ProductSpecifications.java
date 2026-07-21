package com.dunghaiquyen.ecommerce.modules.product.repository.spec;

import com.dunghaiquyen.ecommerce.modules.collection.entity.ProductCollection;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.Gender;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductType;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * size/color/minPrice/maxPrice are independent facets, each implemented as its
 * own EXISTS-subquery against product_variants: "product has size M" and
 * "product has color Black" can each be satisfied by a DIFFERENT variant of
 * the same product. This matches how faceted filters normally read (not "one
 * variant matching every facet at once") and avoids a join-based combinatorial
 * explosion. All variant-based filters exclude INACTIVE variants so a
 * discontinued variant cannot make a product match a facet that is otherwise
 * not really available.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Side-effect specification (no real predicate) that fetch-joins brand and
     * category so the listing endpoint does not lazy-load them once per row
     * (N+1). Safe with pagination here because both are *-to-one - fetch-joining
     * a *-to-many would duplicate rows and break LIMIT/OFFSET, but that does not
     * apply to brand/category. Guarded against the separate COUNT query pass,
     * which cannot have a fetch applied.
     */
    public static Specification<Product> fetchBrandAndCategory() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("brand", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Product> hasCategoryId(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Taxonomy V2: allows filtering by a GROUP category while products still
     * point at leaf categories. A match occurs when product.category.id is the
     * requested id OR product.category.parent.id is that id.
     */
    public static Specification<Product> hasCategoryIdOrParentId(UUID categoryId) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("category").get("id"), categoryId),
                cb.equal(root.get("category").get("parent").get("id"), categoryId));
    }

    public static Specification<Product> hasBrandId(UUID brandId) {
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    /** Admin product list filter. */
    public static Specification<Product> isFeatured(boolean featured) {
        return (root, query, cb) -> cb.equal(root.get("featured"), featured);
    }

    /** Phase N4 (related products): excludes the product itself from its own "related" list. */
    public static Specification<Product> hasIdNot(UUID id) {
        return (root, query, cb) -> cb.notEqual(root.get("id"), id);
    }

    public static Specification<Product> hasGender(Gender gender) {
        return (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }

    public static Specification<Product> hasSportType(String sportType) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("sportType")), sportType.toLowerCase());
    }

    /** Catalog V2 (V10): filter by top-level product type (APPAREL / FOOTWEAR / ACCESSORY / EQUIPMENT). */
    public static Specification<Product> hasProductType(ProductType productType) {
        return (root, query, cb) -> cb.equal(root.get("productType"), productType);
    }

    /**
     * Catalog V2 (V10): filter to products that belong to a specific collection.
     * Implemented as an EXISTS subquery on product_collections, mirroring the
     * same EXISTS pattern used for variant-based facets above - avoids the
     * duplicate-row problem that a JOIN would introduce when paginating.
     */
    public static Specification<Product> inCollection(UUID collectionId) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            var pcRoot = sub.from(ProductCollection.class);
            sub.select(cb.literal(1L));
            sub.where(
                    cb.equal(pcRoot.get("product"), root),
                    cb.equal(pcRoot.get("collection").get("id"), collectionId));
            return cb.exists(sub);
        };
    }

    public static Specification<Product> keywordMatches(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("shortDescription")), pattern));
        };
    }

    public static Specification<Product> hasVariantSize(String size) {
        return variantExists((variantRoot, cb) -> cb.equal(variantRoot.get("size"), size));
    }

    public static Specification<Product> hasVariantColor(String color) {
        return variantExists((variantRoot, cb) -> cb.equal(variantRoot.get("color"), color));
    }

    public static Specification<Product> hasVariantPriceGte(BigDecimal minPrice) {
        return variantExists((variantRoot, cb) -> cb.greaterThanOrEqualTo(variantRoot.get("price"), minPrice));
    }

    public static Specification<Product> hasVariantPriceLte(BigDecimal maxPrice) {
        return variantExists((variantRoot, cb) -> cb.lessThanOrEqualTo(variantRoot.get("price"), maxPrice));
    }

    /**
     * Price is intentionally not a column on products (ERD_PHASE1.md 9.3: price
     * lives on product_variants), so "sort by price" has no column to sort by
     * directly. Implemented as a correlated MIN(variant.price) subquery used
     * directly as the ORDER BY expression - this is a side effect on the shared
     * CriteriaQuery, not a real predicate, so callers MUST pass Sort.unsorted()
     * when using this (Spring Data only overwrites the query's ORDER BY itself
     * when the given Sort.isSorted(), so an unsorted Pageable leaves this intact).
     * Guarded against Spring Data's separate COUNT query pass (result type Long),
     * which cannot have an ORDER BY.
     */
    public static Specification<Product> orderByMinPrice(boolean ascending) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                Subquery<BigDecimal> sub = query.subquery(BigDecimal.class);
                var variantRoot = sub.from(ProductVariant.class);
                sub.select(cb.min(variantRoot.get("price")));
                sub.where(
                        cb.equal(variantRoot.get("product"), root),
                        cb.notEqual(variantRoot.get("status"), VariantStatus.INACTIVE));
                query.orderBy(ascending ? cb.asc(sub) : cb.desc(sub));
            }
            return cb.conjunction();
        };
    }

    /**
     * Phase N3 "ban chay" (best-selling) sort: correlated SUM(order_items.quantity)
     * across every order EXCEPT CANCELLED ones (explicit requirement - a
     * cancelled order's items were never actually sold). A product with zero
     * matching rows gets COALESCE'd to 0 rather than NULL, so it still sorts
     * (last, under descending) instead of being pushed to some undefined spot.
     * Tie-broken by id ascending - the literal "fallback deterministic"
     * requirement for products with no sales data at all (every such product
     * ties at 0, and without a tiebreaker Postgres's row order for equal sort
     * keys is not guaranteed stable across requests, which would corrupt
     * pagination - a row could appear on two pages or neither).
     */
    public static Specification<Product> orderByBestSelling() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                Subquery<Long> sub = query.subquery(Long.class);
                var itemRoot = sub.from(OrderItem.class);
                Join<OrderItem, Order> orderJoin = itemRoot.join("order", JoinType.INNER);
                sub.select(cb.coalesce(cb.sumAsLong(itemRoot.get("quantity")), 0L));
                sub.where(
                        cb.equal(itemRoot.get("product"), root),
                        cb.notEqual(orderJoin.get("orderStatus"), OrderStatus.CANCELLED));
                query.orderBy(cb.desc(sub), cb.asc(root.get("id")));
            }
            return cb.conjunction();
        };
    }

    private interface VariantPredicate {
        jakarta.persistence.criteria.Predicate apply(
                jakarta.persistence.criteria.Root<ProductVariant> variantRoot,
                jakarta.persistence.criteria.CriteriaBuilder cb);
    }

    private static Specification<Product> variantExists(VariantPredicate extra) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var variantRoot = subquery.from(ProductVariant.class);
            subquery.select(cb.literal(1L));
            subquery.where(
                    cb.equal(variantRoot.get("product"), root),
                    cb.notEqual(variantRoot.get("status"), VariantStatus.INACTIVE),
                    extra.apply(variantRoot, cb));
            return cb.exists(subquery);
        };
    }
}
