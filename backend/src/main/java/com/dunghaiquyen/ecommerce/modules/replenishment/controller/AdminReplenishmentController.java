package com.dunghaiquyen.ecommerce.modules.replenishment.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.replenishment.dto.*;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.InventoryPolicyRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.DemandForecastService;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/replenishment")
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdminReplenishmentController {

    private final DemandForecastService demandForecastService;
    private final ReplenishmentRecommendationRepository recommendationRepository;
    private final InventoryPolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;

    public AdminReplenishmentController(DemandForecastService demandForecastService,
                                        ReplenishmentRecommendationRepository recommendationRepository,
                                        InventoryPolicyRepository policyRepository,
                                        UserRepository userRepository,
                                        ProductVariantRepository variantRepository) {
        this.demandForecastService = demandForecastService;
        this.recommendationRepository = recommendationRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.variantRepository = variantRepository;
    }

    @GetMapping("/suggestions")
    public ApiResponse<Page<ReplenishmentSuggestionResponse>> listSuggestions(
            @RequestParam(required = false) ReplenishmentStatus status,
            @RequestParam(required = false) ReplenishmentPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ReplenishmentRecommendation> result = recommendationRepository.searchRecommendations(status, priority, keyword, pageable);
        
        Page<ReplenishmentSuggestionResponse> responsePage = result.map(this::mapToResponse);
        return ApiResponse.ok(responsePage);
    }

    @GetMapping("/suggestions/{id}")
    public ApiResponse<ReplenishmentSuggestionDetailResponse> getDetail(@PathVariable UUID id) {
        ReplenishmentRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
        
        ReplenishmentSuggestionDetailResponse detail = new ReplenishmentSuggestionDetailResponse();
        mapToResponse(rec, detail);
        
        InventoryPolicy policy = policyRepository.findByVariantId(rec.getVariant().getId())
                .orElse(null);
        if (policy != null) {
            detail.setPolicyLeadTimeDays(policy.getLeadTimeDays());
            detail.setPolicyTargetCoverDays(policy.getTargetCoverDays());
            detail.setPolicyServiceLevel(policy.getServiceLevel().doubleValue());
        }
        
        detail.setExplanationJson(rec.getExplanation());
        // For MVP we just return empty charts, or we could fetch from history if needed.
        detail.setHistoryData(new ArrayList<>());
        detail.setFutureForecastData(new ArrayList<>());
        
        return ApiResponse.ok(detail);
    }

    @PostMapping("/generate")
    public ApiResponse<Void> generate(@RequestBody(required = false) GenerateForecastRequest request) {
        List<UUID> variantIds;
        if (request != null && request.getVariantIds() != null && !request.getVariantIds().isEmpty()) {
            variantIds = request.getVariantIds();
        } else {
            variantIds = variantRepository.findAll().stream().map(ProductVariant::getId).collect(Collectors.toList());
        }
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate from = today.minusDays(180);
        
        for (UUID variantId : variantIds) {
            demandForecastService.generateForecastAndRecommendation(variantId, from, today);
        }
        
        return ApiResponse.ok(null);
    }

    @PutMapping("/policies/{variantId}")
    public ApiResponse<Void> updatePolicy(@PathVariable UUID variantId, @RequestBody InventoryPolicyRequest request) {
        InventoryPolicy policy = policyRepository.findByVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        
        policy.setLeadTimeDays(request.getLeadTimeDays());
        policy.setTargetCoverDays(request.getTargetCoverDays());
        policy.setServiceLevel(request.getServiceLevel());
        policy.setMinimumOrderQuantity(request.getMinimumOrderQuantity());
        policy.setPackSize(request.getPackSize());
        policy.setSupplierName(request.getSupplierName());
        policy.setActive(request.isActive());
        
        policyRepository.save(policy);
        return ApiResponse.ok(null);
    }

    @PostMapping("/suggestions/{id}/accept")
    public ApiResponse<Void> acceptSuggestion(
            @PathVariable UUID id,
            @RequestBody(required = false) ReplenishmentActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        ReplenishmentRecommendation rec = recommendationRepository.findById(id).orElseThrow();
        if (rec.getStatus() != ReplenishmentStatus.PENDING) {
            throw new IllegalArgumentException("Invalid state transition");
        }
        rec.setStatus(ReplenishmentStatus.ACCEPTED);
        rec.setAdminQuantity(rec.getSuggestedQuantity());
        if (request != null && request.getNote() != null) {
            rec.setAdminNote(request.getNote());
        }
        rec.setActedBy(userRepository.findById(principal.getUserId()).orElse(null));
        rec.setActedAt(Instant.now());
        recommendationRepository.save(rec);
        
        return ApiResponse.ok(null);
    }

    @PostMapping("/suggestions/{id}/adjust")
    public ApiResponse<Void> adjustSuggestion(
            @PathVariable UUID id,
            @RequestBody ReplenishmentActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        ReplenishmentRecommendation rec = recommendationRepository.findById(id).orElseThrow();
        if (rec.getStatus() != ReplenishmentStatus.PENDING) {
            throw new IllegalArgumentException("Invalid state transition");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity is invalid");
        }
        
        rec.setStatus(ReplenishmentStatus.ADJUSTED);
        rec.setAdminQuantity(request.getQuantity());
        rec.setAdminNote(request.getNote());
        rec.setActedBy(userRepository.findById(principal.getUserId()).orElse(null));
        rec.setActedAt(Instant.now());
        recommendationRepository.save(rec);
        
        return ApiResponse.ok(null);
    }

    @PostMapping("/suggestions/{id}/dismiss")
    public ApiResponse<Void> dismissSuggestion(
            @PathVariable UUID id,
            @RequestBody ReplenishmentActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        ReplenishmentRecommendation rec = recommendationRepository.findById(id).orElseThrow();
        if (rec.getStatus() != ReplenishmentStatus.PENDING) {
            throw new IllegalArgumentException("Invalid state transition");
        }
        if (request.getNote() == null || request.getNote().isBlank()) {
            throw new IllegalArgumentException("Note is required for dismissal");
        }
        rec.setStatus(ReplenishmentStatus.DISMISSED);
        rec.setAdminNote(request.getNote());
        rec.setActedBy(userRepository.findById(principal.getUserId()).orElse(null));
        rec.setActedAt(Instant.now());
        recommendationRepository.save(rec);
        
        return ApiResponse.ok(null);
    }

    private ReplenishmentSuggestionResponse mapToResponse(ReplenishmentRecommendation rec) {
        ReplenishmentSuggestionResponse res = new ReplenishmentSuggestionResponse();
        mapToResponse(rec, res);
        return res;
    }
    
    private void mapToResponse(ReplenishmentRecommendation rec, ReplenishmentSuggestionResponse res) {
        res.setId(rec.getId());
        res.setVariantId(rec.getVariant().getId());
        res.setProductId(rec.getVariant().getProduct().getId());
        res.setSku(rec.getVariant().getSku());
        res.setProductName(rec.getVariant().getProduct().getName());
        res.setSize(rec.getVariant().getSize());
        res.setColor(rec.getVariant().getColor());
        res.setAvailableQuantity(rec.getAvailableQuantity());
        res.setEstimatedStockoutDays(rec.getEstimatedStockoutDays());
        res.setReorderPoint(rec.getReorderPoint());
        res.setSafetyStock(rec.getSafetyStock());
        res.setSuggestedQuantity(rec.getSuggestedQuantity());
        res.setPriority(rec.getPriority());
        res.setStatus(rec.getStatus());
        res.setCreatedAt(rec.getCreatedAt());

        ForecastRun run = rec.getForecastRun();
        if (run != null) {
            res.setAverageDailyDemand(run.getAverageDailyDemand());
            res.setForecastHorizonDays(run.getForecastHorizonDays());
            res.setForecastQuantity(run.getForecastQuantity());
            res.setAlgorithm(run.getAlgorithm().name());
            res.setConfidence(run.getConfidence().name());
            res.setMae(run.getMae());
            res.setWape(run.getWape());
        }
    }
}
