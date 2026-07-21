package com.dunghaiquyen.ecommerce.modules.replenishment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "forecast_model_evaluations")
public class ForecastModelEvaluation {

    @Id
    @Column(name = "variant_id")
    private UUID variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "best_algorithm", nullable = false)
    private ForecastAlgorithmType bestAlgorithm;

    private BigDecimal mae;
    private BigDecimal wape;

    @Column(name = "residual_std_dev")
    private BigDecimal residualStdDev;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastConfidence confidence;

    @Column(name = "last_evaluated_at", nullable = false)
    private Instant lastEvaluatedAt;

    @Column(name = "fallback_reason")
    private String fallbackReason;

    @Column(name = "algorithm_version", nullable = false)
    private Integer algorithmVersion = 1;

    // Getters and setters
    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }
    public ForecastAlgorithmType getBestAlgorithm() { return bestAlgorithm; }
    public void setBestAlgorithm(ForecastAlgorithmType bestAlgorithm) { this.bestAlgorithm = bestAlgorithm; }
    public BigDecimal getMae() { return mae; }
    public void setMae(BigDecimal mae) { this.mae = mae; }
    public BigDecimal getWape() { return wape; }
    public void setWape(BigDecimal wape) { this.wape = wape; }
    public BigDecimal getResidualStdDev() { return residualStdDev; }
    public void setResidualStdDev(BigDecimal residualStdDev) { this.residualStdDev = residualStdDev; }
    public ForecastConfidence getConfidence() { return confidence; }
    public void setConfidence(ForecastConfidence confidence) { this.confidence = confidence; }
    public Instant getLastEvaluatedAt() { return lastEvaluatedAt; }
    public void setLastEvaluatedAt(Instant lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; }
    public String getFallbackReason() { return fallbackReason; }
    public void setFallbackReason(String fallbackReason) { this.fallbackReason = fallbackReason; }
    public Integer getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(Integer algorithmVersion) { this.algorithmVersion = algorithmVersion; }
}
