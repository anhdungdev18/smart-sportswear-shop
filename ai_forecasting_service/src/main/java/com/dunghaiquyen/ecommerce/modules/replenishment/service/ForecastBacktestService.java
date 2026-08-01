package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
            double bias,
            int windows,
            double sumActual) {}

    public record BacktestResult(
            ForecastAlgorithmType bestAlgorithm,
            BacktestMetric bestMetric,
            BacktestMetric benchmarkMetric,
            ForecastConfidence confidence,
            String reason,
            List<BacktestMetric> allMetrics) {}

    public BacktestResult runBacktest(List<Integer> dailyDemand, int testWindowDays) {
        return runBacktest(dailyDemand, testWindowDays, algorithms.stream().map(ForecastAlgorithm::type).collect(java.util.stream.Collectors.toSet()));
    }

    public BacktestResult runBacktest(List<Integer> dailyDemand, int testWindowDays, Set<ForecastAlgorithmType> candidateTypes) {
        if (dailyDemand == null || dailyDemand.size() <= testWindowDays) {
            return new BacktestResult(
                    ForecastAlgorithmType.NAIVE, null, null, ForecastConfidence.INSUFFICIENT,
                    "Khong du lich su de backtest rolling-origin.", List.of());
        }

        int windowCount = Math.max(1, Math.min(3, (dailyDemand.size() - testWindowDays) / Math.max(1, testWindowDays)));

        List<BacktestMetric> metrics = new ArrayList<>();

        for (ForecastAlgorithm algo : algorithms) {
            if (candidateTypes != null && !candidateTypes.isEmpty() && !candidateTypes.contains(algo.type())) {
                continue;
            }
            double sumAbsError = 0;
            double sumActual = 0;
            double sumSqError = 0;
            double sumError = 0;
            int observations = 0;

            for (int window = 0; window < windowCount; window++) {
                int firstTestIndex = dailyDemand.size() - ((windowCount - window) * testWindowDays);
                for (int i = 0; i < testWindowDays; i++) {
                    int actualIndex = firstTestIndex + i;
                    List<Integer> history = dailyDemand.subList(0, actualIndex);
                    int actual = dailyDemand.get(actualIndex);

                    ForecastResult result = algo.forecast(history, 1);
                    double predicted = result.dailyForecast().isEmpty() ? 0.0 : Math.max(0.0, result.dailyForecast().get(0));

                    double error = actual - predicted;
                    sumAbsError += Math.abs(error);
                    sumActual += actual;
                    sumSqError += error * error;
                    sumError += error;
                    observations++;
                }
            }

            double mae = sumAbsError / observations;
            Double wape = sumActual > 0 ? (sumAbsError / sumActual) : null;
            double residualStdDev = Math.sqrt(sumSqError / observations);
            double bias = sumActual > 0 ? (sumError / sumActual) : 0.0;

            metrics.add(new BacktestMetric(algo.type(), mae, wape, residualStdDev, bias, windowCount, sumActual));
        }

        BacktestMetric bestMetric = selectBestMetric(metrics);
        BacktestMetric benchmarkMetric = metrics.stream()
                .filter(metric -> metric.algorithm() == ForecastAlgorithmType.NAIVE)
                .findFirst()
                .orElse(null);
        ForecastConfidence confidence = calculateConfidence(dailyDemand, bestMetric);

        return new BacktestResult(
                bestMetric != null ? bestMetric.algorithm() : ForecastAlgorithmType.NAIVE,
                bestMetric,
                benchmarkMetric,
                confidence,
                buildReason(bestMetric, benchmarkMetric, confidence),
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
            case NAIVE -> 1;
            case MOVING_AVERAGE -> 2;
            case EWMA -> 3;
            case CROSTON -> 4;
            case ROBUST_MEDIAN -> 5;
            default -> 99;
        };
    }

    private ForecastConfidence calculateConfidence(List<Integer> dailyDemand, BacktestMetric metric) {
        if (metric == null || metric.wape() == null) {
            return ForecastConfidence.INSUFFICIENT;
        }

        int totalDays = dailyDemand.size();
        long daysWithDemand = dailyDemand.stream().filter(d -> d != null && d > 0).count();
        double wape = metric.wape();
        double absBias = Math.abs(metric.bias());

        if (totalDays >= 120 && daysWithDemand >= 30 && metric.windows() >= 3 && wape <= 0.30 && absBias <= 0.25) {
            return ForecastConfidence.HIGH;
        }

        if (totalDays >= 60 && daysWithDemand >= 12 && metric.windows() >= 1 && wape <= 0.60) {
            return ForecastConfidence.MEDIUM;
        }

        return ForecastConfidence.LOW;
    }

    private String buildReason(BacktestMetric bestMetric, BacktestMetric benchmarkMetric, ForecastConfidence confidence) {
        if (bestMetric == null) {
            return "Khong co candidate model kha dung cho backtest.";
        }
        String benchmark = "";
        if (benchmarkMetric != null && benchmarkMetric.wape() != null) {
            benchmark = String.format("; naive baseline WAPE %.4f", benchmarkMetric.wape());
        }
        return String.format(
                "Chon %s qua rolling-origin backtest %d window: MAE %.4f, WAPE %s, bias %.4f%s, confidence %s.",
                bestMetric.algorithm(),
                bestMetric.windows(),
                bestMetric.mae(),
                bestMetric.wape() == null ? "N/A" : String.format("%.4f", bestMetric.wape()),
                bestMetric.bias(),
                benchmark,
                confidence);
    }
}
