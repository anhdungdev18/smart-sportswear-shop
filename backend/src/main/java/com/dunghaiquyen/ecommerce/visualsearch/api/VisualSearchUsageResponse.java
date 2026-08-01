package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;
import java.util.List;

public record VisualSearchUsageResponse(int days, List<Row> rows) {
    public record Row(
            LocalDate day,
            String operation,
            int requests,
            @JsonAlias("image_pixels") long imagePixels,
            @JsonAlias("text_tokens") long textTokens,
            @JsonAlias("estimated_cost_usd") double estimatedCostUsd,
            @JsonAlias("success_count") int successCount,
            @JsonAlias("failure_count") int failureCount) {
    }
}
