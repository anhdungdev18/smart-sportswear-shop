package com.dunghaiquyen.ecommerce.modules.coupon.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.Promotion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * promotion is nullable at the DB level (V3: "on delete set null", so an
 * admin deleting a promotion later does not orphan-break this row) but
 * required by CouponService.create - a coupon's discount type/amount/scope
 * always comes from its promotion, so a promotion-less coupon could never be
 * applied at checkout anyway. code is always stored upper-cased/trimmed (see
 * CouponService) so lookups stay a plain equality match against
 * uq_coupons_code, no upper()/lower() needed in queries.
 */
@Getter
@Setter
@Entity
@Table(name = "coupons")
public class Coupon extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status = CouponStatus.ACTIVE;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;
}
