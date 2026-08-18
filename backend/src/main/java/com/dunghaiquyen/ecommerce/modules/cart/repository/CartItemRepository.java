package com.dunghaiquyen.ecommerce.modules.cart.repository;

import com.dunghaiquyen.ecommerce.modules.cart.entity.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndVariantId(UUID cartId, UUID variantId);

    /** Fetch-joins variant+product so assembling a cart response does not lazy-load them per row (N+1). */
    @Query("select ci from CartItem ci join fetch ci.variant v join fetch v.product p "
            + "where ci.cart.id = :cartId order by ci.createdAt desc, ci.id desc")
    List<CartItem> findAllByCartIdWithVariantAndProduct(@Param("cartId") UUID cartId);

    /**
     * Deliberately NOT fetch-joined: OrderService's checkout flow only ever
     * needs each item's variant id here (free off the lazy proxy, no extra
     * query) before pessimistic-locking that variant itself. Eagerly loading
     * the full (unlocked, versioned) ProductVariant via a fetch-join first and
     * then separately re-locking the same row later in the same transaction
     * is exactly what caused ObjectOptimisticLockingFailureException under
     * concurrent checkout - Hibernate detects the row's version moved between
     * the two reads (while this transaction was blocked waiting for the
     * other's lock) and reports it as a conflict, even though the locked read
     * is intentionally the authoritative one.
     */
    List<CartItem> findAllByCartId(UUID cartId);

    void deleteAllByVariantIdIn(List<UUID> variantIds);
}
