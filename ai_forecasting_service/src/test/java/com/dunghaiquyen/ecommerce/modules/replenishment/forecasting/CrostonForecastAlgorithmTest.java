package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CrostonForecastAlgorithmTest {

    private final CrostonForecastAlgorithm algorithm = new CrostonForecastAlgorithm();

    @Test
    void testForecast_withEmptyHistory() {
        ForecastResult result = algorithm.forecast(Collections.emptyList(), 7);
        assertThat(result.averageDailyDemand()).isEqualTo(0.0);
    }

    @Test
    void testForecast_withAllZeros() {
        List<Integer> demand = List.of(0, 0, 0, 0);
        ForecastResult result = algorithm.forecast(demand, 1);
        assertThat(result.averageDailyDemand()).isEqualTo(0.0);
    }

    @Test
    void testForecast_intermittentDemand() {
        // First non-zero is 10. Z=10, P=2.
        // Then 0 -> q=2
        // Then 20 -> Z = 0.1*20 + 0.9*10 = 2 + 9 = 11. P = 0.1*2 + 0.9*2 = 0.2 + 1.8 = 2.0.
        List<Integer> demand = List.of(0, 10, 0, 20);
        ForecastResult result = algorithm.forecast(demand, 1);
        
        // Final avg = Z/P = 11 / 2.0 = 5.5
        assertThat(result.averageDailyDemand()).isCloseTo(5.5, org.assertj.core.data.Offset.offset(0.001));
    }
}
