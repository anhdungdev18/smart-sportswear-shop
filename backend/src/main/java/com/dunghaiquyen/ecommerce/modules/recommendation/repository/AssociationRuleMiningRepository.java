package com.dunghaiquyen.ecommerce.modules.recommendation.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AssociationRuleMiningRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssociationRuleMiningRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderProductRow> findOrderProductRowsForTraining(Collection<OrderStatus> orderStatuses) {
        String sql = """
                SELECT oi.order_id, oi.product_id
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE o.order_status IN (:orderStatuses)
                  AND p.status = 'ACTIVE'
                ORDER BY oi.order_id, oi.product_id
                """;

        List<String> statusNames = orderStatuses.stream()
                .map(Enum::name)
                .toList();

        return jdbcTemplate.query(
                sql,
                Map.of("orderStatuses", statusNames),
                (rs, rowNum) -> new OrderProductRow(
                        rs.getObject("order_id", UUID.class),
                        rs.getObject("product_id", UUID.class)
                )
        );
    }

    public record OrderProductRow(UUID orderId, UUID productId) {
    }
}