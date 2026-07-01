package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * API_SPEC_PHASE1.md 12.2 has no documented response shape - this is this
 * phase's own design. "Orders by time range" is implemented as dateFrom/dateTo
 * acting as a filter over the same by-status breakdown (echoed back so the
 * caller knows what range was actually applied), not a day-by-day histogram -
 * no granularity (daily/weekly) is specified anywhere in the spec docs, and a
 * single filtered breakdown is the simplest reading that still satisfies
 * "orders by status" + "orders by time range" together.
 */
public record OrderReportResponse(
        LocalDate dateFrom, LocalDate dateTo, long totalOrders, List<OrderStatusCount> byStatus) {
}
