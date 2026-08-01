package com.dunghaiquyen.ecommerce.modules.demand.service;

import com.dunghaiquyen.ecommerce.config.AiDemandClassificationProperties;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandClassificationBatchResponse;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandClassificationResponse;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandConfidence;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandPattern;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository.DemandHistory;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository.DemandPoint;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandClassificationService {

    private final DemandClassificationRepository repository;
    private final AiDemandClassificationProperties properties;

    public DemandClassificationService(DemandClassificationRepository repository,
                                       AiDemandClassificationProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public DemandClassificationBatchResponse classifyBatch(LocalDate fromInclusive, LocalDate toInclusive,
                                                           String dataSource) {
        validateRange(fromInclusive, toInclusive);
        List<DemandClassificationResponse> rows = repository.findHistories(fromInclusive, toInclusive, dataSource).stream()
                .map(history -> classify(history, fromInclusive, toInclusive))
                .toList();
        repository.upsertAll(rows);
        return new DemandClassificationBatchResponse(rows.size(), rows.size(), properties.algorithmVersion());
    }

    @Transactional(readOnly = true)
    public List<DemandClassificationResponse> listSaved(String dataSource) {
        return repository.findSaved(dataSource);
    }

    @Transactional(readOnly = true)
    public DemandClassificationResponse getSaved(UUID variantId, String dataSource) {
        return repository.findSaved(variantId, dataSource)
                .orElseThrow(() -> new NoSuchElementException("Demand classification not found"));
    }

    DemandClassificationResponse classify(DemandHistory history, LocalDate fromInclusive, LocalDate toInclusive) {
        int expectedDays = (int) ChronoUnit.DAYS.between(fromInclusive, toInclusive) + 1;
        List<Long> quantities = history.points().stream().map(DemandPoint::quantity).toList();
        int observedDays = history.observedDays();
        int nonZeroDays = (int) quantities.stream().filter(quantity -> quantity > 0).count();
        long totalUnits = quantities.stream().mapToLong(Long::longValue).sum();
        double adi = nonZeroDays == 0 ? 0.0d : (double) observedDays / nonZeroDays;
        double cvSquared = cvSquared(quantities);
        double trendSlope = trendSlope(quantities);
        DemandPattern pattern = pattern(observedDays, nonZeroDays, totalUnits, adi, cvSquared, trendSlope);
        DemandConfidence confidence = confidence(observedDays, nonZeroDays, pattern);

        return new DemandClassificationResponse(
                history.variant().variantId(),
                history.variant().sku(),
                history.variant().productName(),
                history.variant().dataSource(),
                fromInclusive,
                toInclusive,
                observedDays,
                nonZeroDays,
                totalUnits,
                round(adi),
                round(cvSquared),
                round(trendSlope),
                pattern,
                confidence,
                reason(pattern, observedDays, nonZeroDays, adi, cvSquared, trendSlope, expectedDays),
                properties.algorithmVersion());
    }

    private DemandPattern pattern(int observedDays, int nonZeroDays, long totalUnits, double adi, double cvSquared,
                                  double trendSlope) {
        if (observedDays < properties.minHistoryDays()) {
            if (observedDays > 0 && observedDays <= properties.newItemMaxHistoryDays() && totalUnits > 0) {
                return DemandPattern.NEW_ITEM;
            }
            return DemandPattern.INSUFFICIENT_DATA;
        }
        if (totalUnits == 0 || nonZeroDays == 0) {
            return DemandPattern.NO_DEMAND;
        }
        if (nonZeroDays < properties.minNonZeroDays()) {
            return DemandPattern.INSUFFICIENT_DATA;
        }
        if (Math.abs(trendSlope) >= properties.trendSlopeThreshold()) {
            return trendSlope > 0 ? DemandPattern.GROWING : DemandPattern.DECLINING;
        }
        if (adi >= properties.intermittentAdiThreshold()) {
            return DemandPattern.INTERMITTENT;
        }
        if (cvSquared >= properties.erraticCvSquaredThreshold()) {
            return DemandPattern.ERRATIC;
        }
        return DemandPattern.SMOOTH;
    }

    private DemandConfidence confidence(int observedDays, int nonZeroDays, DemandPattern pattern) {
        if (pattern == DemandPattern.INSUFFICIENT_DATA || pattern == DemandPattern.NEW_ITEM) {
            return DemandConfidence.LOW;
        }
        if (observedDays >= 120 && nonZeroDays >= 30) {
            return DemandConfidence.HIGH;
        }
        if (observedDays >= properties.minHistoryDays() && nonZeroDays >= properties.minNonZeroDays()) {
            return DemandConfidence.MEDIUM;
        }
        return DemandConfidence.LOW;
    }

    private double cvSquared(List<Long> quantities) {
        double[] nonZero = quantities.stream().filter(quantity -> quantity > 0).mapToDouble(Long::doubleValue).toArray();
        if (nonZero.length <= 1) {
            return 0.0d;
        }
        double mean = java.util.Arrays.stream(nonZero).average().orElse(0.0d);
        if (mean == 0.0d) {
            return 0.0d;
        }
        double variance = java.util.Arrays.stream(nonZero)
                .map(quantity -> Math.pow(quantity - mean, 2))
                .sum() / (nonZero.length - 1);
        return variance / Math.pow(mean, 2);
    }

    private double trendSlope(List<Long> quantities) {
        int n = quantities.size();
        if (n <= 1) {
            return 0.0d;
        }
        double meanX = (n - 1) / 2.0d;
        double meanY = quantities.stream().mapToDouble(Long::doubleValue).average().orElse(0.0d);
        double numerator = 0.0d;
        double denominator = 0.0d;
        for (int i = 0; i < n; i++) {
            numerator += (i - meanX) * (quantities.get(i) - meanY);
            denominator += Math.pow(i - meanX, 2);
        }
        return denominator == 0.0d ? 0.0d : numerator / denominator;
    }

    private String reason(DemandPattern pattern, int observedDays, int nonZeroDays, double adi, double cvSquared,
                          double trendSlope, int expectedDays) {
        return switch (pattern) {
            case NO_DEMAND -> "No demand was observed in the classification window.";
            case NEW_ITEM -> "SKU has demand but only " + observedDays + " observed history days, so it is treated as a new item.";
            case INTERMITTENT -> "Demand is not continuous; ADI is " + round(adi) + " days per demand occurrence.";
            case ERRATIC -> "Non-zero demand is volatile; CV squared is " + round(cvSquared) + ".";
            case SMOOTH -> "Demand is regular with ADI " + round(adi) + " and CV squared " + round(cvSquared) + ".";
            case GROWING -> "Demand trend is increasing; slope is " + round(trendSlope) + " units per day.";
            case DECLINING -> "Demand trend is decreasing; slope is " + round(trendSlope) + " units per day.";
            case INSUFFICIENT_DATA -> "Only " + observedDays + "/" + expectedDays + " history days and " + nonZeroDays
                    + " non-zero days are available.";
        };
    }

    private void validateRange(LocalDate fromInclusive, LocalDate toInclusive) {
        if (fromInclusive == null || toInclusive == null || fromInclusive.isAfter(toInclusive)) {
            throw new IllegalArgumentException("Demand classification date range is invalid");
        }
    }

    private double round(double value) {
        return Math.round(value * 10000.0d) / 10000.0d;
    }
}
