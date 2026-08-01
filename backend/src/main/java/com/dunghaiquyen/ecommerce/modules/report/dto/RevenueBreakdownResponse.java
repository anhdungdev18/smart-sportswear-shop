package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record RevenueBreakdownResponse(
        BigDecimal grossRevenue,
        BigDecimal realizedRevenue,
        BigDecimal difference,
        List<RevenueStatusBreakdown> byPaymentStatus,
        List<RevenueStatusBreakdown> byOrderStatus,
        RevenueExceptionSlice codDeliveredUnpaid,
        RevenueExceptionSlice paidNotDelivered,
        boolean breakdownAvailable) {
}
