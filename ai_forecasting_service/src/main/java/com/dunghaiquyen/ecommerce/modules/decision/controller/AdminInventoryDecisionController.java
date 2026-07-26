package com.dunghaiquyen.ecommerce.modules.decision.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskResponse;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskType;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventorySimulationRequest;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventorySimulationResponse;
import com.dunghaiquyen.ecommerce.modules.decision.dto.ReplenishmentExplanationResponse;
import com.dunghaiquyen.ecommerce.modules.decision.service.InventoryDecisionEngineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryDecisionController {

    private final InventoryDecisionEngineService decisionEngineService;

    public AdminInventoryDecisionController(InventoryDecisionEngineService decisionEngineService) {
        this.decisionEngineService = decisionEngineService;
    }

    @GetMapping("/inventory-risks")
    @Transactional(readOnly = true)
    public ApiResponse<List<InventoryRiskResponse>> listRisks(
            @RequestParam(required = false) InventoryRiskType risk) {
        return ApiResponse.ok(decisionEngineService.listRisks(risk));
    }

    @GetMapping("/inventory-risks/{variantId}")
    @Transactional(readOnly = true)
    public ApiResponse<InventoryRiskResponse> getRisk(@PathVariable UUID variantId) {
        return ApiResponse.ok(decisionEngineService.getRisk(variantId));
    }

    @GetMapping("/replenishment/explanations/{id}")
    @Transactional(readOnly = true)
    public ApiResponse<ReplenishmentExplanationResponse> explain(@PathVariable UUID id) {
        return ApiResponse.ok(decisionEngineService.explainRecommendation(id));
    }

    @PostMapping("/inventory/simulate")
    @Transactional(readOnly = true)
    public ApiResponse<InventorySimulationResponse> simulate(
            @Valid @RequestBody InventorySimulationRequest request) {
        return ApiResponse.ok(decisionEngineService.simulate(request));
    }
}
