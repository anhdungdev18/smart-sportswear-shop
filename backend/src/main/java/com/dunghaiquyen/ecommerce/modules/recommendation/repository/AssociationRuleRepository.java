package com.dunghaiquyen.ecommerce.modules.recommendation.repository;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AssociationRuleRepository extends JpaRepository<AssociationRule, UUID> {

    @Query("""
        select r
        from AssociationRule r
        join fetch r.consequentProduct p
        join fetch p.brand
        join fetch p.category
        where r.antecedentProduct.id = :productId
          and r.status = com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus.ACTIVE
          and p.status = com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus.ACTIVE
        order by r.confidence desc, r.lift desc, r.support desc
    """)
    List<AssociationRule> findActiveRulesByAntecedentProductId(
            @Param("productId") UUID productId,
            Pageable pageable
    );

    @Query("""
        select r
        from AssociationRule r
        join fetch r.consequentProduct p
        join fetch p.brand
        join fetch p.category
        where r.antecedentProduct.id in :productIds
          and r.status = com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus.ACTIVE
          and p.status = com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus.ACTIVE
        order by r.confidence desc, r.lift desc, r.support desc
    """)
    List<AssociationRule> findActiveRulesByAntecedentProductIdIn(
            @Param("productIds") Collection<UUID> productIds
    );

    @Query("""
        select r
        from AssociationRule r
        where r.antecedentProduct.id in :sourceProductIds
          and r.consequentProduct.id = :recommendedProductId
          and r.status = com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus.ACTIVE
        order by r.confidence desc, r.lift desc, r.support desc, r.pairCount desc
    """)
    List<AssociationRule> findBestActiveRule(
            @Param("sourceProductIds") Collection<UUID> sourceProductIds,
            @Param("recommendedProductId") UUID recommendedProductId,
            Pageable pageable
    );

    @Modifying
    @Query("""
        update AssociationRule r
        set r.status = com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus.ARCHIVED
        where r.status = com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus.ACTIVE
    """)
    int archiveActiveRules();

    long countByModelVersionAndStatus(String modelVersion, AssociationRuleStatus status);
}
