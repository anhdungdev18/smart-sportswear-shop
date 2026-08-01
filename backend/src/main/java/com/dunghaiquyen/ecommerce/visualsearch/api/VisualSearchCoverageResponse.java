package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;

public record VisualSearchCoverageResponse(
        @JsonAlias("total_active_images") int totalActiveImages,
        int ready,
        int pending,
        int processing,
        int failed,
        int missing,
        @JsonAlias("coverage_pct") double coveragePct) {
}
