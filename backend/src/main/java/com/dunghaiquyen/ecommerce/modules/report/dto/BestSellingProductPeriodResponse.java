package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BestSellingProductPeriodResponse(
        LocalDate fromDate,
        LocalDate toDate,
        int limit,
        String source,
        List<BestSellingProductResponse> items
) {
}
