package com.dunghaiquyen.ecommerce.modules.ageing.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.ageing.dto.InventoryAgeingSummaryResponse;
import com.dunghaiquyen.ecommerce.modules.ageing.service.InventoryAgeingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai/inventory-ageing")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryAgeingController {
    private final InventoryAgeingService service;

    public AdminInventoryAgeingController(InventoryAgeingService service) { this.service = service; }

    @GetMapping("/summary")
    public ApiResponse<InventoryAgeingSummaryResponse> summary(@RequestParam(required = false) String dataSource) {
        return ApiResponse.ok(service.summarize(dataSource));
    }
}
