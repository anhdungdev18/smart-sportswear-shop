package com.dunghaiquyen.ecommerce.modules.replenishment.repository;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ReplenishmentRecommendationRepository extends JpaRepository<ReplenishmentRecommendation, UUID> {
    Optional<ReplenishmentRecommendation> findByVariantIdAndStatus(UUID variantId, ReplenishmentStatus status);
    @Query(value="""
        select r.* from replenishment_recommendations r join ai_product_variant_snapshot v on v.variant_id=r.variant_id
        where (cast(:status as text) is null or r.status=cast(:status as text)) and (cast(:priority as text) is null or r.priority=cast(:priority as text))
          and (coalesce(:keyword,'')='' or lower(v.sku) like lower(concat('%',:keyword,'%')) or lower(v.product_name) like lower(concat('%',:keyword,'%')))
        """, countQuery="""
        select count(*) from replenishment_recommendations r join ai_product_variant_snapshot v on v.variant_id=r.variant_id
        where (cast(:status as text) is null or r.status=cast(:status as text)) and (cast(:priority as text) is null or r.priority=cast(:priority as text))
          and (coalesce(:keyword,'')='' or lower(v.sku) like lower(concat('%',:keyword,'%')) or lower(v.product_name) like lower(concat('%',:keyword,'%')))
        """, nativeQuery=true)
    Page<ReplenishmentRecommendation> searchRecommendations(@Param("status") ReplenishmentStatus status, @Param("priority") ReplenishmentPriority priority, @Param("keyword") String keyword, Pageable pageable);
}
