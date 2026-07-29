package com.dunghaiquyen.ecommerce.modules.demand.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dunghaiquyen.ecommerce.config.AiDemandClassificationProperties;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandConfidence;
import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandPattern;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository.DemandHistory;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository.DemandPoint;
import com.dunghaiquyen.ecommerce.modules.demand.repository.DemandClassificationRepository.VariantKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DemandClassificationServiceTest {

    private final DemandClassificationRepository repository = mock(DemandClassificationRepository.class);
    private final DemandClassificationService service = new DemandClassificationService(
            repository, new AiDemandClassificationProperties(60, 3, 30, 1.32, 0.49, 0.03, "test-v1"));

    @Test
    void classifiesSmoothDemand() {
        var result = service.classify(history(repeated(120, 2)), from(), from().plusDays(119));

        assertThat(result.classification()).isEqualTo(DemandPattern.SMOOTH);
        assertThat(result.confidence()).isEqualTo(DemandConfidence.HIGH);
        assertThat(result.adi()).isEqualTo(1.0d);
        assertThat(result.reason()).contains("regular");
    }

    @Test
    void classifiesIntermittentDemand() {
        var quantities = repeated(90, 0);
        for (int i = 0; i < quantities.size(); i += 3) {
            quantities.set(i, 2L);
        }

        var result = service.classify(history(quantities), from(), from().plusDays(89));

        assertThat(result.classification()).isEqualTo(DemandPattern.INTERMITTENT);
        assertThat(result.adi()).isEqualTo(3.0d);
        assertThat(result.reason()).contains("ADI");
    }

    @Test
    void classifiesErraticDemand() {
        var quantities = new ArrayList<Long>();
        for (int i = 0; i < 80; i++) {
            quantities.add(i % 2 == 0 ? 1L : 12L);
        }

        var result = service.classify(history(quantities), from(), from().plusDays(79));

        assertThat(result.classification()).isEqualTo(DemandPattern.ERRATIC);
        assertThat(result.cvSquared()).isGreaterThanOrEqualTo(0.49d);
    }

    @Test
    void classifiesGrowingAndDecliningDemand() {
        var growing = service.classify(history(ramp(80, true)), from(), from().plusDays(79));
        var declining = service.classify(history(ramp(80, false)), from(), from().plusDays(79));

        assertThat(growing.classification()).isEqualTo(DemandPattern.GROWING);
        assertThat(declining.classification()).isEqualTo(DemandPattern.DECLINING);
    }

    @Test
    void classifiesNoDemandNewItemAndInsufficientData() {
        var noDemand = service.classify(history(repeated(90, 0)), from(), from().plusDays(89));
        var newItem = service.classify(history(repeated(20, 2)), from(), from().plusDays(89));
        var insufficient = service.classify(history(repeated(40, 0)), from(), from().plusDays(89));

        assertThat(noDemand.classification()).isEqualTo(DemandPattern.NO_DEMAND);
        assertThat(newItem.classification()).isEqualTo(DemandPattern.NEW_ITEM);
        assertThat(insufficient.classification()).isEqualTo(DemandPattern.INSUFFICIENT_DATA);
    }

    @Test
    void batchClassifiesAndPersistsAllRows() {
        LocalDate from = from();
        LocalDate to = from.plusDays(89);
        when(repository.findHistories(from, to, "DEMO")).thenReturn(List.of(history(repeated(90, 2))));

        var response = service.classifyBatch(from, to, "DEMO");

        assertThat(response.requested()).isEqualTo(1);
        assertThat(response.classified()).isEqualTo(1);
        assertThat(response.algorithmVersion()).isEqualTo("test-v1");
        verify(repository).upsertAll(any());
        verify(repository).findHistories(eq(from), eq(to), eq("DEMO"));
    }

    private DemandHistory history(List<Long> quantities) {
        LocalDate start = from();
        List<DemandPoint> points = new ArrayList<>();
        for (int i = 0; i < quantities.size(); i++) {
            points.add(new DemandPoint(start.plusDays(i), quantities.get(i)));
        }
        return new DemandHistory(new VariantKey(UUID.randomUUID(), "SKU-1", "Product", "DEMO", quantities.size()), points, quantities.size());
    }

    private List<Long> repeated(int days, long quantity) {
        return new ArrayList<>(java.util.Collections.nCopies(days, quantity));
    }

    private List<Long> ramp(int days, boolean ascending) {
        List<Long> quantities = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            long value = ascending ? i + 1L : days - i;
            quantities.add(value);
        }
        return quantities;
    }

    private LocalDate from() {
        return LocalDate.of(2026, 1, 1);
    }
}
