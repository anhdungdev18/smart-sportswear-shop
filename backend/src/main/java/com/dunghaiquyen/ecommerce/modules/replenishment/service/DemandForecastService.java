package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ForecastRunRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.InventoryPolicyRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandForecastService {

    private final DailyDemandService dailyDemandService;
    private final ForecastBacktestService forecastBacktestService;
    private final ReplenishmentService replenishmentService;
    private final ForecastRunRepository forecastRunRepository;
    private final ReplenishmentRecommendationRepository recommendationRepository;
    private final InventoryPolicyRepository policyRepository;
    private final ProductVariantRepository variantRepository;

    public DemandForecastService(
            DailyDemandService dailyDemandService,
            ForecastBacktestService forecastBacktestService,
            ReplenishmentService replenishmentService,
            ForecastRunRepository forecastRunRepository,
            ReplenishmentRecommendationRepository recommendationRepository,
            InventoryPolicyRepository policyRepository,
            ProductVariantRepository variantRepository) {
        this.dailyDemandService = dailyDemandService;
        this.forecastBacktestService = forecastBacktestService;
        this.replenishmentService = replenishmentService;
        this.forecastRunRepository = forecastRunRepository;
        this.recommendationRepository = recommendationRepository;
        this.policyRepository = policyRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional
    public void generateForecastAndRecommendation(UUID variantId, LocalDate fromInclusive, LocalDate toInclusive) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        InventoryPolicy policy = policyRepository.findByVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory policy not found for variant"));

        if (!policy.isActive()) {
            return;
        }

        Map<UUID, List<DailyDemandService.DailyDemandPoint>> demandMap = 
                dailyDemandService.getDailyDemand(List.of(variantId), fromInclusive, toInclusive);

        List<DailyDemandService.DailyDemandPoint> demandPoints = demandMap.get(variantId);
        if (demandPoints == null || demandPoints.isEmpty()) {
            return;
        }

        List<Integer> dailyDemand = demandPoints.stream()
                .map(p -> (int) p.quantity())
                .toList();

        // Standard backtest window
        int testWindowDays = 30;
        ForecastBacktestService.BacktestResult backtestResult = 
                forecastBacktestService.runBacktest(dailyDemand, testWindowDays);

        ForecastBacktestService.BacktestMetric bestMetric = backtestResult.bestMetric();
        
        ForecastRun run = new ForecastRun();
        run.setVariant(variant);
        run.setAlgorithm(backtestResult.bestAlgorithm());
        run.setTrainingFrom(fromInclusive);
        run.setTrainingTo(toInclusive);
        
        // forecast horizon is lead time + target cover
        int horizonDays = policy.getLeadTimeDays() + policy.getTargetCoverDays();
        run.setForecastHorizonDays(horizonDays);
        
        if (bestMetric != null) {
            double avgDailyDemand = bestMetric.sumActual() / (double) testWindowDays;
            if (Double.isNaN(avgDailyDemand)) avgDailyDemand = 0;
            run.setAverageDailyDemand(BigDecimal.valueOf(avgDailyDemand));
            run.setForecastQuantity(BigDecimal.valueOf(avgDailyDemand * horizonDays));
            run.setMae(BigDecimal.valueOf(bestMetric.mae()));
            if (bestMetric.wape() != null) {
                run.setWape(BigDecimal.valueOf(bestMetric.wape()));
            }
            run.setResidualStdDev(BigDecimal.valueOf(bestMetric.residualStdDev()));
        } else {
            run.setAverageDailyDemand(BigDecimal.ZERO);
            run.setForecastQuantity(BigDecimal.ZERO);
        }
        
        run.setConfidence(backtestResult.confidence());
        run.setGeneratedAt(java.time.Instant.now());

        run = forecastRunRepository.save(run);

        ReplenishmentRecommendation recommendation = 
                replenishmentService.generateRecommendation(variant, run, policy);

        // Only save recommendation if suggestedQuantity > 0 or it's a critical item
        if (recommendation.getSuggestedQuantity() > 0 || "CRITICAL".equals(recommendation.getPriority().name())) {
            recommendationRepository.save(recommendation);
        }
    }
}
