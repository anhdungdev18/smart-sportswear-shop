package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerSalesResponse(
        UUID customerId,
        String customerName,
        String email,
        Long totalOrders,
        BigDecimal totalRevenue) {
}
