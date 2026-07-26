package com.dunghaiquyen.ecommerce.modules.demand.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.config.ForecastDataSourceProperties;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandClassificationBatchResponse;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandClassificationResponse;
import com.dunghaiquyen.ecommerce.modules.demand.service.DemandClassificationService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai/demand-classifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDemandClassificationController {

    private final DemandClassificationService service;
    private final ForecastDataSourceProperties dataSourceProperties;

    public AdminDemandClassificationController(DemandClassificationService service,
                                               ForecastDataSourceProperties dataSourceProperties) {
        this.service = service;
        this.dataSourceProperties = dataSourceProperties;
    }

    @PostMapping("/run")
    public ApiResponse<DemandClassificationBatchResponse> run(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String dataSource) {
        DateRange range = range(from, to);
        return ApiResponse.ok(service.classifyBatch(range.from(), range.to(), resolveDataSource(dataSource)));
    }

    @GetMapping("/variants")
    public ApiResponse<List<DemandClassificationResponse>> variants(@RequestParam(required = false) String dataSource) {
        return ApiResponse.ok(service.listSaved(resolveDataSource(dataSource)));
    }

    @GetMapping("/variants/{variantId}")
    public ApiResponse<DemandClassificationResponse> variant(
            @PathVariable UUID variantId,
            @RequestParam(required = false) String dataSource) {
        return ApiResponse.ok(service.getSaved(variantId, resolveDataSource(dataSource)));
    }

    private String resolveDataSource(String dataSource) {
        return dataSource == null || dataSource.isBlank() ? dataSourceProperties.dataSource() : dataSource;
    }

    private DateRange range(LocalDate from, LocalDate to) {
        LocalDate resolvedTo = to == null ? LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")) : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(179) : from;
        return new DateRange(resolvedFrom, resolvedTo);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
