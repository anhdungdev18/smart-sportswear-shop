package com.dunghaiquyen.ecommerce.modules.decision.dto;

public record OverstockMetrics(
        Double daysOfSupply,
        Integer deadStockDays,
        Double inventoryTurnover,
        int excessQuantity,
        Double excessValue) {
}
