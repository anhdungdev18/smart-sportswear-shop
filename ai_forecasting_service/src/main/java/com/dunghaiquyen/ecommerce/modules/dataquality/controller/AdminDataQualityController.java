package com.dunghaiquyen.ecommerce.modules.dataquality.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.DataQualitySummaryResponse;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.SkuDataQualityResponse;
import com.dunghaiquyen.ecommerce.modules.dataquality.service.SkuDataQualityService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai/data-quality")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDataQualityController {

    private final SkuDataQualityService service;

    public AdminDataQualityController(SkuDataQualityService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ApiResponse<DataQualitySummaryResponse> summary(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        DateRange range = range(from, to);
        return ApiResponse.ok(service.summarize(range.from(), range.to()));
    }

    @GetMapping("/variants")
    public ApiResponse<List<SkuDataQualityResponse>> variants(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        DateRange range = range(from, to);
        return ApiResponse.ok(service.listVariants(range.from(), range.to()));
    }

    @GetMapping("/variants/{variantId}")
    public ApiResponse<SkuDataQualityResponse> variant(
            @PathVariable UUID variantId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        DateRange range = range(from, to);
        return ApiResponse.ok(service.getVariant(variantId, range.from(), range.to()));
    }

    private DateRange range(LocalDate from, LocalDate to) {
        LocalDate resolvedTo = to == null ? LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")) : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(179) : from;
        return new DateRange(resolvedFrom, resolvedTo);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
