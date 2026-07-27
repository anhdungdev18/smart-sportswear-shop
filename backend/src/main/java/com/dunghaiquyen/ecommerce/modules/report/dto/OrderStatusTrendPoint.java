package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;
import java.util.List;

public record OrderStatusTrendPoint(LocalDate date, long totalOrders, List<OrderStatusCount> byStatus) {
}
