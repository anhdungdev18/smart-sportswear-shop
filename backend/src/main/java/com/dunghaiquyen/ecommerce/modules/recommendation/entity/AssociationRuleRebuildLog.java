package com.dunghaiquyen.ecommerce.modules.recommendation.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "association_rule_rebuild_logs")
@Getter
@Setter
public class AssociationRuleRebuildLog extends AbstractAuditEntity {

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RebuildStatus status;

    @Column(name = "total_transactions", nullable = false)
    private long totalTransactions;

    @Column(name = "total_rules", nullable = false)
    private long totalRules;

    @Column(name = "min_support", nullable = false)
    private double minSupport;

    @Column(name = "min_confidence", nullable = false)
    private double minConfidence;

    @Column(name = "min_lift", nullable = false)
    private double minLift;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
}