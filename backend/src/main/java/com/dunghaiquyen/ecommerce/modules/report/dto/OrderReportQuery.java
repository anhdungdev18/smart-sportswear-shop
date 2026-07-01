package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;

public record OrderReportQuery(LocalDate dateFrom, LocalDate dateTo) {
}
