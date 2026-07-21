package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class InventoryPolicyRequest {
    @Min(value = 0, message = "Lead time must not be negative")
    private int leadTimeDays;

    @Min(value = 1, message = "Target cover must be positive")
    private int targetCoverDays;

    @DecimalMin(value = "0", inclusive = false, message = "Service level must be greater than 0")
    @DecimalMax(value = "1", inclusive = false, message = "Service level must be less than 1")
    private BigDecimal serviceLevel;

    @Min(value = 1, message = "Minimum order quantity must be positive")
    private int minimumOrderQuantity;

    @Min(value = 1, message = "Pack size must be positive")
    private int packSize;

    private String supplierName;
    private boolean active;
}