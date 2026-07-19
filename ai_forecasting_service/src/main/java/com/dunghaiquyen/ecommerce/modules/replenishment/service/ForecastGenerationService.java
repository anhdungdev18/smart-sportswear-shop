package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ForecastGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ForecastGenerationService.class);

    private final DemandForecastService demandForecastService;
    private final int parallelism;
    private final AtomicBoolean running = new AtomicBoolean();

    public ForecastGenerationService(
            DemandForecastService demandForecastService,
            @Value("${app.forecast.generation-parallelism:4}") int parallelism) {
        this.demandForecastService = demandForecastService;
        this.parallelism = Math.max(1, Math.min(parallelism, 8));
    }

    public GenerationResult generate(List<UUID> variantIds, LocalDate fromInclusive, LocalDate toInclusive) {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalArgumentException("A forecast generation batch is already running");
        }

        Instant startedAt = Instant.now();
        try (ExecutorService executor = Executors.newFixedThreadPool(parallelism)) {
            List<CompletableFuture<VariantResult>> futures = variantIds.stream()
                    .distinct()
                    .map(variantId -> CompletableFuture.supplyAsync(
                            () -> generateOne(variantId, fromInclusive, toInclusive), executor))
                    .toList();
            List<VariantResult> results = futures.stream().map(CompletableFuture::join).toList();
            List<UUID> failedVariantIds = results.stream()
                    .filter(result -> !result.success())
                    .map(VariantResult::variantId)
                    .toList();
            int succeeded = results.size() - failedVariantIds.size();
            long durationMillis = Duration.between(startedAt, Instant.now()).toMillis();
            log.info("Forecast batch completed: requested={}, succeeded={}, failed={}, durationMs={}, parallelism={}",
                    results.size(), succeeded, failedVariantIds.size(), durationMillis, parallelism);
            return new GenerationResult(results.size(), succeeded, failedVariantIds.size(),
                    durationMillis, failedVariantIds);
        } finally {
            running.set(false);
        }
    }

    private VariantResult generateOne(UUID variantId, LocalDate fromInclusive, LocalDate toInclusive) {
        try {
            demandForecastService.generateForecastAndRecommendation(variantId, fromInclusive, toInclusive);
            log.debug("Forecast generated for variant {}", variantId);
            return new VariantResult(variantId, true);
        } catch (Exception exception) {
            log.error("Forecast generation failed for variant {}", variantId, exception);
            return new VariantResult(variantId, false);
        }
    }

    private record VariantResult(UUID variantId, boolean success) {}

    public record GenerationResult(
            int requested,
            int succeeded,
            int failed,
            long durationMillis,
            List<UUID> failedVariantIds) {
        public GenerationResult {
            failedVariantIds = List.copyOf(failedVariantIds);
        }
    }
}