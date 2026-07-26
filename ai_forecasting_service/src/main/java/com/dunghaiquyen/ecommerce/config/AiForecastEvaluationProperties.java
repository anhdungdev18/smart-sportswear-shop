package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.forecast-evaluation")
public record AiForecastEvaluationProperties(
        int testWindowDays,
        int minBacktestWindows,
        double maxWapeHighConfidence,
        double maxWapeMediumConfidence,
        double maxAbsBiasHighConfidence,
        String algorithmVersion) {

    public AiForecastEvaluationProperties {
        if (testWindowDays <= 0) {
            testWindowDays = 30;
        }
        if (minBacktestWindows <= 0) {
            minBacktestWindows = 3;
        }
        if (maxWapeHighConfidence <= 0) {
            maxWapeHighConfidence = 0.30;
        }
        if (maxWapeMediumConfidence <= 0) {
            maxWapeMediumConfidence = 0.60;
        }
        if (maxAbsBiasHighConfidence <= 0) {
            maxAbsBiasHighConfidence = 0.25;
        }
        if (algorithmVersion == null || algorithmVersion.isBlank()) {
            algorithmVersion = "forecast-evaluation-v2";
        }
    }
}
