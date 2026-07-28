package com.dunghaiquyen.ecommerce.modules.replenishment.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.replenishment.dto.*;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastModelEvaluation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ForecastModelEvaluationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.InventoryPolicyRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.DailyDemandService;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.ForecastBacktestService;
import com.dunghaiquyen.ecommerce.modules.demand.service.DemandClassificationService;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandPattern;
import com.dunghaiquyen.ecommerce.config.ForecastDataSourceProperties;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.ForecastGenerationService;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.DemandForecastService;
import jakarta.validation.Valid;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.CoreSnapshotSyncService;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantReadRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/admin/replenishment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReplenishmentController {

    private static final Logger log = LoggerFactory.getLogger(AdminReplenishmentController.class);

    private final CoreSnapshotSyncService snapshotSyncService;
    private final ReplenishmentRecommendationRepository recommendationRepository;
    private final InventoryPolicyRepository policyRepository;
    private final VariantReadRepository variantRepository;
    private final DailyDemandService dailyDemandService;
    private final ForecastBacktestService forecastBacktestService;
    private final DemandClassificationService demandClassificationService;
    private final DemandForecastService demandForecastService;
    private final ForecastDataSourceProperties dataSourceProperties;
    private final ForecastModelEvaluationRepository evaluationRepository;
    private final ForecastGenerationService forecastGenerationService;

    public AdminReplenishmentController(                                        CoreSnapshotSyncService snapshotSyncService,
                                        ReplenishmentRecommendationRepository recommendationRepository,
                                        InventoryPolicyRepository policyRepository,
                                        VariantReadRepository variantRepository,
                                        DailyDemandService dailyDemandService,
                                        ForecastBacktestService forecastBacktestService,
                                        DemandClassificationService demandClassificationService,
                                        DemandForecastService demandForecastService,
                                        ForecastDataSourceProperties dataSourceProperties,
                                        ForecastModelEvaluationRepository evaluationRepository,
                                        ForecastGenerationService forecastGenerationService) {
        this.snapshotSyncService = snapshotSyncService;
        this.recommendationRepository = recommendationRepository;
        this.policyRepository = policyRepository;
        this.variantRepository = variantRepository;
        this.dailyDemandService = dailyDemandService;
        this.forecastBacktestService = forecastBacktestService;
        this.demandClassificationService = demandClassificationService;
        this.demandForecastService = demandForecastService;
        this.dataSourceProperties = dataSourceProperties;
        this.evaluationRepository = evaluationRepository;
        this.forecastGenerationService = forecastGenerationService;
    }

    @GetMapping("/suggestions")
    @Transactional(readOnly = true)
    public ApiResponse<Page<ReplenishmentSuggestionResponse>> listSuggestions(
            @RequestParam(required = false) ReplenishmentStatus status,
            @RequestParam(required = false) ReplenishmentPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ReplenishmentRecommendation> result = recommendationRepository.searchRecommendations(
                status == null ? null : status.name(),
                priority == null ? null : priority.name(),
                keyword,
                pageable);

        List<UUID> variantIds = result.getContent().stream().map(ReplenishmentRecommendation::getVariantId).toList();
        Map<UUID, VariantSnapshot> variantMap = new HashMap<>();
        variantRepository.findAllByIds(variantIds).forEach(v -> variantMap.put(v.id(), v));

        Page<ReplenishmentSuggestionResponse> responsePage = result.map(rec -> mapToResponse(rec, variantMap.get(rec.getVariantId())));
        return ApiResponse.ok(responsePage);
    }

    @GetMapping("/suggestions/{id}")
    @Transactional(readOnly = true)
    public ApiResponse<ReplenishmentSuggestionDetailResponse> getDetail(@PathVariable UUID id) {
        ReplenishmentRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Recommendation not found"));

        ReplenishmentSuggestionDetailResponse detail = new ReplenishmentSuggestionDetailResponse();
        VariantSnapshot variant = variantRepository.findById(rec.getVariantId())
                .orElseThrow(() -> new IllegalStateException("Core variant no longer exists: " + rec.getVariantId()));
        mapToResponse(rec, detail, variant);

        InventoryPolicy policy = policyRepository.findByVariantId(rec.getVariantId())
                .orElse(null);
        if (policy != null) {
            detail.setPolicyLeadTimeDays(policy.getLeadTimeDays());
            detail.setPolicyTargetCoverDays(policy.getTargetCoverDays());
            detail.setPolicyServiceLevel(policy.getServiceLevel().doubleValue());
        }

        detail.setExplanationJson(rec.getExplanation());
        populateForecastDetail(detail, rec);

        return ApiResponse.ok(detail);
    }

    @PostMapping("/snapshots/sync")
    public ApiResponse<CoreSnapshotSyncService.SyncResult> syncSnapshot(
            @RequestBody(required = false) GenerateForecastRequest request) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<UUID> ids = request == null ? List.of() : request.getVariantIds();
        return ApiResponse.ok(snapshotSyncService.sync(today.minusDays(180), today, ids));
    }

    @PostMapping("/generate")
    public ApiResponse<Void> generate(
            @RequestBody(required = false) GenerateForecastRequest request) {
        List<UUID> requestedIds = request == null ? List.of() : request.getVariantIds();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate from = today.minusDays(179);

        forecastGenerationService.startSync();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                snapshotSyncService.sync(from, today, requestedIds);
                String source = dataSourceProperties.dataSource();
                demandClassificationService.classifyBatch(from, today, source);
                List<UUID> requestedVariantIds = requestedIds == null || requestedIds.isEmpty()
                        ? variantRepository.findAllActiveIds(source) : requestedIds;
                java.util.Set<UUID> requestedSet = new java.util.HashSet<>(requestedVariantIds);
                List<UUID> eligibleVariantIds = demandClassificationService.listSaved(source).stream()
                        .filter(row -> requestedSet.contains(row.variantId()))
                        .filter(row -> row.classification() != DemandPattern.NO_DEMAND
                                && row.classification() != DemandPattern.INSUFFICIENT_DATA)
                        .map(com.dunghaiquyen.ecommerce.modules.demand.dto.DemandClassificationResponse::variantId)
                        .toList();
                Map<UUID, ForecastModelEvaluation> existingEvaluations = evaluationRepository
                        .findAllByVariantIdInAndDataSource(eligibleVariantIds, source).stream()
                        .collect(java.util.stream.Collectors.toMap(ForecastModelEvaluation::getVariantId, row -> row));
                List<UUID> variantsNeedingEvaluation = eligibleVariantIds.stream()
                        .filter(id -> {
                            ForecastModelEvaluation evaluation = existingEvaluations.get(id);
                            return evaluation == null || evaluation.getTrainingTo() == null
                                    || evaluation.getTrainingTo().isBefore(today);
                        })
                        .toList();
                if (!variantsNeedingEvaluation.isEmpty()) {
                    demandForecastService.evaluateModelsBatch(variantsNeedingEvaluation, from, today);
                }
                forecastGenerationService.startGenerationAsync(eligibleVariantIds, from, today);
            } catch (Exception e) {
                log.error("Failed during sync phase", e);
                forecastGenerationService.failBatch();
            }
        });

        return ApiResponse.ok(null);
    }

    @PostMapping("/evaluate")
    public ApiResponse<Void> evaluate(
            @RequestBody(required = false) GenerateForecastRequest request) {
        List<UUID> requestedIds = request == null ? List.of() : request.getVariantIds();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate from = today.minusDays(180);

        forecastGenerationService.startSync();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                snapshotSyncService.sync(from, today, requestedIds);
                List<UUID> variantIds = requestedIds == null || requestedIds.isEmpty()
                        ? variantRepository.findAllActiveIds()
                        : requestedIds;
                forecastGenerationService.startEvaluationAsync(variantIds, from, today);
            } catch (Exception e) {
                log.error("Failed during sync phase", e);
                forecastGenerationService.failBatch();
            }
        });

        return ApiResponse.ok(null);
    }

    @GetMapping("/generate/status")
    public ApiResponse<com.dunghaiquyen.ecommerce.modules.replenishment.dto.ForecastGenerationStatus> getGenerationStatus() {
        return ApiResponse.ok(forecastGenerationService.getStatus());
    }

    @PutMapping("/policies/{variantId}")
    public ApiResponse<Void> updatePolicy(@PathVariable UUID variantId, @Valid @RequestBody InventoryPolicyRequest request) {
        InventoryPolicy policy = policyRepository.findByVariantId(variantId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Policy not found"));

        policy.setLeadTimeDays(request.getLeadTimeDays());
        policy.setTargetCoverDays(request.getTargetCoverDays());
        policy.setServiceLevel(request.getServiceLevel());
        policy.setMinimumOrderQuantity(request.getMinimumOrderQuantity());
        policy.setPackSize(request.getPackSize());
        policy.setSupplierName(request.getSupplierName());
        policy.setActive(request.isActive());

        log.info("[AUDIT] AI Policy updated by Admin for variantId: {}, leadTime={}, targetCover={}, serviceLevel={}, MOQ={}, packSize={}",
                variantId, request.getLeadTimeDays(), request.getTargetCoverDays(), request.getServiceLevel(), request.getMinimumOrderQuantity(), request.getPackSize());

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
            throw new IllegalStateException("Invalid state transition");
        }
        ensureActionableConfidence(rec);
        rec.setStatus(ReplenishmentStatus.ACCEPTED);
        rec.setAdminQuantity(rec.getSuggestedQuantity());
        if (request != null && request.getNote() != null) {
            rec.setAdminNote(request.getNote());
        }
        rec.setActedBy(principal.getUserId());
        rec.setActedAt(Instant.now());

        log.info("[AUDIT] Recommendation {} ACCEPTED by user {}, variant={}, qty={}, note={}",
                id, principal.getUserId(), rec.getVariantId(), rec.getAdminQuantity(), rec.getAdminNote());

        recommendationRepository.save(rec);
        return ApiResponse.ok(null);
    }

    @PostMapping("/suggestions/{id}/adjust")
    public ApiResponse<Void> adjustSuggestion(
            @PathVariable UUID id,
            @RequestBody ReplenishmentActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ReplenishmentRecommendation rec = recommendationRepository.findById(id).orElseThrow();
        ensureActionableConfidence(rec);
        if (rec.getStatus() != ReplenishmentStatus.PENDING) {
            throw new IllegalStateException("Invalid state transition");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity is invalid");
        }

        rec.setStatus(ReplenishmentStatus.ADJUSTED);
        rec.setAdminQuantity(request.getQuantity());
        rec.setAdminNote(request.getNote());
        rec.setActedBy(principal.getUserId());
        rec.setActedAt(Instant.now());

        log.info("[AUDIT] Recommendation {} ADJUSTED by user {}, variant={}, new_qty={}, note={}",
                id, principal.getUserId(), rec.getVariantId(), rec.getAdminQuantity(), rec.getAdminNote());

        recommendationRepository.save(rec);
        return ApiResponse.ok(null);
    }

    private void ensureActionableConfidence(ReplenishmentRecommendation recommendation) {
        ForecastRun run = recommendation.getForecastRun();
        if (run == null || run.getConfidence() == ForecastConfidence.LOW
                || run.getConfidence() == ForecastConfidence.INSUFFICIENT) {
            throw new IllegalStateException("Forecast confidence is too low for an import decision");
        }
    }

    @PostMapping("/suggestions/{id}/dismiss")
    public ApiResponse<Void> dismissSuggestion(
            @PathVariable UUID id,
            @RequestBody ReplenishmentActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ReplenishmentRecommendation rec = recommendationRepository.findById(id).orElseThrow();
        if (rec.getStatus() != ReplenishmentStatus.PENDING) {
            throw new IllegalStateException("Invalid state transition");
        }
        if (request.getNote() == null || request.getNote().isBlank()) {
            throw new IllegalArgumentException("Note is required for dismissal");
        }
        rec.setStatus(ReplenishmentStatus.DISMISSED);
        rec.setAdminNote(request.getNote());
        rec.setActedBy(principal.getUserId());
        rec.setActedAt(Instant.now());

        log.info("[AUDIT] Recommendation {} DISMISSED by user {}, variant={}, note={}",
                id, principal.getUserId(), rec.getVariantId(), rec.getAdminNote());

        recommendationRepository.save(rec);
        return ApiResponse.ok(null);
    }

    private void populateForecastDetail(ReplenishmentSuggestionDetailResponse detail,
                                        ReplenishmentRecommendation recommendation) {
        ForecastRun run = recommendation.getForecastRun();
        if (run == null) {
            detail.setHistoryData(List.of());
            detail.setFutureForecastData(List.of());
            detail.setModelMetrics(List.of());
            return;
        }
        ForecastModelEvaluation evaluation = evaluationRepository.findById(recommendation.getVariantId()).orElse(null);
        List<DailyDemandService.DailyDemandPoint> points = dailyDemandService
                .getDailyDemand(List.of(recommendation.getVariantId()), run.getTrainingFrom(), run.getTrainingTo())
                .getOrDefault(recommendation.getVariantId(), List.of());
        List<Integer> demand = points.stream().map(point -> (int) point.quantity()).toList();
        int testWindow = evaluation != null && evaluation.getTestWindowDays() != null
                ? Math.min(evaluation.getTestWindowDays(), Math.max(0, demand.size() - 1))
                : Math.min(30, Math.max(0, demand.size() - 1));
        var backtest = forecastBacktestService.runBacktest(demand, testWindow);
        Map<Integer, Double> predictions = new HashMap<>();
        forecastBacktestService.buildPredictions(demand, testWindow, run.getAlgorithm())
                .forEach(point -> predictions.put(point.index(), point.predicted()));
        int firstBacktestIndex = demand.size() - testWindow;
        detail.setHistoryData(java.util.stream.IntStream.range(0, points.size())
                .mapToObj(index -> new ReplenishmentSuggestionDetailResponse.DailyChartData(
                        points.get(index).date().toString(), (double) points.get(index).quantity(),
                        predictions.get(index), index >= firstBacktestIndex))
                .toList());
        List<Double> persistedForecast = run.getDailyForecast();
        if (persistedForecast != null && persistedForecast.size() == run.getForecastHorizonDays()) {
            detail.setFutureForecastData(java.util.stream.IntStream.rangeClosed(1, run.getForecastHorizonDays())
                    .mapToObj(day -> new ReplenishmentSuggestionDetailResponse.DailyChartData(
                            run.getTrainingTo().plusDays(day).toString(), null, persistedForecast.get(day - 1), false))
                    .toList());
        } else {
            double dailyForecast = run.getAverageDailyDemand().doubleValue();
            detail.setFutureForecastData(java.util.stream.IntStream.rangeClosed(1, run.getForecastHorizonDays())
                    .mapToObj(day -> new ReplenishmentSuggestionDetailResponse.DailyChartData(
                            run.getTrainingTo().plusDays(day).toString(), null, dailyForecast, false))
                    .toList());
        }
        detail.setModelMetrics(backtest.allMetrics().stream()
                .map(metric -> new ReplenishmentSuggestionDetailResponse.ModelMetric(
                        metric.algorithm().name(), metric.mae(), metric.wape(), metric.bias(), metric.residualStdDev(),
                        evaluation != null && evaluation.getBenchmarkAlgorithm() == metric.algorithm(),
                        metric.algorithm() == run.getAlgorithm()))
                .toList());
        detail.setSelectedModel(run.getAlgorithm().name());
        detail.setSelectionReason("Mô hình có WAPE thấp nhất trên 30 ngày backtest walk-forward; nếu hòa, ưu tiên mô hình đơn giản hơn.");
        detail.setForecastConfidence(run.getConfidence().name());
        detail.setResidualStdDev(run.getResidualStdDev() != null ? run.getResidualStdDev().doubleValue() : null);
        if (evaluation != null) {
            detail.setSelectionReason(evaluation.getSelectionReason());
            detail.setDataSource(evaluation.getDataSource());
            detail.setDemandPattern(evaluation.getDemandPattern());
            detail.setBias(evaluation.getBias() != null ? evaluation.getBias().doubleValue() : null);
            detail.setResidualStdDev(evaluation.getResidualStdDev() != null
                    ? evaluation.getResidualStdDev().doubleValue()
                    : detail.getResidualStdDev());
            detail.setBacktestWindows(evaluation.getBacktestWindows());
            detail.setTestWindowDays(evaluation.getTestWindowDays());
            detail.setTrainingFrom(evaluation.getTrainingFrom() != null ? evaluation.getTrainingFrom().toString() : null);
            detail.setTrainingTo(evaluation.getTrainingTo() != null ? evaluation.getTrainingTo().toString() : null);
            detail.setBenchmarkAlgorithm(evaluation.getBenchmarkAlgorithm() != null ? evaluation.getBenchmarkAlgorithm().name() : null);
            detail.setBenchmarkMae(evaluation.getBenchmarkMae() != null ? evaluation.getBenchmarkMae().doubleValue() : null);
            detail.setBenchmarkWape(evaluation.getBenchmarkWape() != null ? evaluation.getBenchmarkWape().doubleValue() : null);
            detail.setFallbackReason(evaluation.getFallbackReason());
        } else {
            detail.setSelectionReason(backtest.reason());
        }
    }
    private ReplenishmentSuggestionResponse mapToResponse(ReplenishmentRecommendation rec, VariantSnapshot variant) {
        ReplenishmentSuggestionResponse res = new ReplenishmentSuggestionResponse();
        if (variant == null) {
            throw new IllegalStateException("Core variant no longer exists: " + rec.getVariantId());
        }
        mapToResponse(rec, res, variant);
        return res;
    }

    private void mapToResponse(ReplenishmentRecommendation rec, ReplenishmentSuggestionResponse res, VariantSnapshot variant) {
        res.setId(rec.getId());
        res.setVariantId(variant.id());
        res.setProductId(variant.productId());
        res.setSku(variant.sku());
        res.setProductName(variant.productName());
        res.setSize(variant.size());
        res.setColor(variant.color());
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


