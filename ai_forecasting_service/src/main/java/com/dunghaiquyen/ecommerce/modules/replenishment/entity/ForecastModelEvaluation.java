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

    @Column(name = "data_source", nullable = false)
    private String dataSource = "REAL";

    @Column(name = "demand_pattern")
    private String demandPattern;

    private BigDecimal bias;

    @Column(name = "backtest_windows", nullable = false)
    private Integer backtestWindows = 0;

    @Column(name = "test_window_days", nullable = false)
    private Integer testWindowDays = 30;

    @Column(name = "training_from")
    private java.time.LocalDate trainingFrom;

    @Column(name = "training_to")
    private java.time.LocalDate trainingTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "benchmark_algorithm")
    private ForecastAlgorithmType benchmarkAlgorithm;

    @Column(name = "benchmark_mae")
    private BigDecimal benchmarkMae;

    @Column(name = "benchmark_wape")
    private BigDecimal benchmarkWape;

    @Column(name = "selection_reason")
    private String selectionReason;

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
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getDemandPattern() { return demandPattern; }
    public void setDemandPattern(String demandPattern) { this.demandPattern = demandPattern; }
    public BigDecimal getBias() { return bias; }
    public void setBias(BigDecimal bias) { this.bias = bias; }
    public Integer getBacktestWindows() { return backtestWindows; }
    public void setBacktestWindows(Integer backtestWindows) { this.backtestWindows = backtestWindows; }
    public Integer getTestWindowDays() { return testWindowDays; }
    public void setTestWindowDays(Integer testWindowDays) { this.testWindowDays = testWindowDays; }
    public java.time.LocalDate getTrainingFrom() { return trainingFrom; }
    public void setTrainingFrom(java.time.LocalDate trainingFrom) { this.trainingFrom = trainingFrom; }
    public java.time.LocalDate getTrainingTo() { return trainingTo; }
    public void setTrainingTo(java.time.LocalDate trainingTo) { this.trainingTo = trainingTo; }
    public ForecastAlgorithmType getBenchmarkAlgorithm() { return benchmarkAlgorithm; }
    public void setBenchmarkAlgorithm(ForecastAlgorithmType benchmarkAlgorithm) { this.benchmarkAlgorithm = benchmarkAlgorithm; }
    public BigDecimal getBenchmarkMae() { return benchmarkMae; }
    public void setBenchmarkMae(BigDecimal benchmarkMae) { this.benchmarkMae = benchmarkMae; }
    public BigDecimal getBenchmarkWape() { return benchmarkWape; }
    public void setBenchmarkWape(BigDecimal benchmarkWape) { this.benchmarkWape = benchmarkWape; }
    public String getSelectionReason() { return selectionReason; }
    public void setSelectionReason(String selectionReason) { this.selectionReason = selectionReason; }
}
