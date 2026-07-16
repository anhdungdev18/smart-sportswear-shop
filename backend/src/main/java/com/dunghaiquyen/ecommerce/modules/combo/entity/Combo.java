package com.dunghaiquyen.ecommerce.modules.combo.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * A combo/bundle: a fixed set of products ({@link #products}) that, when all
 * present in a cart, take a flat {@link #discountAmount} off the order total.
 * Orthogonal to per-variant sale pricing (price/compareAtPrice) — that stays the
 * product-level discount; a combo is the only order-level discount in the system.
 */
@Getter
@Setter
@Entity
@Table(name = "combos")
public class Combo extends AbstractAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComboStatus status = ComboStatus.ACTIVE;

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComboProduct> products = new ArrayList<>();
}
