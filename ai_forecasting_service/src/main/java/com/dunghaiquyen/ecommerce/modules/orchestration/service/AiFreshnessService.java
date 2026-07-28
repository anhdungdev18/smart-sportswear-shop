package com.dunghaiquyen.ecommerce.modules.orchestration.service;

import com.dunghaiquyen.ecommerce.config.ForecastDataSourceProperties;
import com.dunghaiquyen.ecommerce.modules.orchestration.dto.AiDataFreshnessResponse;
import com.dunghaiquyen.ecommerce.modules.orchestration.repository.AiFreshnessRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiFreshnessService {

    private final AiFreshnessRepository repository;
    private final ForecastDataSourceProperties dataSourceProperties;
    private final long staleAfterMinutes;

    public AiFreshnessService(
            AiFreshnessRepository repository,
            ForecastDataSourceProperties dataSourceProperties,
            @Value("${app.ai.freshness-stale-after-minutes:720}") long staleAfterMinutes) {
        this.repository = repository;
        this.dataSourceProperties = dataSourceProperties;
        this.staleAfterMinutes = staleAfterMinutes;
    }

    @Transactional(readOnly = true)
    public AiDataFreshnessResponse getFreshness(String requestedDataSource) {
        String dataSource = requestedDataSource == null || requestedDataSource.isBlank()
                ? dataSourceProperties.dataSource()
                : requestedDataSource.toUpperCase();
        Instant checkedAt = Instant.now();
        Instant latestForecast = repository.latestForecastGeneratedAt(dataSource);
        Instant latestEvaluation = repository.latestEvaluationUpdatedAt(dataSource);
        Instant latestRecommendation = repository.latestRecommendationCreatedAt(dataSource);
        boolean stale = latestForecast == null
                || latestEvaluation == null
                || latestRecommendation == null
                || Duration.between(latestForecast, checkedAt).toMinutes() > staleAfterMinutes;
        return new AiDataFreshnessResponse(
                dataSource,
                checkedAt,
                repository.latestSalesDate(dataSource),
                latestForecast,
                latestEvaluation,
                latestRecommendation,
                repository.salesRows(dataSource),
                repository.forecastRows(dataSource),
                repository.evaluationRows(dataSource),
                repository.pendingRecommendations(dataSource),
                stale,
                staleAfterMinutes);
    }
}
