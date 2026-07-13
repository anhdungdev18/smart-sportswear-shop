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
}
