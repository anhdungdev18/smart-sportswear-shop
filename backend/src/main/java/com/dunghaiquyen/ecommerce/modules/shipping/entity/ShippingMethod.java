package com.dunghaiquyen.ecommerce.modules.shipping.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Maps 1:1 onto the shipping_methods table already created by V3 - no new migration needed for this entity. */
@Getter
@Setter
@Entity
@Table(name = "shipping_methods")
public class ShippingMethod extends AbstractAuditEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShippingMethodStatus status = ShippingMethodStatus.ACTIVE;

    /** Carrier display name (e.g. "GHN", "Viettel Post") - V3's column name, reused as-is rather than adding a new "carrierName" column. */
    @Column(length = 50)
    private String provider;

    @Column(name = "base_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee = BigDecimal.ZERO;

    @Column(name = "estimated_days_min")
    private Integer estimatedDaysMin;

    @Column(name = "estimated_days_max")
    private Integer estimatedDaysMax;
}
