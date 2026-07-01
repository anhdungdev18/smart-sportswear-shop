package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;

/** API_SPEC_PHASE1.md 12.1 - exact field set from the documented sample response. */
public record OverviewReportResponse(
        BigDecimal grossRevenue,
        BigDecimal realizedRevenue,
        long totalOrders,
        long pendingOrders,
        long lowStockCount) {
}
