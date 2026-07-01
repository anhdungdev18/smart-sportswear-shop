package com.dunghaiquyen.ecommerce.modules.review.repository;

import com.dunghaiquyen.ecommerce.modules.review.entity.ProductReview;
import com.dunghaiquyen.ecommerce.modules.review.entity.ReviewStatus;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    /** Fetch-joins user so the public/admin list does not lazy-load it per row (N+1). */
    @Query("select pr from ProductReview pr join fetch pr.user u "
            + "where pr.product.id = :productId and pr.status = :status")
    Page<ProductReview> findAllByProductIdAndStatus(
            @Param("productId") UUID productId, @Param("status") ReviewStatus status, Pageable pageable);

    @Query(
            value = "select pr from ProductReview pr join fetch pr.user u join fetch pr.product p order by pr.createdAt desc",
            countQuery = "select count(pr) from ProductReview pr")
    Page<ProductReview> findAllForAdmin(Pageable pageable);

    @Query("select pr from ProductReview pr join fetch pr.user u join fetch pr.product p where pr.id = :id")
    Optional<ProductReview> findDetailById(@Param("id") UUID id);
}
