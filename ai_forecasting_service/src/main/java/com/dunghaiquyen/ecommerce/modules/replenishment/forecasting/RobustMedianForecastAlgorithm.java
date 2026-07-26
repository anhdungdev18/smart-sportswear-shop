package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RobustMedianForecastAlgorithm implements ForecastAlgorithm {

    private static final int WINDOW_SIZE = 30;

    @Override
    public ForecastAlgorithmType type() {
        return ForecastAlgorithmType.ROBUST_MEDIAN;
    }

    @Override
    public ForecastResult forecast(List<Integer> dailyDemand, int horizonDays) {
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("Horizon days must be positive");
        }

        double median = 0.0;
        if (dailyDemand != null && !dailyDemand.isEmpty()) {
            int start = Math.max(0, dailyDemand.size() - WINDOW_SIZE);
            List<Integer> window = dailyDemand.subList(start, dailyDemand.size()).stream()
                    .map(value -> value == null ? 0 : Math.max(0, value))
                    .sorted(Comparator.naturalOrder())
                    .toList();
            int size = window.size();
            if (size % 2 == 0) {
                median = (window.get(size / 2 - 1) + window.get(size / 2)) / 2.0;
            } else {
                median = window.get(size / 2);
            }
        }

        List<Double> dailyForecast = new ArrayList<>(horizonDays);
        for (int i = 0; i < horizonDays; i++) {
            dailyForecast.add(median);
        }
        return new ForecastResult(type(), median, median * horizonDays, dailyForecast);
    }
}
