package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NaiveForecastAlgorithm implements ForecastAlgorithm {

    @Override
    public ForecastAlgorithmType type() {
        return ForecastAlgorithmType.NAIVE;
    }

    @Override
    public ForecastResult forecast(List<Integer> dailyDemand, int horizonDays) {
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("Horizon days must be positive");
        }

        double forecast = 0.0;
        if (dailyDemand != null && !dailyDemand.isEmpty()) {
            Integer last = dailyDemand.get(dailyDemand.size() - 1);
            forecast = Math.max(0.0, last != null ? last : 0.0);
        }

        List<Double> dailyForecast = new ArrayList<>(horizonDays);
        for (int i = 0; i < horizonDays; i++) {
            dailyForecast.add(forecast);
        }
        return new ForecastResult(type(), forecast, forecast * horizonDays, dailyForecast);
    }
}
