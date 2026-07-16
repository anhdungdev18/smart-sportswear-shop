package com.dunghaiquyen.ecommerce.modules.combo.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractCreatedAtEntity;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One required product of a {@link Combo}: the combo applies only when the cart contains every one of these. */
@Getter
@Setter
@Entity
@Table(name = "combo_products")
public class ComboProduct extends AbstractCreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity = 1;
}
