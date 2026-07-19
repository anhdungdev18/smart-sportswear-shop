package com.dunghaiquyen.ecommerce.modules.replenishment.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "forecast_runs")
@EntityListeners(AuditingEntityListener.class)
public class ForecastRun extends AbstractEntity {

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ForecastAlgorithmType algorithm;

    @Column(name = "training_from", nullable = false)
    private LocalDate trainingFrom;

    @Column(name = "training_to", nullable = false)
    private LocalDate trainingTo;

    @Column(name = "forecast_horizon_days", nullable = false)
    private int forecastHorizonDays;

    @Column(name = "average_daily_demand", nullable = false, precision = 12, scale = 4)
    private BigDecimal averageDailyDemand;

    @Column(name = "forecast_quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal forecastQuantity;

    @Column(precision = 12, scale = 4)
    private BigDecimal mae;

    @Column(precision = 12, scale = 6)
    private BigDecimal wape;

    @Column(name = "residual_std_dev", precision = 12, scale = 4)
    private BigDecimal residualStdDev;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ForecastConfidence confidence;

    @CreatedDate
    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;
}
