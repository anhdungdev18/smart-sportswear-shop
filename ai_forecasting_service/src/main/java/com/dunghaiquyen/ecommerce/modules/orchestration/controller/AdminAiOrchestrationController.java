package com.dunghaiquyen.ecommerce.modules.orchestration.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.config.ForecastDataSourceProperties;
import com.dunghaiquyen.ecommerce.modules.orchestration.dto.AiDataFreshnessResponse;
import com.dunghaiquyen.ecommerce.modules.orchestration.dto.AiJobStatusResponse;
import com.dunghaiquyen.ecommerce.modules.orchestration.service.AiFreshnessService;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.ForecastGenerationService;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiOrchestrationController {

    private final AiFreshnessService freshnessService;
    private final ForecastGenerationService forecastGenerationService;
    private final ForecastDataSourceProperties dataSourceProperties;

    public AdminAiOrchestrationController(
            AiFreshnessService freshnessService,
            ForecastGenerationService forecastGenerationService,
            ForecastDataSourceProperties dataSourceProperties) {
        this.freshnessService = freshnessService;
        this.forecastGenerationService = forecastGenerationService;
        this.dataSourceProperties = dataSourceProperties;
    }

    @GetMapping("/freshness")
    public ApiResponse<AiDataFreshnessResponse> freshness(@RequestParam(required = false) String dataSource) {
        return ApiResponse.ok(freshnessService.getFreshness(dataSource));
    }

    @GetMapping("/jobs/{jobId}")
    public ApiResponse<AiJobStatusResponse> jobStatus(@PathVariable String jobId) {
        var status = forecastGenerationService.getStatus();
        return ApiResponse.ok(new AiJobStatusResponse(
                jobId,
                status.status().name(),
                dataSourceProperties.dataSource(),
                Instant.now(),
                status.requested(),
                status.processed(),
                status.succeeded(),
                status.failed(),
                status.durationMillis(),
                status.failedVariantIds(),
                List.of("In-memory job status maps to the current AI batch status for this service instance.")));
    }
}
