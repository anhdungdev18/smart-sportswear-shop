package com.dunghaiquyen.ecommerce.modules.report.dto;

import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import java.time.LocalDate;
import java.util.List;

public record ProductReportResponse(
        LocalDate dateFrom,
        LocalDate dateTo,
        List<BestSellingProductResponse> bestSelling,
        PageMeta meta) {
}
