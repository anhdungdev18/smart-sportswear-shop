package com.dunghaiquyen.ecommerce.modules.coupon.repository;

import com.dunghaiquyen.ecommerce.modules.coupon.entity.Coupon;
import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    boolean existsByCode(String code);

    Page<Coupon> findAllByStatus(CouponStatus status, Pageable pageable);

    /**
     * Locked, not a plain read: checkout must serialize concurrent uses of the
     * SAME coupon code so usage_count/usage_limit and per-user-limit checks
     * never race (same reasoning as CartRepository.findByUserIdForUpdate /
     * ProductVariantRepository.findByIdForUpdate). code is always looked up
     * upper-cased/trimmed by the caller (see CouponService), matching how it
     * was stored at creation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.code = :code")
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
}
