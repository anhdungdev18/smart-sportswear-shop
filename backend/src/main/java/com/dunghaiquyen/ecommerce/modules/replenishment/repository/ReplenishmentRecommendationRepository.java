package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplenishmentRecommendationRepository
        extends JpaRepository<ReplenishmentRecommendation, UUID> {

    Optional<ReplenishmentRecommendation> findByVariantIdAndStatus(UUID variantId, ReplenishmentStatus status);

    List<ReplenishmentRecommendation> findAllByStatusOrderByCreatedAtDesc(ReplenishmentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM ReplenishmentRecommendation r " +
           "JOIN r.variant v " +
           "JOIN v.product p " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:priority IS NULL OR r.priority = :priority) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    org.springframework.data.domain.Page<ReplenishmentRecommendation> searchRecommendations(
            @org.springframework.data.repository.query.Param("status") ReplenishmentStatus status,
            @org.springframework.data.repository.query.Param("priority") com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority priority,
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);
}
