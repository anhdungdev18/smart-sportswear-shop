package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MovingAverageForecastAlgorithm implements ForecastAlgorithm {

    private static final int WINDOW_SIZE = 30;

    @Override
    public ForecastAlgorithmType type() {
        return ForecastAlgorithmType.MOVING_AVERAGE;
    }

    @Override
    public ForecastResult forecast(List<Integer> dailyDemand, int horizonDays) {
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("Horizon days must be positive");
        }

        double averageDailyDemand = 0.0;
        if (dailyDemand != null && !dailyDemand.isEmpty()) {
            int daysToUse = Math.min(WINDOW_SIZE, dailyDemand.size());
            long sum = 0;
            // Get the last 'daysToUse' days
            int startIndex = dailyDemand.size() - daysToUse;
            for (int i = startIndex; i < dailyDemand.size(); i++) {
                Integer demand = dailyDemand.get(i);
                sum += (demand != null ? demand : 0);
            }
            averageDailyDemand = (double) sum / daysToUse;
        }

        double forecastQuantity = averageDailyDemand * horizonDays;
        List<Double> dailyForecast = new ArrayList<>(horizonDays);
        for (int i = 0; i < horizonDays; i++) {
            dailyForecast.add(averageDailyDemand);
        }

        return new ForecastResult(type(), averageDailyDemand, forecastQuantity, dailyForecast);
    }
}
