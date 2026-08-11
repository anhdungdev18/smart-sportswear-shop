package com.dunghaiquyen.ecommerce.modules.promotion.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractCreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Join row: which products a promotion applies to. */
@Getter
@Setter
@Entity
@Table(name = "promotion_products")
public class PromotionProduct extends AbstractCreatedAtEntity {

    @Column(name = "promotion_id", nullable = false)
    private UUID promotionId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;
}
