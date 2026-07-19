package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class ReplenishmentSuggestionResponse {
    private UUID id;
    private UUID variantId;
    private UUID productId;
    private String sku;
    private String productName;
    private String size;
    private String color;
    private int availableQuantity;
    private BigDecimal averageDailyDemand;
    private int forecastHorizonDays;
    private BigDecimal forecastQuantity;
    private Integer estimatedStockoutDays;
    private int reorderPoint;
    private int safetyStock;
    private int suggestedQuantity;
    private ReplenishmentPriority priority;
    private String algorithm;
    private String confidence;
    private BigDecimal mae;
    private BigDecimal wape;
    private ReplenishmentStatus status;
    private Instant createdAt;
}
