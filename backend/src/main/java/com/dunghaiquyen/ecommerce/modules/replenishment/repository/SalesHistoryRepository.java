package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesHistoryRepository extends JpaRepository<OrderItem, UUID> {

    @Query(value = """
            select
                oi.variant_id as variantId,
                (o.created_at at time zone 'Asia/Ho_Chi_Minh')::date as demandDate,
                sum(oi.quantity) as quantity
            from order_items oi
            join orders o on o.id = oi.order_id
            where o.order_status in ('CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED')
              and o.created_at >= :fromInclusive
              and o.created_at < :toExclusive
              and (cast(:variantIds as uuid[]) is null or oi.variant_id = any(cast(:variantIds as uuid[])))
            group by oi.variant_id, demandDate
            order by oi.variant_id, demandDate
            """, nativeQuery = true)
    List<DailyVariantDemandProjection> aggregateDailyDemand(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive,
            @Param("variantIds") UUID[] variantIds);
}
