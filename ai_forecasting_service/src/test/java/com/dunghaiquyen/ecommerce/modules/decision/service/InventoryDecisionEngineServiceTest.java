package com.dunghaiquyen.ecommerce.modules.decision.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskLevel;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastModelEvaluation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InventoryDecisionEngineServiceTest {

    private final InventoryDecisionEngineService service =
            new InventoryDecisionEngineService(null, null, null, null, null);

    @Test
    void stockoutDecisionAppliesSafetyStockMoqAndPackSize() {
        var response = service.toRisk(
                variant(30, 10),
                forecast("2.0", "1.5", ForecastConfidence.MEDIUM),
                policy(7, 30, "0.950", 10, 12),
                evaluation("INTERMITTENT"),
                0).orElseThrow();

        assertThat(response.risk()).isEqualTo(InventoryRiskType.STOCKOUT);
        assertThat(response.severity()).isEqualTo(InventoryRiskLevel.HIGH);
        assertThat(response.availableQuantity()).isEqualTo(20);
        assertThat(response.expectedDemandDuringLeadTime()).isEqualTo(14);
        assertThat(response.safetyStock()).isEqualTo(7);
        assertThat(response.reorderPoint()).isEqualTo(21);
        assertThat(response.suggestedQuantity()).isEqualTo(72);
        assertThat(response.formula().rawSuggestedQuantity()).isEqualTo(61);
        assertThat(response.formula().packSize()).isEqualTo(12);
        assertThat(response.reasons()).anyMatch(reason -> reason.contains("MOQ 10"));
    }

    @Test
    void zeroStockIsCriticalWhenDemandExists() {
        var response = service.toRisk(
                variant(0, 0),
                forecast("3.0", "0.5", ForecastConfidence.HIGH),
                policy(5, 14, "0.950", 1, 1),
                evaluation("SMOOTH"),
                0).orElseThrow();

        assertThat(response.risk()).isEqualTo(InventoryRiskType.STOCKOUT);
        assertThat(response.severity()).isEqualTo(InventoryRiskLevel.CRITICAL);
        assertThat(response.estimatedStockoutDays()).isZero();
        assertThat(response.stockoutProbability()).isEqualTo(0.95);
    }

    @Test
    void zeroDemandDoesNotDivideByZeroAndFlagsOverstockOnlyWhenInventoryExists() {
        var withInventory = service.toRisk(
                variant(18, 0),
                forecast("0", null, ForecastConfidence.LOW),
                policy(7, 30, "0.950", 1, 6),
                evaluation("NO_DEMAND"),
                0).orElseThrow();

        assertThat(withInventory.risk()).isEqualTo(InventoryRiskType.OVERSTOCK);
        assertThat(withInventory.estimatedStockoutDays()).isNull();
        assertThat(withInventory.suggestedQuantity()).isZero();
        assertThat(withInventory.overstock().deadStockDays()).isEqualTo(999);

        var withoutInventory = service.toRisk(
                variant(0, 0),
                forecast("0", null, ForecastConfidence.LOW),
                policy(7, 30, "0.950", 1, 6),
                evaluation("NO_DEMAND"),
                0).orElseThrow();
        assertThat(withoutInventory.risk()).isEqualTo(InventoryRiskType.BALANCED);
    }

    @Test
    void invalidLeadTimeIsNormalizedAndWarned() {
        InventoryPolicy policy = policy(0, 30, "0.950", 1, 5);
        var response = service.toRisk(
                variant(5, 0),
                forecast("1.0", null, ForecastConfidence.MEDIUM),
                policy,
                evaluation("INTERMITTENT"),
                0).orElseThrow();

        assertThat(response.formula().leadTimeDays()).isEqualTo(1);
        assertThat(response.warnings()).contains("Lead time was invalid and normalized to 1 day.");
    }

    @Test
    void insufficientForecastDoesNotProduceAutomaticDecision() {
        var response = service.toRisk(
                variant(5, 0),
                forecast("1.0", null, ForecastConfidence.INSUFFICIENT),
                policy(7, 30, "0.950", 1, 1),
                evaluation("INSUFFICIENT_DATA"),
                0).orElseThrow();

        assertThat(response.risk()).isEqualTo(InventoryRiskType.INSUFFICIENT_DATA);
        assertThat(response.suggestedQuantity()).isZero();
        assertThat(response.warnings()).anyMatch(warning -> warning.contains("insufficient"));
    }

    private VariantSnapshot variant(int stock, int reserved) {
        return new VariantSnapshot(UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Demo Product", "M", "Black", stock, reserved);
    }

    private ForecastRun forecast(String averageDemand, String residualStdDev, ForecastConfidence confidence) {
        ForecastRun run = new ForecastRun();
        run.setVariantId(UUID.randomUUID());
        run.setAlgorithm(ForecastAlgorithmType.ROBUST_MEDIAN);
        run.setTrainingFrom(LocalDate.now().minusDays(180));
        run.setTrainingTo(LocalDate.now());
        run.setForecastHorizonDays(37);
        run.setAverageDailyDemand(new BigDecimal(averageDemand));
        run.setForecastQuantity(BigDecimal.ZERO);
        if (residualStdDev != null) {
            run.setResidualStdDev(new BigDecimal(residualStdDev));
        }
        run.setConfidence(confidence);
        run.setGeneratedAt(Instant.now());
        return run;
    }

    private InventoryPolicy policy(int leadTime, int cover, String serviceLevel, int moq, int packSize) {
        InventoryPolicy policy = new InventoryPolicy();
        policy.setVariantId(UUID.randomUUID());
        policy.setLeadTimeDays(leadTime);
        policy.setTargetCoverDays(cover);
        policy.setServiceLevel(new BigDecimal(serviceLevel));
        policy.setMinimumOrderQuantity(moq);
        policy.setPackSize(packSize);
        policy.setSupplierName("Demo Supplier");
        policy.setActive(true);
        return policy;
    }

    private ForecastModelEvaluation evaluation(String demandPattern) {
        ForecastModelEvaluation evaluation = new ForecastModelEvaluation();
        evaluation.setDemandPattern(demandPattern);
        return evaluation;
    }
}
