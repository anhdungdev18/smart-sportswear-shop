package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.List;

public record ForecastResult(
        ForecastAlgorithmType algorithm,
        double averageDailyDemand,
        double forecastQuantity,
        List<Double> dailyForecast) {
}
