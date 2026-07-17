package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class InventoryPolicyRequest {
    private int leadTimeDays;
    private int targetCoverDays;
    private BigDecimal serviceLevel;
    private int minimumOrderQuantity;
    private int packSize;
    private String supplierName;
    private boolean active;
}
