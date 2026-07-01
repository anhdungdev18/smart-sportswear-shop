package com.dunghaiquyen.ecommerce.modules.coupon.repository;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionProduct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Dedicated repository for PromotionProduct, owned by the coupon module (same
 * "dedicated repository per consuming module" pattern as
 * modules/review/repository/ReviewOrderItemRepository): CouponService only
 * ever needs the eligible product-id set for a PRODUCT-scope promotion at
 * checkout time, so it gets its own read-only query here instead of
 * depending on the promotion module's own PromotionProductRepository.
 */
public interface CouponPromotionProductRepository extends JpaRepository<PromotionProduct, UUID> {

    @Query("select pp.product.id from PromotionProduct pp where pp.promotion.id = :promotionId")
    List<UUID> findProductIdsByPromotionId(@Param("promotionId") UUID promotionId);
}
