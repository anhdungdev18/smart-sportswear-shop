package com.dunghaiquyen.ecommerce.modules.review.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Dedicated repository for OrderItem, owned by the review module (same
 * pattern as the report module's OrderItemReportRepository): the order
 * module's own OrderItemRepository is left untouched, so this query need
 * never risk breaking it.
 */
public interface ReviewOrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * "Successfully purchased" = the order reached DELIVERED. Excludes order
     * items that already have a review attached (uq_product_reviews_user_order_item
     * would reject the insert anyway, but checking here lets ReviewService
     * return a clean 422 instead of a raw constraint violation). Ordered by id
     * so picking the first eligible item is deterministic.
     */
    @Query("select oi from OrderItem oi "
            + "where oi.order.user.id = :userId and oi.product.id = :productId "
            + "and oi.order.orderStatus = :status "
            + "and not exists (select 1 from ProductReview pr where pr.orderItem = oi) "
            + "order by oi.id asc")
    List<OrderItem> findEligibleUnreviewed(
            @Param("userId") UUID userId, @Param("productId") UUID productId, @Param("status") OrderStatus status);
}
