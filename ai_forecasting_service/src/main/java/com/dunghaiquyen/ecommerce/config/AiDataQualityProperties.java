package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "ai.data-quality")
public record AiDataQualityProperties(
        @Min(1) int minHistoryDays,
        @Min(1) int highHistoryDays,
        @Min(1) int minNonZeroDays,
        @Min(1) int highNonZeroDays) {

    public AiDataQualityProperties {
        if (minHistoryDays == 0) {
            minHistoryDays = 60;
        }
        if (highHistoryDays == 0) {
            highHistoryDays = 120;
        }
        if (minNonZeroDays == 0) {
            minNonZeroDays = 12;
        }
        if (highNonZeroDays == 0) {
            highNonZeroDays = 30;
        }
        if (minHistoryDays > highHistoryDays) {
            throw new IllegalArgumentException("minHistoryDays must not exceed highHistoryDays");
        }
        if (minNonZeroDays > highNonZeroDays) {
            throw new IllegalArgumentException("minNonZeroDays must not exceed highNonZeroDays");
        }
    }
}
