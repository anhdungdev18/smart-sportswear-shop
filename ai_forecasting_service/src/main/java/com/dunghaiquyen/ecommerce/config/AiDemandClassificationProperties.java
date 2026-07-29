package com.dunghaiquyen.ecommerce.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ai.demand-classification")
public record AiDemandClassificationProperties(
        @Min(1) int minHistoryDays,
        @Min(1) int minNonZeroDays,
        @Min(1) int newItemMaxHistoryDays,
        @DecimalMin("0.0") double intermittentAdiThreshold,
        @DecimalMin("0.0") double erraticCvSquaredThreshold,
        @DecimalMin("0.0") double trendSlopeThreshold,
        String algorithmVersion) {

    public AiDemandClassificationProperties {
        if (minHistoryDays == 0) {
            minHistoryDays = 60;
        }
        if (minNonZeroDays == 0) {
            minNonZeroDays = 3;
        }
        if (newItemMaxHistoryDays == 0) {
            newItemMaxHistoryDays = 30;
        }
        if (intermittentAdiThreshold == 0.0d) {
            intermittentAdiThreshold = 1.32d;
        }
        if (erraticCvSquaredThreshold == 0.0d) {
            erraticCvSquaredThreshold = 0.49d;
        }
        if (trendSlopeThreshold == 0.0d) {
            trendSlopeThreshold = 0.03d;
        }
        if (algorithmVersion == null || algorithmVersion.isBlank()) {
            algorithmVersion = "demand-classification-v1";
        }
    }
}
