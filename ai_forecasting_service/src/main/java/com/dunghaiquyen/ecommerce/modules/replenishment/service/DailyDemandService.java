package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.config.ForecastDataSourceProperties;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.DailyVariantDemandProjection;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.SalesHistoryRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyDemandService {

    private final SalesHistoryRepository salesHistoryRepository;
    private final ForecastDataSourceProperties forecastDataSourceProperties;

    public DailyDemandService(SalesHistoryRepository salesHistoryRepository,
            ForecastDataSourceProperties forecastDataSourceProperties) {
        this.salesHistoryRepository = salesHistoryRepository;
        this.forecastDataSourceProperties = forecastDataSourceProperties;
    }

    public record DailyDemandPoint(LocalDate date, long quantity) {
    }

    /**
     * Returns one point for every calendar day and requested variant. Missing
     * sales days are deliberately represented by zero to avoid inflating demand.
     */
    @Transactional(readOnly = true)
    public Map<UUID, List<DailyDemandPoint>> getDailyDemand(
            Collection<UUID> variantIds, LocalDate fromInclusive, LocalDate toInclusive) {
        if (fromInclusive == null || toInclusive == null || fromInclusive.isAfter(toInclusive)) {
            throw new IllegalArgumentException("Demand date range is invalid");
        }
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }

        List<UUID> requestedIds = variantIds.stream().distinct().toList();
        Instant from = fromInclusive.atStartOfDay(AppTimeZone.ZONE).toInstant();
        Instant toExclusive = toInclusive.plusDays(1).atStartOfDay(AppTimeZone.ZONE).toInstant();
        List<DailyVariantDemandProjection> rows = salesHistoryRepository.aggregateDailyDemand(
                from, toExclusive, requestedIds.toArray(UUID[]::new), forecastDataSourceProperties.dataSource());

        Map<UUID, Map<LocalDate, Long>> quantities = new LinkedHashMap<>();
        for (UUID variantId : requestedIds) {
            quantities.put(variantId, new LinkedHashMap<>());
        }
        for (DailyVariantDemandProjection row : rows) {
            Map<LocalDate, Long> byDate = quantities.get(row.getVariantId());
            if (byDate != null) {
                byDate.put(row.getDemandDate(), row.getQuantity());
            }
        }

        Map<UUID, List<DailyDemandPoint>> result = new LinkedHashMap<>();
        for (UUID variantId : requestedIds) {
            List<DailyDemandPoint> series = new ArrayList<>();
            Map<LocalDate, Long> byDate = quantities.get(variantId);
            for (LocalDate date = fromInclusive; !date.isAfter(toInclusive); date = date.plusDays(1)) {
                series.add(new DailyDemandPoint(date, byDate.getOrDefault(date, 0L)));
            }
            result.put(variantId, List.copyOf(series));
        }
        return result;
    }
}


