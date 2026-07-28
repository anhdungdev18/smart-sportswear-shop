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
        return listVariants(fromInclusive, toInclusive, null);
    }

    @Transactional(readOnly = true)
    public List<SkuDataQualityResponse> listVariants(LocalDate fromInclusive, LocalDate toInclusive, String dataSource) {
        validateRange(fromInclusive, toInclusive);
        int expectedDays = expectedDays(fromInclusive, toInclusive);
        return repository.findQualityRows(fromInclusive, toInclusive, dataSource).stream()
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
        return summarize(fromInclusive, toInclusive, null);
    }

    @Transactional(readOnly = true)
    public DataQualitySummaryResponse summarize(LocalDate fromInclusive, LocalDate toInclusive, String dataSource) {
        List<SkuDataQualityResponse> rows = listVariants(fromInclusive, toInclusive, dataSource);
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
        if (row.nonZeroDays() == 0) warnings.add("No demand was recorded; route this SKU to inventory ageing instead of forecasting.");
        else if (row.nonZeroDays() < properties.minNonZeroDays()) warnings.add("Demand is sparse; use a sparse-demand or cold-start model.");
        if (row.inventorySnapshotDays() < properties.minHistoryDays()) {
            warnings.add("Inventory history is shorter than the minimum threshold.");
        }
        if (!supplierConfigured) {
            warnings.add("Supplier is not configured on the active inventory policy.");
        }

        int score = score(row, missingDays, expectedDays);
        SkuDataQualityLevel level = level(row, score, missingDays, expectedDays);
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

    private int score(SkuDataQualityRow row, int missingDays, int expectedDays) {
        int continuity = Math.max(0, 100 - Math.min(100, missingDays * 100 / Math.max(1, expectedDays)));
        int demandSignal = row.nonZeroDays() == 0 ? 0
                : Math.min(100, row.nonZeroDays() * 100 / Math.max(1, properties.highNonZeroDays()));
        int inventoryHistory = Math.min(100,
                row.inventorySnapshotDays() * 100 / Math.max(1, properties.highHistoryDays()));
        return (int) Math.round(continuity * 0.4 + demandSignal * 0.4 + inventoryHistory * 0.2);
    }

    private SkuDataQualityLevel level(SkuDataQualityRow row, int score, int missingDays, int expectedDays) {
        if (missingDays >= expectedDays) return SkuDataQualityLevel.INSUFFICIENT;
        if (row.nonZeroDays() == 0 || row.inventorySnapshotDays() < properties.minHistoryDays()) {
            return SkuDataQualityLevel.LOW;
        }
        if (missingDays == 0
                && row.nonZeroDays() >= properties.highNonZeroDays()
                && row.inventorySnapshotDays() >= properties.highHistoryDays()) {
            return SkuDataQualityLevel.HIGH;
        }
        if (score >= 60 && row.nonZeroDays() >= properties.minNonZeroDays()) return SkuDataQualityLevel.MEDIUM;
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

