package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.modules.replenishment.dto.ForecastGenerationStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.dto.ForecastGenerationStatus.Status;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ForecastGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ForecastGenerationService.class);

    private final DemandForecastService demandForecastService;
    private final int parallelism;

    private final AtomicReference<Status> currentStatus = new AtomicReference<>(Status.IDLE);
    private final AtomicInteger requestedCount = new AtomicInteger();
    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger succeededCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();
    private final CopyOnWriteArrayList<UUID> failedIds = new CopyOnWriteArrayList<>();
    private final AtomicReference<Instant> startedAt = new AtomicReference<>();

    public ForecastGenerationService(
            DemandForecastService demandForecastService,
            @Value("${app.forecast.generation-parallelism:4}") int parallelism) {
        this.demandForecastService = demandForecastService;
        this.parallelism = Math.max(1, Math.min(parallelism, 8));
    }

    public ForecastGenerationStatus getStatus() {
        long duration = startedAt.get() != null ? Duration.between(startedAt.get(), Instant.now()).toMillis() : 0;
        return new ForecastGenerationStatus(
            currentStatus.get(),
            requestedCount.get(),
            processedCount.get(),
            succeededCount.get(),
            failedCount.get(),
            duration,
            List.copyOf(failedIds)
        );
    }

    public void startSync() {
        if (!currentStatus.compareAndSet(Status.IDLE, Status.SYNCING)) {
            // If it is COMPLETED or FAILED, we can allow a new run
            if (!currentStatus.compareAndSet(Status.COMPLETED, Status.SYNCING) &&
                !currentStatus.compareAndSet(Status.FAILED, Status.SYNCING)) {
                throw new IllegalArgumentException("A batch is already running");
            }
        }
        startedAt.set(Instant.now());
        requestedCount.set(0);
        processedCount.set(0);
        succeededCount.set(0);
        failedCount.set(0);
        failedIds.clear();
    }

    public void startGenerationAsync(List<UUID> variantIds, LocalDate fromInclusive, LocalDate toInclusive) {
        if (!currentStatus.compareAndSet(Status.SYNCING, Status.FORECASTING)) {
            return;
        }

        List<UUID> distinctVariantIds = variantIds.stream().distinct().toList();
        requestedCount.set(distinctVariantIds.size());

        CompletableFuture.runAsync(() -> {
            long batchStart = System.currentTimeMillis();
            try (ExecutorService executor = Executors.newFixedThreadPool(parallelism)) {
                int chunkSize = 200;
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int i = 0; i < distinctVariantIds.size(); i += chunkSize) {
                    List<UUID> chunk = distinctVariantIds.subList(i, Math.min(i + chunkSize, distinctVariantIds.size()));
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            demandForecastService.generateForecastAndRecommendationBatch(chunk, fromInclusive, toInclusive);
                            succeededCount.addAndGet(chunk.size());
                            log.debug("Forecast generated for chunk of {} variants", chunk.size());
                        } catch (Exception exception) {
                            log.error("Forecast generation failed for a chunk of {} variants", chunk.size(), exception);
                            failedCount.addAndGet(chunk.size());
                            failedIds.addAll(chunk);
                        } finally {
                            processedCount.addAndGet(chunk.size());
                        }
                    }, executor);
                    futures.add(future);
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                
                long totalMillis = System.currentTimeMillis() - batchStart;
                currentStatus.set(Status.COMPLETED);
                log.info("Forecast batch completed in {}ms: requested={}, succeeded={}, failed={}", 
                         totalMillis, requestedCount.get(), succeededCount.get(), failedCount.get());
            } catch (Exception e) {
                currentStatus.set(Status.FAILED);
                log.error("Forecast batch orchestration failed", e);
            }
        });
    }

    public void startEvaluationAsync(List<UUID> variantIds, LocalDate fromInclusive, LocalDate toInclusive) {
        if (!currentStatus.compareAndSet(Status.SYNCING, Status.EVALUATING)) {
            return;
        }

        List<UUID> distinctVariantIds = variantIds.stream().distinct().toList();
        requestedCount.set(distinctVariantIds.size());

        CompletableFuture.runAsync(() -> {
            long batchStart = System.currentTimeMillis();
            try (ExecutorService executor = Executors.newFixedThreadPool(parallelism)) {
                int chunkSize = 200;
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int i = 0; i < distinctVariantIds.size(); i += chunkSize) {
                    List<UUID> chunk = distinctVariantIds.subList(i, Math.min(i + chunkSize, distinctVariantIds.size()));
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            demandForecastService.evaluateModelsBatch(chunk, fromInclusive, toInclusive);
                            succeededCount.addAndGet(chunk.size());
                            log.debug("Model evaluation completed for chunk of {} variants", chunk.size());
                        } catch (Exception exception) {
                            log.error("Model evaluation failed for a chunk of {} variants", chunk.size(), exception);
                            failedCount.addAndGet(chunk.size());
                            failedIds.addAll(chunk);
                        } finally {
                            processedCount.addAndGet(chunk.size());
                        }
                    }, executor);
                    futures.add(future);
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                
                long totalMillis = System.currentTimeMillis() - batchStart;
                currentStatus.set(Status.COMPLETED);
                log.info("Evaluation batch completed in {}ms: requested={}, succeeded={}, failed={}", 
                         totalMillis, requestedCount.get(), succeededCount.get(), failedCount.get());
            } catch (Exception e) {
                currentStatus.set(Status.FAILED);
                log.error("Evaluation batch orchestration failed", e);
            }
        });
    }

    public void failBatch() {
        currentStatus.set(Status.FAILED);
    }
}
