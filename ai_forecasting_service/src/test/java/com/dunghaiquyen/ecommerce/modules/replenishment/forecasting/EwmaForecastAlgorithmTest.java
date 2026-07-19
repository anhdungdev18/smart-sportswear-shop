package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EwmaForecastAlgorithmTest {

    private final EwmaForecastAlgorithm algorithm = new EwmaForecastAlgorithm();

    @Test
    void testForecast_withEmptyHistory() {
        ForecastResult result = algorithm.forecast(Collections.emptyList(), 7);
        assertThat(result.averageDailyDemand()).isEqualTo(0.0);
    }

    @Test
    void testForecast_withValidHistory() {
        List<Integer> demand = List.of(10, 20, 30);
        ForecastResult result = algorithm.forecast(demand, 1);
        // alpha = 0.3
        // S0 = 10
        // S1 = 0.3 * 20 + 0.7 * 10 = 6 + 7 = 13
        // S2 = 0.3 * 30 + 0.7 * 13 = 9 + 9.1 = 18.1
        assertThat(result.averageDailyDemand()).isCloseTo(18.1, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testForecast_doesNotReturnNaN() {
        java.util.List<Integer> demand = new java.util.ArrayList<>();
        demand.add(null);
        demand.add(20);
        ForecastResult result = algorithm.forecast(demand, 1);
        assertThat(result.averageDailyDemand()).isNotNaN();
        assertThat(result.averageDailyDemand()).isGreaterThan(0.0);
    }
}
