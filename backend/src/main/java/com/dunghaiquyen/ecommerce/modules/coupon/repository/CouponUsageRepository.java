package com.dunghaiquyen.ecommerce.modules.coupon.repository;

import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponUsage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    long countByCouponIdAndUserId(UUID couponId, UUID userId);
}
