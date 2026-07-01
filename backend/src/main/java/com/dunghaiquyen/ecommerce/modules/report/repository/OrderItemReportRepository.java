package com.dunghaiquyen.ecommerce.modules.report.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.report.dto.BestSellingProductResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Report-only read queries against {@code order_items} - see OrderReportRepository's javadoc for why this is separate. */
public interface OrderItemReportRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * Aggregates REAL order_items (never cart_items or in-flight reservations).
     * Excludes items belonging to a CANCELLED order: a cancelled order never
     * actually sold anything, so counting its items would inflate "best
     * selling" with phantom sales - not stated explicitly in the spec, but the
     * only reading consistent with "rule bán chạy phải dựa trên order items
     * thật" (a cancelled order's items are not a real sale).
     */
    @Query("select new com.dunghaiquyen.ecommerce.modules.report.dto.BestSellingProductResponse("
            + "p.id, p.name, sum(oi.quantity), sum(oi.lineTotal)) "
            + "from OrderItem oi join oi.product p "
            + "where oi.order.orderStatus <> :excludedStatus "
            + "group by p.id, p.name "
            + "order by sum(oi.quantity) desc")
    List<BestSellingProductResponse> findBestSelling(@Param("excludedStatus") OrderStatus excludedStatus, Pageable pageable);
}
