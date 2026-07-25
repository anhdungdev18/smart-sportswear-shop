package com.dunghaiquyen.ecommerce.modules.dataquality.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dunghaiquyen.ecommerce.config.AiDataQualityProperties;
import com.dunghaiquyen.ecommerce.modules.dataquality.dto.SkuDataQualityLevel;
import com.dunghaiquyen.ecommerce.modules.dataquality.repository.SkuDataQualityRepository;
import com.dunghaiquyen.ecommerce.modules.dataquality.repository.SkuDataQualityRow;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SkuDataQualityServiceTest {

    private final SkuDataQualityRepository repository = mock(SkuDataQualityRepository.class);
    private final SkuDataQualityService service = new SkuDataQualityService(
            repository, new AiDataQualityProperties(60, 120, 12, 30));

    @Test
    void marksMissingSalesDaysAsInsufficient() {
        UUID variantId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 29);
        when(repository.findQualityRow(variantId, from, to)).thenReturn(Optional.of(new SkuDataQualityRow(
                variantId, "SKU-1", "Product", 179, 35, 120, to.minusDays(1), 180, "Supplier")));

        var result = service.getVariant(variantId, from, to);

        assertThat(result.historyDays()).isEqualTo(180);
        assertThat(result.missingDays()).isEqualTo(1);
        assertThat(result.qualityLevel()).isEqualTo(SkuDataQualityLevel.INSUFFICIENT);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("missing days"));
    }

    @Test
    void marksCompleteHighQualitySeries() {
        UUID variantId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 29);
        when(repository.findQualityRow(variantId, from, to)).thenReturn(Optional.of(new SkuDataQualityRow(
                variantId, "SKU-2", "Product", 180, 40, 300, to, 180, "Supplier")));

        var result = service.getVariant(variantId, from, to);

        assertThat(result.missingDays()).isZero();
        assertThat(result.supplierConfigured()).isTrue();
        assertThat(result.qualityScore()).isEqualTo(100);
        assertThat(result.qualityLevel()).isEqualTo(SkuDataQualityLevel.HIGH);
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void summarizesSupplierAndInventoryWarnings() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 29);
        when(repository.findQualityRows(from, to)).thenReturn(List.of(
                new SkuDataQualityRow(UUID.randomUUID(), "SKU-1", "Product", 180, 40, 300, to, 180, "Supplier"),
                new SkuDataQualityRow(UUID.randomUUID(), "SKU-2", "Product", 180, 20, 60, to.minusDays(10), 20, null)));

        var summary = service.summarize(from, to);

        assertThat(summary.totalVariants()).isEqualTo(2);
        assertThat(summary.highQualityVariants()).isEqualTo(1);
        assertThat(summary.variantsMissingSupplier()).isEqualTo(1);
        assertThat(summary.variantsWithInventoryGaps()).isEqualTo(1);
    }
}
