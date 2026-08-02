package com.dunghaiquyen.ecommerce.visualsearch.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class VisualSearchServiceTest {

    @Test
    void fetchesWideCandidatePoolBeforeApplyingCommerceFilters() {
        VisualSearchClient client = mock(VisualSearchClient.class);
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(client.search(image, VisualSearchService.CANDIDATE_LIMIT)).thenReturn(List.of());
        VisualSearchService service = new VisualSearchService(
                new VisualSearchProperties(true, "http://localhost", "token", 5, 10),
                client,
                mock(ProductService.class),
                mock(ProductRepository.class));

        assertThat(service.search(image, 20, null, null, null, null)).isEmpty();
        verify(client).search(image, 50);
    }
}
