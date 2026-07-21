package com.dunghaiquyen.ecommerce.modules.replenishment.forecasting;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import java.util.List;

public interface ForecastAlgorithm {
    ForecastAlgorithmType type();

    ForecastResult forecast(
            List<Integer> dailyDemand,
            int horizonDays);
}
