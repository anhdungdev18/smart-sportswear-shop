package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;

public record RevenueStatusBreakdown(String status, long orders, BigDecimal amount) {
}
