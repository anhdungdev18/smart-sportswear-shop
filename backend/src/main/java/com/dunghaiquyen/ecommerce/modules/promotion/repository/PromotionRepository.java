package com.dunghaiquyen.ecommerce.modules.promotion.repository;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.Promotion;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Promotion> findAllByStatus(PromotionStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Promotion p where p.id = :id")
    java.util.Optional<Promotion> findByIdForUpdate(@Param("id") UUID id);

    /**
     * "Active" for the public listing means status=ACTIVE AND currently
     * within its own time window (open-ended bounds treated as unbounded,
     * same null-means-unbounded convention CouponService.validate already
     * uses for promotion.startsAt/endsAt). Deliberately does NOT also check
     * usageLimit/usageCount: a campaign that already hit its usage cap is
     * still "running" in the calendar sense - whether a given coupon under
     * it can still be redeemed is CouponService.validate's call to make, not
     * this listing's.
     */
    @Query("select p from Promotion p where p.status = :status "
            + "and (p.startsAt is null or p.startsAt <= :now) "
            + "and (p.endsAt is null or p.endsAt >= :now) "
            + "order by p.createdAt desc")
    List<Promotion> findAllActive(@Param("status") PromotionStatus status, @Param("now") Instant now);
}
