package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;
import java.util.List;

public record OrderStatusTrendResponse(
        LocalDate fromDate,
        LocalDate toDate,
        List<OrderStatusTrendPoint> points,
        boolean trendAvailable) {
}
