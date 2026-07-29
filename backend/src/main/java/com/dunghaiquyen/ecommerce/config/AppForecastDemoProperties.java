package com.dunghaiquyen.ecommerce.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
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
        @NotBlank String marker,
        boolean cleanupBeforeSeed,
        @Min(0) int smoothVariants,
        @Min(0) int normalVariants,
        @Min(0) int slowVariants,
        @Min(0) int intermittentVariants,
        @Min(0) int erraticVariants,
        @Min(0) int growingVariants,
        @Min(0) int decliningVariants,
        @Min(0) int newItemVariants,
        @Min(0) int noDemandVariants,
        @DecimalMin(value = "0.0001") BigDecimal weekendMultiplier,
        @DecimalMin(value = "0.0001") BigDecimal promotionMultiplier,
        @Min(1) @Max(28) int promotionDayOfMonth,
        @Min(1) int newItemHistoryDays,
        @DecimalMin(value = "0.0") BigDecimal cancelRate,
        boolean inventoryHistoryEnabled,
        @Min(1) int inventoryHistoryDays,
        @Min(1) int supplierCount,
        @Min(0) int minLeadTimeDays,
        @Min(0) int maxLeadTimeDays,
        @DecimalMin(value = "0.0001") BigDecimal defaultServiceLevel,
        boolean groundTruthEnabled,
        @NotBlank String scenarioVersion
) {
    public int profileVariantCount() {
        return smoothVariants + normalVariants + slowVariants + intermittentVariants + erraticVariants
                + growingVariants + decliningVariants + newItemVariants + noDemandVariants;
    }

    public BigDecimal effectiveWeekendMultiplier() {
        return weekendMultiplier != null ? weekendMultiplier : BigDecimal.valueOf(1.30);
    }

    public BigDecimal effectivePromotionMultiplier() {
        return promotionMultiplier != null ? promotionMultiplier : BigDecimal.valueOf(3.00);
    }

    public BigDecimal effectiveCancelRate() {
        return cancelRate != null ? cancelRate : BigDecimal.valueOf(0.05);
    }

    public BigDecimal effectiveDefaultServiceLevel() {
        return defaultServiceLevel != null ? defaultServiceLevel : BigDecimal.valueOf(0.95);
    }
}