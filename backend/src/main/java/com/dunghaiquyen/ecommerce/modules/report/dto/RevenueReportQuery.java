package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;

/**
 * Query params for {@code GET /api/v1/admin/reports/revenue}. All optional:
 * {@code granularity} defaults to MONTH, and when {@code dateFrom}/{@code dateTo}
 * are omitted the service derives a sensible window from the granularity.
 */
public record RevenueReportQuery(String granularity, LocalDate dateFrom, LocalDate dateTo) {
}
