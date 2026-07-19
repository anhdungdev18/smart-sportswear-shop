package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class ForecastGenerationServiceTest {

    @Test
    void isolatesVariantFailureAndReportsBatchResult() {
        DemandForecastService demandService = mock(DemandForecastService.class);
        UUID successful = UUID.randomUUID();
        UUID failed = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 7, 19);
        doThrow(new IllegalStateException("broken SKU"))
                .when(demandService).generateForecastAndRecommendation(failed, date.minusDays(180), date);

        var result = new ForecastGenerationService(demandService, 2)
                .generate(List.of(successful, failed), date.minusDays(180), date);

        assertThat(result.requested()).isEqualTo(2);
        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.failedVariantIds()).containsExactly(failed);
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
        }).when(demandService).generateForecastAndRecommendation(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        ForecastGenerationService service = new ForecastGenerationService(demandService, 1);

        Thread first = Thread.startVirtualThread(
                () -> service.generate(List.of(UUID.randomUUID()), date.minusDays(180), date));
        assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
        assertThatThrownBy(() -> service.generate(List.of(UUID.randomUUID()), date.minusDays(180), date))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already running");
        release.countDown();
        first.join();
    }
}