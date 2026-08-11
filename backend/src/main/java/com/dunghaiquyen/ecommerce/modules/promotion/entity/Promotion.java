package com.dunghaiquyen.ecommerce.modules.promotion.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * A promotion campaign. For this feature we drive product-level percentage
 * discounts (scope=PRODUCT, type=PERCENTAGE) with a start/end window: while a
 * promotion is ACTIVE and now is within [startsAt, endsAt], its products show a
 * sale price; once endsAt passes the discount simply stops applying (prices
 * revert automatically — no scheduled job needed for the storefront view).
 */
@Getter
@Setter
@Entity
@Table(name = "promotions")
public class Promotion extends AbstractAuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionType type = PromotionType.PERCENTAGE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionScope scope = PromotionScope.PRODUCT;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionStatus status = PromotionStatus.DRAFT;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "created_by")
    private UUID createdBy;
}
