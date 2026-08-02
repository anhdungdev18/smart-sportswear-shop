package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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
            @JsonAlias("completed_at") OffsetDateTime completedAt,
            @JsonAlias("source_counts") Map<String, Integer> sourceCounts,
            @JsonAlias("error_summary") Map<String, Integer> errorSummary) {
    }
}
