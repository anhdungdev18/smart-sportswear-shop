package com.dunghaiquyen.ecommerce.modules.recommendation.repository;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationEventType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationLog;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, UUID> {

    long countByCreatedAtGreaterThanEqual(Instant from);

    long countByEventTypeAndCreatedAtGreaterThanEqual(
            RecommendationEventType eventType,
            Instant from
    );

    @Query(value = """
            SELECT
                rl.recommended_product_id AS "productId",
                p.name AS "productName",
                COUNT(*) FILTER (WHERE rl.event_type = 'IMPRESSION') AS "impressions",
                COUNT(*) FILTER (WHERE rl.event_type = 'CLICK') AS "clicks",
                COUNT(*) FILTER (WHERE rl.event_type = 'ADD_TO_CART') AS "addToCarts"
            FROM recommendation_logs rl
            JOIN products p ON p.id = rl.recommended_product_id
            WHERE rl.created_at >= :from
            GROUP BY rl.recommended_product_id, p.name
            ORDER BY
                COUNT(*) FILTER (WHERE rl.event_type = 'IMPRESSION') DESC,
                COUNT(*) FILTER (WHERE rl.event_type = 'CLICK') DESC,
                COUNT(*) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<RecommendationLogProductStatsProjection> findTopRecommendedProducts(
            @Param("from") Instant from,
            @Param("limit") int limit
    );
}