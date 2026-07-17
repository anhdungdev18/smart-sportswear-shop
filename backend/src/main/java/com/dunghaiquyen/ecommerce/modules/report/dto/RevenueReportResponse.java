package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Revenue time-series report: the resolved granularity + window plus one
 * continuous list of points (empty buckets are filled with zero so the chart
 * line never has gaps).
 */
public record RevenueReportResponse(
        String granularity,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<RevenuePointResponse> points) {
}
