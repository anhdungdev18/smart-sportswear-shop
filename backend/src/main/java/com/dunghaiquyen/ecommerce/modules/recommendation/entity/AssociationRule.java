package com.dunghaiquyen.ecommerce.modules.recommendation.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "association_rules")
@Getter
@Setter
public class AssociationRule extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "antecedent_product_id", nullable = false)
    private Product antecedentProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consequent_product_id", nullable = false)
    private Product consequentProduct;

    @Column(nullable = false)
    private double support;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false)
    private double lift;

    @Column(name = "antecedent_count", nullable = false)
    private long antecedentCount;

    @Column(name = "consequent_count", nullable = false)
    private long consequentCount;

    @Column(name = "pair_count", nullable = false)
    private long pairCount;

    @Column(name = "total_transactions", nullable = false)
    private long totalTransactions;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssociationRuleStatus status = AssociationRuleStatus.ACTIVE;
}