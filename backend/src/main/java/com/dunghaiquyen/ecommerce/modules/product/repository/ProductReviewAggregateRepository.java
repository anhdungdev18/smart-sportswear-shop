package com.dunghaiquyen.ecommerce.modules.product.repository;

import com.dunghaiquyen.ecommerce.modules.product.dto.ReviewAggregateProjection;
import com.dunghaiquyen.ecommerce.modules.review.entity.ProductReview;
import com.dunghaiquyen.ecommerce.modules.review.entity.ReviewStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Dedicated repository for ProductReview, owned by the product module (same
 * "dedicated repository per consuming module" pattern as
 * modules/review/repository/ReviewOrderItemRepository and
 * modules/coupon/repository/CouponPromotionProductRepository): the review
 * module's own ProductReviewRepository is left untouched. Only used by
 * ProductService to build the PDP's review summary (Phase N4) - APPROVED
 * only, per the public-detail visibility rule already enforced for reviews
 * everywhere else.
 */
public interface ProductReviewAggregateRepository extends JpaRepository<ProductReview, UUID> {

    @Query("select avg(pr.rating) as avgRating, count(pr) as reviewCount "
            + "from ProductReview pr where pr.product.id = :productId and pr.status = :status")
    ReviewAggregateProjection aggregateForProduct(@Param("productId") UUID productId, @Param("status") ReviewStatus status);
}
