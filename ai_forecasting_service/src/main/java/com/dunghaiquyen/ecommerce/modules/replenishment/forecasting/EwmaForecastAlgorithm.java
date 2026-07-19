package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EwmaForecastAlgorithm implements ForecastAlgorithm {

    private static final double DEFAULT_ALPHA = 0.30;

    @Override
    public ForecastAlgorithmType type() {
        return ForecastAlgorithmType.EWMA;
    }

    @Override
    public ForecastResult forecast(List<Integer> dailyDemand, int horizonDays) {
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("Horizon days must be positive");
        }

        double averageDailyDemand = 0.0;
        if (dailyDemand != null && !dailyDemand.isEmpty()) {
            double s = dailyDemand.get(0) != null ? dailyDemand.get(0) : 0.0; // S(0) = X(0)
            for (int i = 1; i < dailyDemand.size(); i++) {
                double x = dailyDemand.get(i) != null ? dailyDemand.get(i) : 0.0;
                s = DEFAULT_ALPHA * x + (1 - DEFAULT_ALPHA) * s;
            }
            averageDailyDemand = s;
        }

        double forecastQuantity = averageDailyDemand * horizonDays;
        List<Double> dailyForecast = new ArrayList<>(horizonDays);
        for (int i = 0; i < horizonDays; i++) {
            dailyForecast.add(averageDailyDemand);
        }

        return new ForecastResult(type(), averageDailyDemand, forecastQuantity, dailyForecast);
    }
}
