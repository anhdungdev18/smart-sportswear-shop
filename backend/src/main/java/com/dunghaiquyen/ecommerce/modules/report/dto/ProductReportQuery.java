package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.time.LocalDate;

public record ProductReportQuery(Integer page, Integer limit, LocalDate dateFrom, LocalDate dateTo) {
}
