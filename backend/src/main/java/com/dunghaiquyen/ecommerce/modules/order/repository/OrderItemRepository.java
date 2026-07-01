package com.dunghaiquyen.ecommerce.modules.order.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * order_items snapshot everything needed for display (product name, sku,
 * size, color, unit price) directly on the row, so reading items back never
 * needs to join product/variant - only the order_id matters. No createdAt to
 * order by (OrderItem is timestamp-less by design, see its class comment);
 * ordering by id is just for a stable, deterministic display order.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findAllByOrderIdOrderByIdAsc(UUID orderId);

    List<OrderItem> findAllByOrderIdIn(List<UUID> orderIds);
}
