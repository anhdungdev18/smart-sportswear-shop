package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;

public record VisualSearchRetryResponse(
        @JsonAlias("job_id") String jobId,
        @JsonAlias("enqueued_count") int enqueuedCount) {
}
