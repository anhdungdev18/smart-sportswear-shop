package com.dunghaiquyen.ecommerce.config;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.forecast-demo")
public record AppForecastDemoProperties(
        boolean enabled,
        long randomSeed,
        LocalDate anchorDate,
        @Min(1) int historyDays,
        @Min(1) int orderCount,
        @Min(1) int variantCount,
        String marker,
        boolean cleanupBeforeSeed
) {}
