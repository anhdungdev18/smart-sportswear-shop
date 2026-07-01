package com.dunghaiquyen.ecommerce.modules.promotion.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractCreatedAtEntity;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Only meaningful for PRODUCT-scoped promotions; see Promotion/PromotionScope. */
@Getter
@Setter
@Entity
@Table(name = "promotion_products")
public class PromotionProduct extends AbstractCreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
