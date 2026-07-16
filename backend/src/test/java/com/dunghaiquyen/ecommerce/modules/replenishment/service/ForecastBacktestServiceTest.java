package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.CrostonForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.EwmaForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.MovingAverageForecastAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForecastBacktestServiceTest {

    private ForecastBacktestService service;

    @BeforeEach
    void setUp() {
        List<ForecastAlgorithm> algorithms = List.of(
                new MovingAverageForecastAlgorithm(),
                new EwmaForecastAlgorithm(),
                new CrostonForecastAlgorithm());
        service = new ForecastBacktestService(algorithms);
    }

    @Test
    void runBacktest_withNotEnoughData() {
        ForecastBacktestService.BacktestResult result = service.runBacktest(List.of(1, 2, 3), 5);
        assertThat(result.confidence()).isEqualTo(ForecastConfidence.LOW);
        assertThat(result.bestAlgorithm()).isEqualTo(ForecastAlgorithmType.MOVING_AVERAGE);
    }

    @Test
    void runBacktest_withValidData() {
        // Create 150 days of train, 30 days of test
        List<Integer> demand = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            demand.add(10);
        }
        for (int i = 0; i < 30; i++) {
            demand.add(10);
        }

        ForecastBacktestService.BacktestResult result = service.runBacktest(demand, 30);
        
        assertThat(result.allMetrics()).hasSize(3);
        // MA should perfectly predict 10 every time. Error = 0.
        assertThat(result.bestMetric().mae()).isEqualTo(0.0);
        assertThat(result.bestMetric().wape()).isEqualTo(0.0);
        
        // Since data is perfectly 10, confidence could be HIGH because wape is 0 <= 0.25, > 90 days, > 20 days demand
        assertThat(result.confidence()).isEqualTo(ForecastConfidence.HIGH);
    }
}
