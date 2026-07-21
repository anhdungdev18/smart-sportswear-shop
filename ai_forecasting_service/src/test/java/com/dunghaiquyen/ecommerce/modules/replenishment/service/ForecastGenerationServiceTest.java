package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.dunghaiquyen.ecommerce.modules.replenishment.dto.ForecastGenerationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class ForecastGenerationServiceTest {

    @Test
    void isolatesVariantFailureAndReportsBatchResult() throws InterruptedException {
        DemandForecastService demandService = mock(DemandForecastService.class);
        UUID successful = UUID.randomUUID();
        UUID failed = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 7, 19);
        
        List<UUID> batch = List.of(successful, failed);
        doThrow(new IllegalStateException("broken batch"))
                .when(demandService).generateForecastAndRecommendationBatch(ArgumentMatchers.anyList(), ArgumentMatchers.any(), ArgumentMatchers.any());

        var service = new ForecastGenerationService(demandService, 2);
        service.startSync();
        service.startGenerationAsync(batch, date.minusDays(180), date);

        while (service.getStatus().status() != ForecastGenerationStatus.Status.COMPLETED && 
               service.getStatus().status() != ForecastGenerationStatus.Status.FAILED) {
            Thread.sleep(10);
        }

        var result = service.getStatus();
        assertThat(result.requested()).isEqualTo(2);
        // Because the whole chunk fails, succeeded is 0, failed is 2
        assertThat(result.succeeded()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(2);
        assertThat(result.failedVariantIds()).contains(successful, failed);
    }

    @Test
    void rejectsOverlappingBatches() throws Exception {
        DemandForecastService demandService = mock(DemandForecastService.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        LocalDate date = LocalDate.of(2026, 7, 19);
        doAnswer(invocation -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return null;
        }).when(demandService).generateForecastAndRecommendationBatch(
                ArgumentMatchers.anyList(), ArgumentMatchers.any(), ArgumentMatchers.any());
        ForecastGenerationService service = new ForecastGenerationService(demandService, 1);

        service.startSync();
        service.startGenerationAsync(List.of(UUID.randomUUID()), date.minusDays(180), date);
        
        assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
        assertThatThrownBy(() -> service.startSync())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already running");
        release.countDown();
        
        while (service.getStatus().status() != ForecastGenerationStatus.Status.COMPLETED && 
               service.getStatus().status() != ForecastGenerationStatus.Status.FAILED) {
            Thread.sleep(10);
        }
    }
}