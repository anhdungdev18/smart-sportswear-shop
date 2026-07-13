package com.dunghaiquyen.ecommerce.modules.replenishment.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_policies")
public class InventoryPolicy extends AbstractAuditEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false, unique = true)
    private ProductVariant variant;

    @Column(name = "lead_time_days", nullable = false)
    private int leadTimeDays = 7;

    @Column(name = "target_cover_days", nullable = false)
    private int targetCoverDays = 30;

    @Column(name = "service_level", nullable = false, precision = 4, scale = 3)
    private BigDecimal serviceLevel = new BigDecimal("0.950");

    @Column(name = "minimum_order_quantity", nullable = false)
    private int minimumOrderQuantity = 1;

    @Column(name = "pack_size", nullable = false)
    private int packSize = 1;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(nullable = false)
    private boolean active = true;
}
