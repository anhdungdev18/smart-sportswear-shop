package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ForecastBacktestService {

    private final List<ForecastAlgorithm> algorithms;

    public ForecastBacktestService(List<ForecastAlgorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public record BacktestMetric(
            ForecastAlgorithmType algorithm,
            double mae,
            Double wape,
            double residualStdDev,
            double sumActual) {}

    public record BacktestResult(
            ForecastAlgorithmType bestAlgorithm,
            BacktestMetric bestMetric,
            ForecastConfidence confidence,
            List<BacktestMetric> allMetrics) {}

    public BacktestResult runBacktest(List<Integer> dailyDemand, int testWindowDays) {
        if (dailyDemand == null || dailyDemand.size() <= testWindowDays) {
            // Not enough data to backtest
            return new BacktestResult(
                    ForecastAlgorithmType.MOVING_AVERAGE, null, ForecastConfidence.LOW, List.of());
        }

        int nTest = testWindowDays;
        int nTrain = dailyDemand.size() - nTest;

        List<BacktestMetric> metrics = new ArrayList<>();

        for (ForecastAlgorithm algo : algorithms) {
            double sumAbsError = 0;
            double sumActual = 0;
            double sumSqError = 0;

            for (int i = 0; i < nTest; i++) {
                List<Integer> history = dailyDemand.subList(0, nTrain + i);
                int actual = dailyDemand.get(nTrain + i);

                ForecastResult result = algo.forecast(history, 1);
                double predicted = result.dailyForecast().isEmpty() ? 0.0 : result.dailyForecast().get(0);

                double error = actual - predicted;
                sumAbsError += Math.abs(error);
                sumActual += actual;
                sumSqError += error * error;
            }

            double mae = sumAbsError / nTest;
            Double wape = sumActual > 0 ? (sumAbsError / sumActual) : null;
            double residualStdDev = Math.sqrt(sumSqError / nTest);

            metrics.add(new BacktestMetric(algo.type(), mae, wape, residualStdDev, sumActual));
        }

        BacktestMetric bestMetric = selectBestMetric(metrics);
        ForecastConfidence confidence = calculateConfidence(dailyDemand, bestMetric);

        return new BacktestResult(
                bestMetric != null ? bestMetric.algorithm() : ForecastAlgorithmType.MOVING_AVERAGE,
                bestMetric,
                confidence,
                metrics);
    }

    public record BacktestPrediction(int index, double predicted) {}

    public List<BacktestPrediction> buildPredictions(
            List<Integer> dailyDemand, int testWindowDays, ForecastAlgorithmType algorithmType) {
        if (dailyDemand == null || dailyDemand.size() <= testWindowDays) return List.of();
        ForecastAlgorithm algorithm = algorithms.stream()
                .filter(candidate -> candidate.type() == algorithmType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Forecast algorithm is unavailable: " + algorithmType));
        int firstTestIndex = dailyDemand.size() - testWindowDays;
        List<BacktestPrediction> predictions = new ArrayList<>(testWindowDays);
        for (int index = firstTestIndex; index < dailyDemand.size(); index++) {
            ForecastResult result = algorithm.forecast(dailyDemand.subList(0, index), 1);
            double predicted = result.dailyForecast().isEmpty() ? 0.0 : result.dailyForecast().get(0);
            predictions.add(new BacktestPrediction(index, Math.max(0.0, predicted)));
        }
        return List.copyOf(predictions);
    }
    private BacktestMetric selectBestMetric(List<BacktestMetric> metrics) {
        if (metrics.isEmpty()) return null;

        return metrics.stream().min((m1, m2) -> {
            if (m1.sumActual() > 0 && m2.sumActual() > 0) {
                int cmp = Double.compare(m1.wape(), m2.wape());
                if (cmp != 0) return cmp;
            } else if (m1.sumActual() == 0 && m2.sumActual() == 0) {
                int cmp = Double.compare(m1.mae(), m2.mae());
                if (cmp != 0) return cmp;
            } else {
                // One has actual > 0 and the other doesn't, this shouldn't happen for same dataset
                return 0;
            }
            // Tie breaker: MOVING_AVERAGE > EWMA > CROSTON
            return Integer.compare(getAlgorithmPriority(m1.algorithm()), getAlgorithmPriority(m2.algorithm()));
        }).orElse(null);
    }

    private int getAlgorithmPriority(ForecastAlgorithmType type) {
        return switch (type) {
            case MOVING_AVERAGE -> 1;
            case EWMA -> 2;
            case CROSTON -> 3;
            default -> 99;
        };
    }

    private ForecastConfidence calculateConfidence(List<Integer> dailyDemand, BacktestMetric metric) {
        if (metric == null || metric.wape() == null) {
            return ForecastConfidence.LOW;
        }

        int totalDays = dailyDemand.size();
        long daysWithDemand = dailyDemand.stream().filter(d -> d != null && d > 0).count();
        double wape = metric.wape();

        if (totalDays >= 90 && daysWithDemand >= 20 && wape <= 0.25) {
            return ForecastConfidence.HIGH;
        }

        if (totalDays >= 60 && daysWithDemand >= 10 && wape <= 0.50) {
            return ForecastConfidence.MEDIUM;
        }

        return ForecastConfidence.LOW;
    }
}
