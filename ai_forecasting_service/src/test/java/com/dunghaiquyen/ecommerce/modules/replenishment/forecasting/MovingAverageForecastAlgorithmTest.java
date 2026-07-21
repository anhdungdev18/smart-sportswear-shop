package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MovingAverageForecastAlgorithmTest {

    private final MovingAverageForecastAlgorithm algorithm = new MovingAverageForecastAlgorithm();

    @Test
    void testForecast_withEmptyHistory() {
        ForecastResult result = algorithm.forecast(Collections.emptyList(), 7);
        assertThat(result.averageDailyDemand()).isEqualTo(0.0);
        assertThat(result.forecastQuantity()).isEqualTo(0.0);
        assertThat(result.dailyForecast()).hasSize(7).containsOnly(0.0);
    }

    @Test
    void testForecast_withValidHistory() {
        List<Integer> demand = List.of(2, 4, 6, 8, 10);
        ForecastResult result = algorithm.forecast(demand, 5);
        // sum = 30, days = 5 => avg = 6.0
        assertThat(result.averageDailyDemand()).isEqualTo(6.0);
        assertThat(result.forecastQuantity()).isEqualTo(30.0);
    }

    @Test
    void testForecast_withNulls() {
        List<Integer> demand = java.util.Arrays.asList(2, null, 6, null, 10);
        ForecastResult result = algorithm.forecast(demand, 5);
        // sum = 18, days = 5 => avg = 3.6
        assertThat(result.averageDailyDemand()).isEqualTo(3.6);
        assertThat(result.forecastQuantity()).isEqualTo(18.0);
    }

    @Test
    void testForecast_moreThanWindowSize() {
        // window size is 30, so let's provide 32 items
        List<Integer> demand = new java.util.ArrayList<>();
        demand.add(100);
        demand.add(200);
        for (int i = 0; i < 30; i++) {
            demand.add(10);
        }
        ForecastResult result = algorithm.forecast(demand, 5);
        // Should only use last 30 items, sum = 300, avg = 10
        assertThat(result.averageDailyDemand()).isEqualTo(10.0);
    }

    @Test
    void testForecast_negativeHorizon() {
        assertThatThrownBy(() -> algorithm.forecast(List.of(1, 2), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
