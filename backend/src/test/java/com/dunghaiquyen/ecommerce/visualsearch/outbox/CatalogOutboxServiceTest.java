package com.dunghaiquyen.ecommerce.visualsearch.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

class CatalogOutboxServiceTest {

    private final JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CatalogOutboxService service = new CatalogOutboxService(jdbcTemplate, objectMapper);

    @Test
    void imageEventMatchesVersionOneContract() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        when(jdbcTemplate.update(any(String.class), any(Object[].class))).thenReturn(1);

        UUID eventId = service.append(CatalogEventType.PRODUCT_IMAGE_CREATED, productId, imageId);

        verify(jdbcTemplate).update(any(String.class), args.capture());
        Object[] values = args.getValue();
        assertThat(values[0]).isEqualTo(eventId);
        assertThat(values[1]).isEqualTo("PRODUCT_IMAGE_CREATED");
        assertThat(values[2]).isEqualTo(1);
        assertThat(values[3]).isEqualTo(productId);
        JsonNode payload = objectMapper.readTree((String) values[4]);
        assertThat(payload.path("eventId").asText()).isEqualTo(eventId.toString());
        assertThat(payload.path("eventVersion").asInt()).isEqualTo(1);
        assertThat(payload.path("productId").asText()).isEqualTo(productId.toString());
        assertThat(payload.path("imageId").asText()).isEqualTo(imageId.toString());
        assertThat(payload.path("traceId").asText()).isNotBlank();
        assertThat(payload.path("occurredAt").asText()).isNotBlank();
    }

    @Test
    void productEventOmitsImageId() throws Exception {
        UUID productId = UUID.randomUUID();
        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        when(jdbcTemplate.update(any(String.class), any(Object[].class))).thenReturn(1);

        service.append(CatalogEventType.PRODUCT_ACTIVATED, productId, null);

        verify(jdbcTemplate).update(any(String.class), args.capture());
        JsonNode payload = objectMapper.readTree((String) args.getValue()[4]);
        assertThat(payload.path("eventType").asText()).isEqualTo("PRODUCT_ACTIVATED");
        assertThat(payload.has("imageId")).isFalse();
    }
}
