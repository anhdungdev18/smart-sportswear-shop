package com.dunghaiquyen.ecommerce.modules.report.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderStatusCount;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueBucketRow;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Report-only read queries against {@code orders} - deliberately its own
 * repository rather than added to the order module's existing
 * {@code OrderRepository}, so report-specific data access stays fully inside
 * the report module (TASK_BREAKDOWN_PHASE1.md Phase K's "không nhét logic
 * report lung tung vào order/inventory service cũ").
 */
public interface OrderReportRepository extends JpaRepository<Order, UUID> {

    /** PHASE1_SPEC.md 6.9: "Gross revenue: tính theo đơn có payment_status = PAID". */
    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.paymentStatus = :status")
    BigDecimal sumTotalAmountByPaymentStatus(@Param("status") PaymentStatus status);

    /** PHASE1_SPEC.md 6.9: "Realized revenue: tính theo đơn có order_status = DELIVERED". */
    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.orderStatus = :status")
    BigDecimal sumTotalAmountByOrderStatus(@Param("status") OrderStatus status);

    long countByOrderStatus(OrderStatus orderStatus);

    /**
     * Both bounds are always concrete, non-null Instants (the service resolves
     * "no dateFrom/dateTo given" to a far-past/far-future sentinel before
     * calling this) - binding a literal null here for a parameter used only
     * inside an "IS NULL OR ..." clause makes Postgres unable to infer that
     * parameter's type ahead of time, which fails with
     * InvalidDataAccessResourceUsageException at query time, not compile time.
     */
    @Query("select new com.dunghaiquyen.ecommerce.modules.report.dto.OrderStatusCount(o.orderStatus, count(o)) "
            + "from Order o "
            + "where o.createdAt >= :from and o.createdAt <= :to "
            + "group by o.orderStatus")
    List<OrderStatusCount> countByStatusInRange(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * Gross revenue time series: SUM(total_amount) over PAID orders grouped into
     * day/month/year buckets. created_at (timestamptz) is shifted into the shop
     * timezone ({@link com.dunghaiquyen.ecommerce.common.time.AppTimeZone}) before
     * truncating, so a bucket boundary is local midnight, not UTC midnight. The
     * {@code :granularity} bind is the date_trunc field ("day"/"month"/"year").
     * Postgres allows GROUP BY / ORDER BY on the select alias. Only buckets that
     * actually have PAID orders are returned; the service fills the gaps with zero.
     */
    @Query(value = """
            select cast(date_trunc(cast(:granularity as text), o.created_at at time zone 'Asia/Ho_Chi_Minh') as date) as bucket,
                   coalesce(sum(o.total_amount), 0) as revenue,
                   count(*) as "orderCount"
            from orders o
            where o.payment_status = 'PAID'
              and o.created_at >= :from and o.created_at <= :to
            group by bucket
            order by bucket
            """, nativeQuery = true)
    List<RevenueBucketRow> sumRevenueByBucket(
            @Param("granularity") String granularity,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
