package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/visual-search")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVisualSearchController {

    private final VisualSearchClient client;

    public AdminVisualSearchController(VisualSearchClient client) {
        this.client = client;
    }

    @GetMapping("/coverage")
    public ApiResponse<VisualSearchCoverageResponse> coverage() {
        return ApiResponse.ok(client.getAdmin("/internal/v1/admin/coverage", VisualSearchCoverageResponse.class));
    }

    @GetMapping("/operations")
    public ApiResponse<VisualSearchOperationsResponse> operations() {
        return ApiResponse.ok(client.getAdmin("/internal/v1/admin/operations", VisualSearchOperationsResponse.class));
    }

    @GetMapping("/usage")
    public ApiResponse<VisualSearchUsageResponse> usage(@RequestParam(defaultValue = "30") int days) {
        int boundedDays = Math.max(1, Math.min(days, 365));
        return ApiResponse.ok(client.getAdmin(
                "/internal/v1/admin/usage?days=" + boundedDays, VisualSearchUsageResponse.class));
    }

    @GetMapping("/jobs")
    public ApiResponse<VisualSearchJobsResponse> jobs(@RequestParam(defaultValue = "10") int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 50));
        return ApiResponse.ok(client.getAdmin(
                "/internal/v1/admin/jobs?limit=" + boundedLimit, VisualSearchJobsResponse.class));
    }

    @PostMapping("/retry-failed")
    public ApiResponse<VisualSearchRetryResponse> retryFailed() {
        return ApiResponse.ok("Retry job queued", client.postAdmin(
                "/internal/v1/admin/retry-failed", VisualSearchRetryResponse.class));
    }

    @PostMapping("/backfill-missing")
    public ApiResponse<VisualSearchRetryResponse> backfillMissing() {
        return ApiResponse.ok("Backfill job queued", client.postAdmin(
                "/internal/v1/admin/backfill-missing", VisualSearchRetryResponse.class));
    }

    @PostMapping("/reindex")
    public ApiResponse<VisualSearchRetryResponse> reindex(
            @Valid @RequestBody VisualSearchReindexRequest request) {
        return ApiResponse.ok("Reindex job queued", client.postAdmin(
                "/internal/v1/admin/reindex", request, VisualSearchRetryResponse.class));
    }
}
