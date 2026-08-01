package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.OffsetDateTime;
import java.util.List;

public record VisualSearchJobsResponse(List<Job> jobs) {
    public record Job(
            String id,
            @JsonAlias("job_type") String jobType,
            String status,
            @JsonAlias("total_count") int totalCount,
            @JsonAlias("completed_count") int completedCount,
            @JsonAlias("failed_count") int failedCount,
            @JsonAlias("pending_count") int pendingCount,
            @JsonAlias("created_at") OffsetDateTime createdAt,
            @JsonAlias("completed_at") OffsetDateTime completedAt) {
    }
}
