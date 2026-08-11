package com.dunghaiquyen.ecommerce.modules.promotion.repository;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.Promotion;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    boolean existsBySlug(String slug);

    /**
     * Best active percentage discount per product for the given ids. Active =
     * status ACTIVE, PERCENTAGE product-scope, and now inside [starts_at, ends_at]
     * (null bounds mean open-ended). Native SQL keeps the join to the join-table
     * and the enum-as-string comparison simple.
     */
    @Query(value = """
            select pp.product_id as productId, max(p.discount_percent) as percent
            from promotions p
            join promotion_products pp on pp.promotion_id = p.id
            where p.status = 'ACTIVE' and p.type = 'PERCENTAGE' and p.scope = 'PRODUCT'
              and (p.starts_at is null or p.starts_at <= :now)
              and (p.ends_at is null or p.ends_at >= :now)
              and pp.product_id in (:ids)
            group by pp.product_id
            """, nativeQuery = true)
    List<ProductPromoDiscount> findActivePercentDiscounts(
            @Param("now") Instant now, @Param("ids") Collection<UUID> ids);

    /** All active product-scope percentage promotions, soonest-ending first (for the storefront countdown). */
    @Query(value = """
            select * from promotions
            where status = 'ACTIVE' and type = 'PERCENTAGE' and scope = 'PRODUCT'
              and (starts_at is null or starts_at <= :now)
              and (ends_at is null or ends_at >= :now)
            order by ends_at asc nulls last
            """, nativeQuery = true)
    List<Promotion> findActiveProductPromotions(@Param("now") Instant now);

    List<Promotion> findAllByOrderByCreatedAtDesc();

    /** Product ids covered by any currently-active product-scope percentage promotion. */
    @Query(value = """
            select distinct pp.product_id
            from promotion_products pp
            join promotions p on p.id = pp.promotion_id
            where p.status = 'ACTIVE' and p.type = 'PERCENTAGE' and p.scope = 'PRODUCT'
              and (p.starts_at is null or p.starts_at <= :now)
              and (p.ends_at is null or p.ends_at >= :now)
            """, nativeQuery = true)
    List<UUID> findActivePromotionProductIds(@Param("now") Instant now);
}
