package com.dunghaiquyen.ecommerce.modules.coupon.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractEntity;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * V3's uq_coupon_usages_order_id (unique on order_id alone) is what actually
 * enforces "at most 1 coupon per order" at the DB level - the Phase N2
 * decision to support only a single coupon per order this phase rides
 * directly on that existing constraint, no new migration needed. No
 * updatedAt/createdAt from AbstractAuditEntity: this row is written exactly
 * once (at successful checkout) and never edited - usedAt is the one
 * timestamp that matters, set explicitly by CouponService since the column
 * is named used_at, not created_at (so the @CreatedDate auditing listener on
 * AbstractCreatedAtEntity does not apply here).
 */
@Getter
@Setter
@Entity
@Table(name = "coupon_usages")
public class CouponUsage extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "used_at", nullable = false)
    private Instant usedAt;
}
