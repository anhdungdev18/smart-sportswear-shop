package com.dunghaiquyen.ecommerce.modules.report.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.report.dto.CustomerSalesResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerReportRepository extends JpaRepository<Order, UUID> {

    @Query(value = "select new com.dunghaiquyen.ecommerce.modules.report.dto.CustomerSalesResponse("
            + "u.id, u.fullName, u.email, count(o), sum(o.totalAmount)) "
            + "from Order o join o.user u "
            + "where o.orderStatus <> :excludedStatus "
            + "and o.createdAt >= :from and o.createdAt <= :to "
            + "group by u.id, u.fullName, u.email "
            + "order by sum(o.totalAmount) desc",
            countQuery = "select count(distinct u.id) from Order o join o.user u "
                    + "where o.orderStatus <> :excludedStatus "
                    + "and o.createdAt >= :from and o.createdAt <= :to")
    Page<CustomerSalesResponse> findCustomerSales(
            @Param("excludedStatus") OrderStatus excludedStatus,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
