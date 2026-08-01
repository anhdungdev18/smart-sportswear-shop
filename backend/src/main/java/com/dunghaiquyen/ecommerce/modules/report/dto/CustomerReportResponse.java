package com.dunghaiquyen.ecommerce.modules.report.dto;

import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import java.time.LocalDate;
import java.util.List;

public record CustomerReportResponse(
        LocalDate dateFrom,
        LocalDate dateTo,
        List<CustomerSalesResponse> customers,
        PageMeta meta) {
}
