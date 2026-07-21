package com.dunghaiquyen.ecommerce.modules.replenishment.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "replenishment_recommendations")
public class ReplenishmentRecommendation extends AbstractAuditEntity {

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forecast_run_id")
    private ForecastRun forecastRun;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "incoming_quantity", nullable = false)
    private int incomingQuantity;

    @Column(name = "reorder_point", nullable = false)
    private int reorderPoint;

    @Column(name = "safety_stock", nullable = false)
    private int safetyStock;

    @Column(name = "suggested_quantity", nullable = false)
    private int suggestedQuantity;

    @Column(name = "admin_quantity")
    private Integer adminQuantity;

    @Column(name = "estimated_stockout_days")
    private Integer estimatedStockoutDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReplenishmentPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReplenishmentStatus status = ReplenishmentStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> explanation = new LinkedHashMap<>();

    @Column(name = "admin_note", columnDefinition = "text")
    private String adminNote;

    @Column(name = "acted_by")
    private UUID actedBy;

    @Column(name = "acted_at")
    private Instant actedAt;
}
