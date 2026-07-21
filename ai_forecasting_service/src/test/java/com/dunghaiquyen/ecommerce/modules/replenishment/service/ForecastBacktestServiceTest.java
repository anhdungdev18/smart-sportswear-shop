package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.CrostonForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.EwmaForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastResult;
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

    @Test
    void runBacktest_calculatesHandCheckedMaeAndWape() {
        service = new ForecastBacktestService(List.of(fixedAlgorithm(ForecastAlgorithmType.MOVING_AVERAGE, 3.0)));
        var result = service.runBacktest(List.of(10, 10, 10, 2, 4), 2);
        assertThat(result.bestMetric().mae()).isEqualTo(1.0);
        assertThat(result.bestMetric().wape()).isCloseTo(
                1.0 / 3.0, org.assertj.core.data.Offset.offset(1.0e-12));
    }

    @Test
    void runBacktest_handlesZeroActualWithoutDividingByZero() {
        service = new ForecastBacktestService(List.of(fixedAlgorithm(ForecastAlgorithmType.MOVING_AVERAGE, 2.0)));
        var result = service.runBacktest(List.of(5, 5, 5, 0, 0), 2);
        assertThat(result.bestMetric().mae()).isEqualTo(2.0);
        assertThat(result.bestMetric().wape()).isNull();
        assertThat(result.confidence()).isEqualTo(ForecastConfidence.LOW);
    }

    @Test
    void runBacktest_selectsBestModelAndUsesStableTieBreak() {
        service = new ForecastBacktestService(List.of(
                fixedAlgorithm(ForecastAlgorithmType.CROSTON, 5.0),
                fixedAlgorithm(ForecastAlgorithmType.EWMA, 5.0),
                fixedAlgorithm(ForecastAlgorithmType.MOVING_AVERAGE, 1.0)));
        assertThat(service.runBacktest(List.of(4, 4, 4, 5, 5), 2).bestAlgorithm())
                .isEqualTo(ForecastAlgorithmType.EWMA);

        service = new ForecastBacktestService(List.of(
                fixedAlgorithm(ForecastAlgorithmType.CROSTON, 5.0),
                fixedAlgorithm(ForecastAlgorithmType.EWMA, 5.0),
                fixedAlgorithm(ForecastAlgorithmType.MOVING_AVERAGE, 5.0)));
        assertThat(service.runBacktest(List.of(5, 5, 5, 5, 5), 2).bestAlgorithm())
                .isEqualTo(ForecastAlgorithmType.MOVING_AVERAGE);
    }

    @Test
    void buildPredictions_neverIncludesCurrentOrFutureActualsInHistory() {
        RecordingAlgorithm algorithm = new RecordingAlgorithm();
        service = new ForecastBacktestService(List.of(algorithm));
        service.buildPredictions(List.of(1, 2, 3, 100, 200), 2, ForecastAlgorithmType.MOVING_AVERAGE);
        assertThat(algorithm.histories).containsExactly(List.of(1, 2, 3), List.of(1, 2, 3, 100));
    }

    private ForecastAlgorithm fixedAlgorithm(ForecastAlgorithmType type, double prediction) {
        return new ForecastAlgorithm() {
            public ForecastResult forecast(List<Integer> demand, int horizon) {
                return new ForecastResult(type, prediction, prediction, List.of(prediction));
            }
            public ForecastAlgorithmType type() { return type; }
        };
    }

    private static final class RecordingAlgorithm implements ForecastAlgorithm {
        private final List<List<Integer>> histories = new ArrayList<>();
        public ForecastResult forecast(List<Integer> demand, int horizon) {
            histories.add(List.copyOf(demand));
            return new ForecastResult(type(), 0.0, 0.0, List.of(0.0));
        }
        public ForecastAlgorithmType type() { return ForecastAlgorithmType.MOVING_AVERAGE; }
    }
}
