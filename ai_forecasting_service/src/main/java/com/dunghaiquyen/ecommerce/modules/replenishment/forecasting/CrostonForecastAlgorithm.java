package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CrostonForecastAlgorithm implements ForecastAlgorithm {

    private static final double ALPHA = 0.10;

    @Override
    public ForecastAlgorithmType type() {
        return ForecastAlgorithmType.CROSTON;
    }

    @Override
    public ForecastResult forecast(List<Integer> dailyDemand, int horizonDays) {
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("Horizon days must be positive");
        }

        double averageDailyDemand = 0.0;
        if (dailyDemand != null && !dailyDemand.isEmpty()) {
            double z = 0.0; // Demand size
            double p = 1.0; // Interval
            int q = 1;      // Periods since last demand
            boolean initialized = false;

            for (Integer demand : dailyDemand) {
                double x = (demand != null) ? demand : 0.0;
                if (x > 0) {
                    if (!initialized) {
                        z = x;
                        p = q;
                        initialized = true;
                    } else {
                        z = ALPHA * x + (1 - ALPHA) * z;
                        p = ALPHA * q + (1 - ALPHA) * p;
                    }
                    q = 1;
                } else {
                    q++;
                }
            }

            if (initialized && p > 0) {
                averageDailyDemand = z / p;
            }
        }

        double forecastQuantity = averageDailyDemand * horizonDays;
        List<Double> dailyForecast = new ArrayList<>(horizonDays);
        for (int i = 0; i < horizonDays; i++) {
            dailyForecast.add(averageDailyDemand);
        }

        return new ForecastResult(type(), averageDailyDemand, forecastQuantity, dailyForecast);
    }
}
