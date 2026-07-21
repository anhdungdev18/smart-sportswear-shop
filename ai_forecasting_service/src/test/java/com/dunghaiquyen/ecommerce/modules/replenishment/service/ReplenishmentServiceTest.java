package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReplenishmentServiceTest {
    private final ReplenishmentService service = new ReplenishmentService(new ObjectMapper());

    @Test
    void computesAvailableSafetyStockReorderPointAndPackRounding() {
        VariantSnapshot variant = variant(30, 10);
        ForecastRun forecast = forecast("2.0", "1.5");
        InventoryPolicy policy = policy(7, 30, "0.950", 10, 12);

        var result = service.generateRecommendation(variant, forecast, policy);

        assertThat(result.getAvailableQuantity()).isEqualTo(20);
        assertThat(result.getSafetyStock()).isEqualTo(7);
        assertThat(result.getReorderPoint()).isEqualTo(21);
        assertThat(result.getSuggestedQuantity()).isEqualTo(72);
        assertThat(result.getEstimatedStockoutDays()).isEqualTo(10);
        assertThat(result.getPriority()).isEqualTo(ReplenishmentPriority.HIGH);
        assertThat(result.getExplanation()).containsEntry("summary", "SKU đã chạm mức đặt hàng lại (Reorder Point).");
        Map<?, ?> formula = (Map<?, ?>) result.getExplanation().get("formula");
        assertThat(formula.get("targetStock")).isEqualTo(81);
        assertThat(formula.get("rawSuggestion")).isEqualTo(61);
        assertThat(formula.get("roundedSuggestion")).isEqualTo(72);
    }

    @Test
    void appliesMinimumOrderBeforePackRounding() {
        var result = service.generateRecommendation(variant(80, 5), forecast("2.0", "1.5"),
                policy(7, 30, "0.950", 10, 12));
        assertThat(result.getAvailableQuantity()).isEqualTo(75);
        assertThat(result.getSuggestedQuantity()).isEqualTo(12);
    }

    @Test
    void zeroDemandNeverDividesByZeroOrSuggestsNegativeQuantity() {
        var result = service.generateRecommendation(variant(5, 0), forecast("0", null),
                policy(7, 30, "0.950", 1, 6));
        assertThat(result.getEstimatedStockoutDays()).isNull();
        assertThat(result.getSafetyStock()).isZero();
        assertThat(result.getReorderPoint()).isZero();
        assertThat(result.getSuggestedQuantity()).isZero();
        assertThat(result.getPriority()).isEqualTo(ReplenishmentPriority.LOW);
    }

    private VariantSnapshot variant(int stock, int reserved) {
        return new VariantSnapshot(UUID.randomUUID(), UUID.randomUUID(), "TEST-SKU", "Test", "M", "Black", stock, reserved);
    }

    private ForecastRun forecast(String averageDemand, String residualStdDev) {
        ForecastRun run = new ForecastRun();
        run.setAverageDailyDemand(new BigDecimal(averageDemand));
        if (residualStdDev != null) run.setResidualStdDev(new BigDecimal(residualStdDev));
        return run;
    }

    private InventoryPolicy policy(int leadTime, int cover, String serviceLevel, int minimumOrder, int packSize) {
        InventoryPolicy policy = new InventoryPolicy();
        policy.setLeadTimeDays(leadTime);
        policy.setTargetCoverDays(cover);
        policy.setServiceLevel(new BigDecimal(serviceLevel));
        policy.setMinimumOrderQuantity(minimumOrder);
        policy.setPackSize(packSize);
        return policy;
    }
}