package com.dunghaiquyen.ecommerce.modules.decision.service;

import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryDecisionFormula;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskLevel;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskResponse;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventoryRiskType;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventorySimulationRequest;
import com.dunghaiquyen.ecommerce.modules.decision.dto.InventorySimulationResponse;
import com.dunghaiquyen.ecommerce.modules.decision.dto.OverstockMetrics;
import com.dunghaiquyen.ecommerce.modules.decision.dto.ReplenishmentExplanationResponse;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastModelEvaluation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ForecastModelEvaluationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ForecastRunRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.InventoryPolicyRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantReadRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryDecisionEngineService {

    private final VariantReadRepository variantRepository;
    private final InventoryPolicyRepository policyRepository;
    private final ForecastRunRepository forecastRunRepository;
    private final ForecastModelEvaluationRepository evaluationRepository;
    private final ReplenishmentRecommendationRepository recommendationRepository;

    public InventoryDecisionEngineService(
            VariantReadRepository variantRepository,
            InventoryPolicyRepository policyRepository,
            ForecastRunRepository forecastRunRepository,
            ForecastModelEvaluationRepository evaluationRepository,
            ReplenishmentRecommendationRepository recommendationRepository) {
        this.variantRepository = variantRepository;
        this.policyRepository = policyRepository;
        this.forecastRunRepository = forecastRunRepository;
        this.evaluationRepository = evaluationRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryRiskResponse> listRisks(InventoryRiskType risk) {
        List<ReplenishmentRecommendation> pending = recommendationRepository.findAllByStatus(ReplenishmentStatus.PENDING);
        List<UUID> variantIds = pending.stream().map(ReplenishmentRecommendation::getVariantId).distinct().toList();
        var variants = variantRepository.findAllByIds(variantIds).stream()
                .collect(Collectors.toMap(VariantSnapshot::id, v -> v));
        var policies = policyRepository.findAllByVariantIdIn(variantIds).stream()
                .collect(Collectors.toMap(InventoryPolicy::getVariantId, p -> p, (first, second) -> first));
        var evaluations = evaluationRepository.findAllByVariantIdIn(variantIds).stream()
                .collect(Collectors.toMap(ForecastModelEvaluation::getVariantId, e -> e, (first, second) -> first));

        return pending.stream()
                .map(rec -> toRisk(variants.get(rec.getVariantId()), rec.getForecastRun(),
                        policies.get(rec.getVariantId()),
                        evaluations.get(rec.getVariantId()),
                        rec.getIncomingQuantity()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(response -> risk == null || response.risk() == risk)
                .sorted(Comparator.comparing(InventoryRiskResponse::severity))
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryRiskResponse getRisk(UUID variantId) {
        VariantSnapshot variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant snapshot not found"));
        ForecastRun forecastRun = forecastRunRepository.findFirstByVariantIdOrderByGeneratedAtDesc(variantId)
                .orElse(null);
        InventoryPolicy policy = policyRepository.findByVariantId(variantId).orElse(null);
        ForecastModelEvaluation evaluation = evaluationRepository.findById(variantId).orElse(null);
        return toRisk(variant, forecastRun, policy, evaluation, 0)
                .orElseThrow(() -> new NoSuchElementException("Inventory risk cannot be calculated"));
    }

    @Transactional(readOnly = true)
    public ReplenishmentExplanationResponse explainRecommendation(UUID recommendationId) {
        ReplenishmentRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new NoSuchElementException("Recommendation not found"));
        VariantSnapshot variant = variantRepository.findById(recommendation.getVariantId())
                .orElseThrow(() -> new NoSuchElementException("Variant snapshot not found"));
        InventoryPolicy policy = policyRepository.findByVariantId(recommendation.getVariantId()).orElse(null);
        ForecastModelEvaluation evaluation = evaluationRepository.findById(recommendation.getVariantId()).orElse(null);
        InventoryRiskResponse decision = toRisk(variant, recommendation.getForecastRun(), policy, evaluation,
                recommendation.getIncomingQuantity())
                .orElseThrow(() -> new NoSuchElementException("Recommendation cannot be explained"));
        return new ReplenishmentExplanationResponse(recommendation.getId(), recommendation.getVariantId(),
                decision, recommendation.getExplanation());
    }

    @Transactional(readOnly = true)
    public InventorySimulationResponse simulate(InventorySimulationRequest request) {
        InventoryRiskResponse current = getRisk(request.variantId());
        VariantSnapshot baseVariant = variantRepository.findById(request.variantId())
                .orElseThrow(() -> new NoSuchElementException("Variant snapshot not found"));
        ForecastRun forecastRun = forecastRunRepository.findFirstByVariantIdOrderByGeneratedAtDesc(request.variantId())
                .orElseThrow(() -> new NoSuchElementException("Forecast run not found"));
        InventoryPolicy basePolicy = policyRepository.findByVariantId(request.variantId())
                .orElseThrow(() -> new NoSuchElementException("Inventory policy not found"));
        ForecastModelEvaluation evaluation = evaluationRepository.findById(request.variantId()).orElse(null);

        VariantSnapshot simulatedVariant = new VariantSnapshot(
                baseVariant.id(),
                baseVariant.productId(),
                baseVariant.sku(),
                baseVariant.productName(),
                baseVariant.size(),
                baseVariant.color(),
                request.availableQuantity() != null ? request.availableQuantity() : baseVariant.stockQuantity(),
                0);
        InventoryPolicy simulatedPolicy = copyPolicy(basePolicy);
        if (request.leadTimeDays() != null) simulatedPolicy.setLeadTimeDays(request.leadTimeDays());
        if (request.targetCoverDays() != null) simulatedPolicy.setTargetCoverDays(request.targetCoverDays());
        if (request.serviceLevel() != null) simulatedPolicy.setServiceLevel(BigDecimal.valueOf(request.serviceLevel()));
        if (request.minimumOrderQuantity() != null) simulatedPolicy.setMinimumOrderQuantity(request.minimumOrderQuantity());
        if (request.packSize() != null) simulatedPolicy.setPackSize(request.packSize());

        InventoryRiskResponse simulated = toRisk(simulatedVariant, forecastRun, simulatedPolicy, evaluation,
                request.incomingQuantity() != null ? request.incomingQuantity() : current.incomingQuantity())
                .orElseThrow(() -> new NoSuchElementException("Simulation cannot be calculated"));
        Integer stockoutDelta = current.estimatedStockoutDays() == null || simulated.estimatedStockoutDays() == null
                ? null
                : simulated.estimatedStockoutDays() - current.estimatedStockoutDays();
        List<String> warnings = new ArrayList<>(simulated.warnings());
        warnings.add("What-if simulation is read-only and does not update inventory policy or recommendations.");
        return new InventorySimulationResponse(
                request.variantId(),
                current,
                simulated,
                simulated.suggestedQuantity() - current.suggestedQuantity(),
                simulated.reorderPoint() - current.reorderPoint(),
                stockoutDelta,
                warnings);
    }

    Optional<InventoryRiskResponse> toRisk(VariantSnapshot variant,
                                           ForecastRun forecastRun,
                                           InventoryPolicy policy,
                                           ForecastModelEvaluation evaluation,
                                           int incomingQuantity) {
        if (variant == null) return Optional.empty();
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (forecastRun == null || forecastRun.getConfidence() == ForecastConfidence.INSUFFICIENT) {
            warnings.add("Forecast is missing or insufficient; no automatic replenishment decision is produced.");
            return Optional.of(baseInsufficientResponse(variant, forecastRun, evaluation, warnings));
        }
        if (policy == null || !policy.isActive()) {
            warnings.add("Inventory policy is missing or inactive; no automatic replenishment decision is produced.");
            return Optional.of(baseInsufficientResponse(variant, forecastRun, evaluation, warnings));
        }

        int available = Math.max(0, variant.availableQuantity());
        double averageDailyDemand = nonNegative(forecastRun.getAverageDailyDemand());
        int leadTimeDays = Math.max(1, policy.getLeadTimeDays());
        int targetCoverDays = Math.max(1, policy.getTargetCoverDays());
        int minimumOrder = Math.max(1, policy.getMinimumOrderQuantity());
        int packSize = Math.max(1, policy.getPackSize());
        double residualStdDev = nonNegative(forecastRun.getResidualStdDev());
        double serviceLevel = policy.getServiceLevel() != null ? policy.getServiceLevel().doubleValue() : 0.95;
        double zScore = zScore(serviceLevel);

        int expectedDemandDuringLeadTime = (int) Math.ceil(averageDailyDemand * leadTimeDays);
        int safetyStock = residualStdDev > 0
                ? (int) Math.ceil(zScore * residualStdDev * Math.sqrt(leadTimeDays))
                : (int) Math.ceil(averageDailyDemand * 3);
        int reorderPoint = expectedDemandDuringLeadTime + safetyStock;
        int targetStock = (int) Math.ceil(averageDailyDemand * (leadTimeDays + targetCoverDays) + safetyStock);
        int rawSuggestion = Math.max(0, targetStock - available - Math.max(0, incomingQuantity));
        if (rawSuggestion > 0) rawSuggestion = Math.max(rawSuggestion, minimumOrder);
        int suggestedQuantity = roundToPack(rawSuggestion, packSize);
        Integer stockoutDays = averageDailyDemand > 0 ? (int) Math.floor(available / averageDailyDemand) : null;
        Double stockoutProbability = stockoutProbability(available, reorderPoint, leadTimeDays, stockoutDays);
        OverstockMetrics overstock = overstock(available, averageDailyDemand, targetStock, targetCoverDays);
        InventoryRiskType risk = riskType(averageDailyDemand, available, reorderPoint, overstock.excessQuantity(), evaluation);
        InventoryRiskLevel severity = severity(risk, available, reorderPoint, leadTimeDays, stockoutDays, suggestedQuantity, overstock.excessQuantity());

        reasons.add("Available quantity is " + available + ".");
        reasons.add("Expected demand during lead time is " + expectedDemandDuringLeadTime + ".");
        reasons.add("Reorder point is " + reorderPoint + " = lead-time demand + safety stock.");
        if (suggestedQuantity > 0) {
            reasons.add("Suggested quantity applies MOQ " + minimumOrder + " and pack size " + packSize + ".");
        }
        if (policy.getSupplierName() == null || policy.getSupplierName().isBlank()) {
            warnings.add("Supplier is not configured for this policy.");
        }
        if (policy.getLeadTimeDays() <= 0) warnings.add("Lead time was invalid and normalized to 1 day.");
        if (forecastRun.getConfidence() == ForecastConfidence.LOW) warnings.add("Forecast confidence is LOW.");

        InventoryDecisionFormula formula = new InventoryDecisionFormula(
                averageDailyDemand,
                leadTimeDays,
                targetCoverDays,
                residualStdDev,
                serviceLevel,
                zScore,
                Math.max(0, incomingQuantity),
                expectedDemandDuringLeadTime,
                safetyStock,
                reorderPoint,
                targetStock,
                rawSuggestion,
                minimumOrder,
                packSize,
                suggestedQuantity);

        return Optional.of(new InventoryRiskResponse(
                variant.id(),
                variant.productId(),
                variant.sku(),
                variant.productName(),
                variant.size(),
                variant.color(),
                risk,
                severity,
                available,
                Math.max(0, incomingQuantity),
                expectedDemandDuringLeadTime,
                safetyStock,
                reorderPoint,
                suggestedQuantity,
                stockoutDays,
                stockoutDays,
                stockoutProbability,
                overstock,
                forecastRun.getConfidence(),
                forecastRun.getAlgorithm() != null ? forecastRun.getAlgorithm().name() : null,
                evaluation != null ? evaluation.getDemandPattern() : null,
                formula,
                reasons,
                warnings,
                forecastRun.getGeneratedAt() != null ? forecastRun.getGeneratedAt() : Instant.now()));
    }

    private InventoryRiskResponse baseInsufficientResponse(VariantSnapshot variant,
                                                           ForecastRun forecastRun,
                                                           ForecastModelEvaluation evaluation,
                                                           List<String> warnings) {
        return new InventoryRiskResponse(
                variant.id(), variant.productId(), variant.sku(), variant.productName(), variant.size(), variant.color(),
                InventoryRiskType.INSUFFICIENT_DATA, InventoryRiskLevel.NONE, Math.max(0, variant.availableQuantity()),
                0, 0, 0, 0, 0, null, null, null,
                new OverstockMetrics(null, null, null, 0, null),
                forecastRun != null ? forecastRun.getConfidence() : ForecastConfidence.INSUFFICIENT,
                forecastRun != null && forecastRun.getAlgorithm() != null ? forecastRun.getAlgorithm().name() : null,
                evaluation != null ? evaluation.getDemandPattern() : null,
                new InventoryDecisionFormula(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                List.of(),
                warnings,
                forecastRun != null && forecastRun.getGeneratedAt() != null ? forecastRun.getGeneratedAt() : Instant.now());
    }

    private InventoryPolicy copyPolicy(InventoryPolicy source) {
        InventoryPolicy copy = new InventoryPolicy();
        copy.setVariantId(source.getVariantId());
        copy.setLeadTimeDays(source.getLeadTimeDays());
        copy.setTargetCoverDays(source.getTargetCoverDays());
        copy.setServiceLevel(source.getServiceLevel());
        copy.setMinimumOrderQuantity(source.getMinimumOrderQuantity());
        copy.setPackSize(source.getPackSize());
        copy.setSupplierName(source.getSupplierName());
        copy.setActive(source.isActive());
        return copy;
    }

    private double nonNegative(BigDecimal value) {
        return value == null ? 0.0 : Math.max(0.0, value.doubleValue());
    }

    private int roundToPack(int quantity, int packSize) {
        if (quantity <= 0) return 0;
        return (int) Math.ceil((double) quantity / packSize) * packSize;
    }

    private double zScore(double serviceLevel) {
        if (serviceLevel >= 0.99) return 2.33;
        if (serviceLevel >= 0.975) return 1.96;
        if (serviceLevel >= 0.95) return 1.65;
        if (serviceLevel >= 0.90) return 1.28;
        return 1.65;
    }

    private Double stockoutProbability(int available, int reorderPoint, int leadTimeDays, Integer stockoutDays) {
        if (stockoutDays == null) return 0.0;
        if (available == 0 || stockoutDays <= leadTimeDays) return 0.95;
        if (available <= reorderPoint) return 0.70;
        if (stockoutDays <= leadTimeDays + 7) return 0.40;
        return 0.10;
    }

    private OverstockMetrics overstock(int available, double averageDailyDemand, int targetStock, int targetCoverDays) {
        if (averageDailyDemand <= 0) {
            return new OverstockMetrics(null, available > 0 ? 999 : 0, null, available, null);
        }
        double daysOfSupply = available / averageDailyDemand;
        double turnover = averageDailyDemand * 365.0 / Math.max(1, available);
        int excessQuantity = daysOfSupply > targetCoverDays * 2.0 ? Math.max(0, available - targetStock) : 0;
        return new OverstockMetrics(daysOfSupply, null, turnover, excessQuantity, null);
    }

    private InventoryRiskType riskType(double averageDailyDemand, int available, int reorderPoint, int excessQuantity,
                                       ForecastModelEvaluation evaluation) {
        if (evaluation != null && "INSUFFICIENT_DATA".equals(evaluation.getDemandPattern())) {
            return InventoryRiskType.INSUFFICIENT_DATA;
        }
        if (averageDailyDemand <= 0) {
            return available > 0 ? InventoryRiskType.OVERSTOCK : InventoryRiskType.BALANCED;
        }
        if (available <= reorderPoint) return InventoryRiskType.STOCKOUT;
        if (excessQuantity > 0) return InventoryRiskType.OVERSTOCK;
        return InventoryRiskType.BALANCED;
    }

    private InventoryRiskLevel severity(InventoryRiskType risk, int available, int reorderPoint, int leadTimeDays,
                                        Integer stockoutDays, int suggestedQuantity, int excessQuantity) {
        if (risk == InventoryRiskType.INSUFFICIENT_DATA || risk == InventoryRiskType.BALANCED) return InventoryRiskLevel.NONE;
        if (risk == InventoryRiskType.OVERSTOCK) {
            if (excessQuantity >= Math.max(20, reorderPoint * 2)) return InventoryRiskLevel.HIGH;
            return InventoryRiskLevel.MEDIUM;
        }
        if (available == 0 || (stockoutDays != null && stockoutDays <= leadTimeDays)) return InventoryRiskLevel.CRITICAL;
        if (stockoutDays != null && stockoutDays <= leadTimeDays + 7) return InventoryRiskLevel.HIGH;
        if (suggestedQuantity > 0) return InventoryRiskLevel.MEDIUM;
        return InventoryRiskLevel.LOW;
    }
}
