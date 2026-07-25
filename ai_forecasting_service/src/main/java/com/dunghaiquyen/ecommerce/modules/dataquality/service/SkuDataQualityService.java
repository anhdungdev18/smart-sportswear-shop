package com.dunghaiquyen.ecommerce.modules.dataquality.service;

import com.dunghaiquyen.ecommerce.config.AiDataQualityProperties;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.DataQualitySummaryResponse;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.DataQualitySourceSummaryResponse;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.SkuDataQualityLevel;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.SkuDataQualityResponse;
import com.dunghaiquyen.ecommerce.modules.dataquality.repository.SkuDataQualityRepository;
import com.dunghaiquyen.ecommerce.modules.dataquality.repository.SkuDataQualityRow;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkuDataQualityService {

    private final SkuDataQualityRepository repository;
    private final AiDataQualityProperties properties;

    public SkuDataQualityService(SkuDataQualityRepository repository, AiDataQualityProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<SkuDataQualityResponse> listVariants(LocalDate fromInclusive, LocalDate toInclusive) {
        validateRange(fromInclusive, toInclusive);
        int expectedDays = expectedDays(fromInclusive, toInclusive);
        return repository.findQualityRows(fromInclusive, toInclusive).stream()
                .map(row -> map(row, fromInclusive, toInclusive, expectedDays))
                .toList();
    }

    @Transactional(readOnly = true)
    public SkuDataQualityResponse getVariant(UUID variantId, LocalDate fromInclusive, LocalDate toInclusive) {
        validateRange(fromInclusive, toInclusive);
        int expectedDays = expectedDays(fromInclusive, toInclusive);
        return repository.findQualityRow(variantId, fromInclusive, toInclusive)
                .map(row -> map(row, fromInclusive, toInclusive, expectedDays))
                .orElseThrow(() -> new java.util.NoSuchElementException("Variant not found"));
    }

    @Transactional(readOnly = true)
    public DataQualitySummaryResponse summarize(LocalDate fromInclusive, LocalDate toInclusive) {
        List<SkuDataQualityResponse> rows = listVariants(fromInclusive, toInclusive);
        return new DataQualitySummaryResponse(
                rows.size(),
                countLevel(rows, SkuDataQualityLevel.HIGH),
                countLevel(rows, SkuDataQualityLevel.MEDIUM),
                countLevel(rows, SkuDataQualityLevel.LOW),
                countLevel(rows, SkuDataQualityLevel.INSUFFICIENT),
                (int) rows.stream().filter(row -> !row.supplierConfigured()).count(),
                (int) rows.stream().filter(row -> row.missingDays() > 0).count(),
                (int) rows.stream().filter(row -> row.inventorySnapshotDays() < properties.minHistoryDays()).count(),
                summarizeBySource(rows));
    }

    private SkuDataQualityResponse map(SkuDataQualityRow row, LocalDate fromInclusive, LocalDate toInclusive,
                                       int expectedDays) {
        int missingDays = Math.max(0, expectedDays - row.salesRows());
        boolean supplierConfigured = row.supplierName() != null && !row.supplierName().isBlank();
        List<String> warnings = new ArrayList<>();
        if (missingDays > 0) {
            warnings.add("Sales series has missing days; missing demand must be materialized as quantity 0.");
        }
        if (row.nonZeroDays() < properties.minNonZeroDays()) {
            warnings.add("Non-zero demand days are below the minimum forecast threshold.");
        }
        if (row.inventorySnapshotDays() < properties.minHistoryDays()) {
            warnings.add("Inventory history is shorter than the minimum threshold.");
        }
        if (!supplierConfigured) {
            warnings.add("Supplier is not configured on the active inventory policy.");
        }

        int score = score(row, missingDays, supplierConfigured, expectedDays);
        SkuDataQualityLevel level = level(row, score, missingDays);
        Integer daysSinceLastSale = row.lastSaleDate() == null
                ? null
                : (int) ChronoUnit.DAYS.between(row.lastSaleDate(), toInclusive);

        return new SkuDataQualityResponse(
                row.variantId(),
                row.sku(),
                row.productName(),
                row.dataSource(),
                fromInclusive,
                toInclusive,
                expectedDays,
                row.nonZeroDays(),
                row.totalUnits(),
                missingDays,
                daysSinceLastSale,
                row.inventorySnapshotDays(),
                supplierConfigured,
                score,
                level,
                List.copyOf(warnings));
    }

    private int score(SkuDataQualityRow row, int missingDays, boolean supplierConfigured, int expectedDays) {
        int score = 100;
        score -= Math.min(40, missingDays * 40 / Math.max(1, expectedDays));
        if (row.nonZeroDays() < properties.highNonZeroDays()) {
            score -= row.nonZeroDays() >= properties.minNonZeroDays() ? 10 : 25;
        }
        if (row.inventorySnapshotDays() < properties.highHistoryDays()) {
            score -= row.inventorySnapshotDays() >= properties.minHistoryDays() ? 10 : 20;
        }
        if (!supplierConfigured) {
            score -= 15;
        }
        return Math.max(0, score);
    }

    private SkuDataQualityLevel level(SkuDataQualityRow row, int score, int missingDays) {
        if (missingDays > 0 || row.nonZeroDays() < properties.minNonZeroDays()) {
            return SkuDataQualityLevel.INSUFFICIENT;
        }
        if (score >= 80
                && row.nonZeroDays() >= properties.highNonZeroDays()
                && row.inventorySnapshotDays() >= properties.highHistoryDays()) {
            return SkuDataQualityLevel.HIGH;
        }
        if (score >= 60) {
            return SkuDataQualityLevel.MEDIUM;
        }
        return SkuDataQualityLevel.LOW;
    }

    private List<DataQualitySourceSummaryResponse> summarizeBySource(List<SkuDataQualityResponse> rows) {
        return rows.stream()
                .collect(Collectors.groupingBy(SkuDataQualityResponse::dataSource, Collectors.toList()))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> new DataQualitySourceSummaryResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        countLevel(entry.getValue(), SkuDataQualityLevel.HIGH),
                        countLevel(entry.getValue(), SkuDataQualityLevel.MEDIUM),
                        countLevel(entry.getValue(), SkuDataQualityLevel.LOW),
                        countLevel(entry.getValue(), SkuDataQualityLevel.INSUFFICIENT),
                        (int) entry.getValue().stream().filter(row -> !row.supplierConfigured()).count(),
                        (int) entry.getValue().stream().filter(row -> row.missingDays() > 0).count(),
                        (int) entry.getValue().stream()
                                .filter(row -> row.inventorySnapshotDays() < properties.minHistoryDays()).count()))
                .toList();
    }

    private int countLevel(List<SkuDataQualityResponse> rows, SkuDataQualityLevel level) {
        return (int) rows.stream().filter(row -> row.qualityLevel() == level).count();
    }

    private void validateRange(LocalDate fromInclusive, LocalDate toInclusive) {
        if (fromInclusive == null || toInclusive == null || fromInclusive.isAfter(toInclusive)) {
            throw new IllegalArgumentException("Data quality date range is invalid");
        }
    }

    private int expectedDays(LocalDate fromInclusive, LocalDate toInclusive) {
        return (int) ChronoUnit.DAYS.between(fromInclusive, toInclusive) + 1;
    }
}

