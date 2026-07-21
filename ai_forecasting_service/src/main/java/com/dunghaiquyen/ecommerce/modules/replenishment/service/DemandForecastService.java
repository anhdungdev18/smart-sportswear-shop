package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastModelEvaluation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ForecastModelEvaluationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ForecastRunRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.InventoryPolicyRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantReadRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandForecastService {

    private static final Logger log = LoggerFactory.getLogger(DemandForecastService.class);

    private final DailyDemandService dailyDemandService;
    private final ForecastBacktestService forecastBacktestService;
    private final ReplenishmentService replenishmentService;
    private final ForecastRunRepository forecastRunRepository;
    private final ReplenishmentRecommendationRepository recommendationRepository;
    private final InventoryPolicyRepository policyRepository;
    private final VariantReadRepository variantRepository;
    private final ForecastModelEvaluationRepository evaluationRepository;
    private final List<com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm> algorithms;

    public DemandForecastService(
            DailyDemandService dailyDemandService,
            ForecastBacktestService forecastBacktestService,
            ReplenishmentService replenishmentService,
            ForecastRunRepository forecastRunRepository,
            ReplenishmentRecommendationRepository recommendationRepository,
            InventoryPolicyRepository policyRepository,
            VariantReadRepository variantRepository,
            ForecastModelEvaluationRepository evaluationRepository,
            List<com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm> algorithms) {
        this.dailyDemandService = dailyDemandService;
        this.forecastBacktestService = forecastBacktestService;
        this.replenishmentService = replenishmentService;
        this.forecastRunRepository = forecastRunRepository;
        this.recommendationRepository = recommendationRepository;
        this.policyRepository = policyRepository;
        this.variantRepository = variantRepository;
        this.evaluationRepository = evaluationRepository;
        this.algorithms = algorithms;
    }

    public void evaluateModelsBatch(List<UUID> variantIds, LocalDate fromInclusive, LocalDate toInclusive) {
        if (variantIds == null || variantIds.isEmpty()) return;
        
        long start = System.currentTimeMillis();
        Map<UUID, List<DailyDemandService.DailyDemandPoint>> demandMap = 
                dailyDemandService.getDailyDemand(variantIds, fromInclusive, toInclusive);
        long loadDemandMillis = System.currentTimeMillis() - start;

        long backtestStart = System.currentTimeMillis();
        List<ForecastModelEvaluation> evaluations = new ArrayList<>();
        
        for (UUID variantId : variantIds) {
            List<DailyDemandService.DailyDemandPoint> demandPoints = demandMap.get(variantId);
            if (demandPoints == null || demandPoints.isEmpty()) continue;
            
            List<Integer> dailyDemand = demandPoints.stream()
                    .map(p -> (int) p.quantity())
                    .toList();
            
            int testWindowDays = 30;
            ForecastBacktestService.BacktestResult backtestResult = 
                    forecastBacktestService.runBacktest(dailyDemand, testWindowDays);
            
            ForecastBacktestService.BacktestMetric bestMetric = backtestResult.bestMetric();
            
            ForecastModelEvaluation eval = new ForecastModelEvaluation();
            eval.setVariantId(variantId);
            eval.setBestAlgorithm(backtestResult.bestAlgorithm());
            eval.setConfidence(backtestResult.confidence());
            eval.setLastEvaluatedAt(Instant.now());
            eval.setAlgorithmVersion(1);
            
            if (bestMetric != null) {
                eval.setMae(BigDecimal.valueOf(bestMetric.mae()));
                if (bestMetric.wape() != null) {
                    eval.setWape(BigDecimal.valueOf(bestMetric.wape()));
                }
                eval.setResidualStdDev(BigDecimal.valueOf(bestMetric.residualStdDev()));
            }
            
            evaluations.add(eval);
        }
        long backtestMillis = System.currentTimeMillis() - backtestStart;
        
        long saveStart = System.currentTimeMillis();
        evaluationRepository.saveAll(evaluations);
        long saveMillis = System.currentTimeMillis() - saveStart;
        
        log.info("Evaluate models chunk metrics: variants={}, loadDemand={}ms, backtest={}ms, save={}ms",
                variantIds.size(), loadDemandMillis, backtestMillis, saveMillis);
    }

    public void generateForecastAndRecommendationBatch(List<UUID> variantIds, LocalDate fromInclusive, LocalDate toInclusive) {
        if (variantIds == null || variantIds.isEmpty()) return;

        long totalStart = System.currentTimeMillis();
        
        long t0 = System.currentTimeMillis();
        List<VariantSnapshot> variants = variantRepository.findAllByIds(variantIds);
        Map<UUID, VariantSnapshot> variantMap = variants.stream()
                .collect(Collectors.toMap(VariantSnapshot::id, v -> v));
        long loadVariantsMillis = System.currentTimeMillis() - t0;

        long t1 = System.currentTimeMillis();
        List<InventoryPolicy> policies = policyRepository.findAllByVariantIdIn(variantIds);
        Map<UUID, InventoryPolicy> activePolicyMap = policies.stream()
                .filter(InventoryPolicy::isActive)
                .collect(Collectors.toMap(InventoryPolicy::getVariantId, p -> p));
        long loadPoliciesMillis = System.currentTimeMillis() - t1;

        List<UUID> activeVariantIds = activePolicyMap.keySet().stream().toList();
        if (activeVariantIds.isEmpty()) return;

        long t2 = System.currentTimeMillis();
        Map<UUID, List<DailyDemandService.DailyDemandPoint>> demandMap = 
                dailyDemandService.getDailyDemand(activeVariantIds, fromInclusive, toInclusive);
        long loadDemandMillis = System.currentTimeMillis() - t2;

        long tEval = System.currentTimeMillis();
        Map<UUID, ForecastModelEvaluation> evaluationMap = evaluationRepository.findAllByVariantIdIn(activeVariantIds).stream()
                .collect(Collectors.toMap(ForecastModelEvaluation::getVariantId, e -> e));
        long loadEvaluationMillis = System.currentTimeMillis() - tEval;

        long t3 = System.currentTimeMillis();
        List<ForecastRun> forecastRuns = new ArrayList<>();

        for (UUID variantId : activeVariantIds) {
            VariantSnapshot variant = variantMap.get(variantId);
            InventoryPolicy policy = activePolicyMap.get(variantId);
            
            List<DailyDemandService.DailyDemandPoint> demandPoints = demandMap.get(variantId);
            if (demandPoints == null || demandPoints.isEmpty()) continue;

            List<Integer> dailyDemand = demandPoints.stream()
                    .map(p -> (int) p.quantity())
                    .toList();

            ForecastModelEvaluation eval = evaluationMap.get(variantId);
            ForecastAlgorithmType bestAlgoType = eval != null ? eval.getBestAlgorithm() : ForecastAlgorithmType.MOVING_AVERAGE;
            
            ForecastRun run = new ForecastRun();
            run.setVariantId(variant.id());
            run.setAlgorithm(bestAlgoType);
            run.setTrainingFrom(fromInclusive);
            run.setTrainingTo(toInclusive);
            
            int horizonDays = policy.getLeadTimeDays() + policy.getTargetCoverDays();
            run.setForecastHorizonDays(horizonDays);
            
            com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm bestAlgo = algorithms.stream()
                    .filter(a -> a.type() == bestAlgoType)
                    .findFirst()
                    .orElse(null);

            if (bestAlgo != null) {
                com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastResult result = 
                        bestAlgo.forecast(dailyDemand, horizonDays);
                run.setAlgorithm(result.algorithm());
                run.setAverageDailyDemand(BigDecimal.valueOf(result.averageDailyDemand()));
                run.setForecastQuantity(BigDecimal.valueOf(result.forecastQuantity()));
                run.setDailyForecast(result.dailyForecast());
            } else {
                run.setAlgorithm(bestAlgoType);
                run.setAverageDailyDemand(BigDecimal.ZERO);
                run.setForecastQuantity(BigDecimal.ZERO);
            }
            
            if (eval != null) {
                run.setMae(eval.getMae());
                run.setWape(eval.getWape());
                run.setResidualStdDev(eval.getResidualStdDev());
                run.setConfidence(eval.getConfidence());
            } else {
                run.setConfidence(com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence.LOW);
            }
            run.setGeneratedAt(Instant.now());

            forecastRuns.add(run);
        }
        long forecastComputeMillis = System.currentTimeMillis() - t3;

        long t4 = System.currentTimeMillis();
        List<ForecastRun> savedRuns = forecastRunRepository.saveAll(forecastRuns);
        Map<UUID, ForecastRun> savedRunMap = savedRuns.stream()
                .collect(Collectors.toMap(ForecastRun::getVariantId, r -> r));
        long forecastInsertMillis = System.currentTimeMillis() - t4;

        long t5 = System.currentTimeMillis();
        List<ReplenishmentRecommendation> recommendationsToSave = new ArrayList<>();
        Map<UUID, ReplenishmentRecommendation> existingPendingMap = recommendationRepository
                .findAllByVariantIdInAndStatus(activeVariantIds, com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus.PENDING)
                .stream()
                .collect(Collectors.toMap(ReplenishmentRecommendation::getVariantId, r -> r));

        for (UUID variantId : activeVariantIds) {
            ForecastRun savedRun = savedRunMap.get(variantId);
            if (savedRun == null) continue;

            VariantSnapshot variant = variantMap.get(variantId);
            InventoryPolicy policy = activePolicyMap.get(variantId);

            ReplenishmentRecommendation generated = 
                    replenishmentService.generateRecommendation(variant, savedRun, policy);

            ReplenishmentRecommendation existing = existingPendingMap.get(variantId);

            if (generated.getSuggestedQuantity() > 0 || "CRITICAL".equals(generated.getPriority().name())) {
                if (existing != null) {
                    existing.setForecastRun(savedRun);
                    existing.setAvailableQuantity(generated.getAvailableQuantity());
                    existing.setIncomingQuantity(generated.getIncomingQuantity());
                    existing.setReorderPoint(generated.getReorderPoint());
                    existing.setSafetyStock(generated.getSafetyStock());
                    existing.setSuggestedQuantity(generated.getSuggestedQuantity());
                    existing.setEstimatedStockoutDays(generated.getEstimatedStockoutDays());
                    existing.setPriority(generated.getPriority());
                    existing.setExplanation(generated.getExplanation());
                    recommendationsToSave.add(existing);
                } else {
                    recommendationsToSave.add(generated);
                }
            } else {
                if (existing != null) {
                    existing.setStatus(com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus.DISMISSED);
                    existing.setAdminNote("Tự động hủy do dữ liệu mới cho thấy không còn cần nhập hàng");
                    recommendationsToSave.add(existing);
                }
            }
        }
        long recommendationComputeMillis = System.currentTimeMillis() - t5;

        long t6 = System.currentTimeMillis();
        recommendationRepository.saveAll(recommendationsToSave);
        long recommendationUpsertMillis = System.currentTimeMillis() - t6;
        
        long totalMillis = System.currentTimeMillis() - totalStart;

        log.info("Forecast chunk metrics: variants={}, loadVariants={}ms, loadPolicies={}ms, loadDemand={}ms, loadEvaluation={}ms, forecastCompute={}ms, forecastInsert={}ms, recCompute={}ms, recUpsert={}ms, total={}ms", 
                 activeVariantIds.size(), loadVariantsMillis, loadPoliciesMillis, loadDemandMillis, loadEvaluationMillis, forecastComputeMillis, forecastInsertMillis, recommendationComputeMillis, recommendationUpsertMillis, totalMillis);
    }
}
