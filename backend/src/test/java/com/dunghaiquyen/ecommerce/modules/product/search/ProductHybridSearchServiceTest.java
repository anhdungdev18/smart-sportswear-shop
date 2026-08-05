package com.dunghaiquyen.ecommerce.modules.product.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListQuery;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductHybridSearchServiceTest {

    @Mock InternalProductSearchClient client;
    @Mock ProductService productService;

    @Test
    void successfulInternalSearch_preservesRankAndMetadata() {
        ProductSearchProperties properties =
                new ProductSearchProperties(true, "http://search", "token", 2, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        var parsed = JsonNodeFactory.instance.objectNode().put("brand", "Nike");
        when(client.search(anyString(), anyInt(), anyInt(), anyMap())).thenReturn(
                new InternalProductSearchClient.Response(
                        List.of(
                                new InternalProductSearchClient.Item(first, 1, 2, .81, .03, List.of("KEYWORD")),
                                new InternalProductSearchClient.Item(second, 2, 1, .84, .02, List.of("SEMANTIC"))),
                        2, parsed, "HYBRID", 80));
        when(productService.assembleRankedSearchItems(List.of(first, second), query("Nike")))
                .thenReturn(List.of());

        var result = service.search(query("Nike"));

        assertThat(result.meta().searchMode()).isEqualTo("HYBRID");
        assertThat(result.meta().parsedQuery()).isEqualTo(parsed);
        assertThat(result.meta().fallbackReason()).isNull();
        verify(productService).assembleRankedSearchItems(List.of(first, second), query("Nike"));
        verify(productService, never()).listPublic(any());
    }

    @Test
    void upstreamFailure_returnsKeywordFallbackWithoutLeakingError() {
        ProductSearchProperties properties =
                new ProductSearchProperties(true, "http://search", "token", 2, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);
        when(client.search(anyString(), anyInt(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("secret upstream detail"));
        when(productService.listPublic(any()))
                .thenReturn(new ProductService.ListResult(List.of(), new PageMeta(1, 20, 0, 0)));

        var result = service.search(query("Nike"));

        assertThat(result.meta().searchMode()).isEqualTo("KEYWORD_FALLBACK");
        assertThat(result.meta().fallbackReason()).isEqualTo("UPSTREAM_UNAVAILABLE");
        assertThat(result.meta().fallbackReason()).doesNotContain("secret");
    }

    @Test
    void upstreamTimeout_returnsSpecificFallbackReason() {
        ProductSearchProperties properties =
                new ProductSearchProperties(true, "http://search", "token", 8, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);
        when(client.search(anyString(), anyInt(), anyInt(), anyMap()))
                .thenThrow(new ResourceAccessException("Read timed out"));
        when(productService.listPublic(any()))
                .thenReturn(new ProductService.ListResult(List.of(), new PageMeta(1, 20, 0, 0)));

        var result = service.search(query("Nike"));

        assertThat(result.meta().fallbackReason()).isEqualTo("UPSTREAM_TIMEOUT");
    }

    @Test
    void nestedSocketTimeout_returnsSpecificFallbackReason() {
        ProductSearchProperties properties =
                new ProductSearchProperties(true, "http://search", "token", 8, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);
        when(client.search(anyString(), anyInt(), anyInt(), anyMap()))
                .thenThrow(new ResourceAccessException(
                        "I/O error calling product search", new SocketTimeoutException("Read timed out")));
        when(productService.listPublic(any()))
                .thenReturn(new ProductService.ListResult(List.of(), new PageMeta(1, 20, 0, 0)));

        var result = service.search(query("Nike"));

        assertThat(result.meta().fallbackReason()).isEqualTo("UPSTREAM_TIMEOUT");
    }

    @Test
    void upstreamHttpFailure_exposesOnlyStatusCode() {
        ProductSearchProperties properties =
                new ProductSearchProperties(true, "http://search", "token", 8, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);
        var upstreamFailure = new HttpClientErrorException(
                HttpStatus.UNAUTHORIZED, "secret upstream detail");
        when(client.search(anyString(), anyInt(), anyInt(), anyMap())).thenThrow(upstreamFailure);
        when(productService.listPublic(any()))
                .thenReturn(new ProductService.ListResult(List.of(), new PageMeta(1, 20, 0, 0)));

        var result = service.search(query("Nike"));

        assertThat(result.meta().fallbackReason()).isEqualTo("UPSTREAM_HTTP_401");
        assertThat(result.meta().fallbackReason()).doesNotContain("secret");
    }

    @Test
    void disabledFeature_skipsInternalService() {
        ProductSearchProperties properties =
                new ProductSearchProperties(false, "http://search", "token", 2, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);
        when(productService.listPublic(any()))
                .thenReturn(new ProductService.ListResult(List.of(), new PageMeta(1, 20, 0, 0)));

        var result = service.search(query("Nike"));

        assertThat(result.meta().fallbackReason()).isEqualTo("FEATURE_DISABLED");
        verify(client, never()).search(anyString(), anyInt(), anyInt(), anyMap());
    }

    @Test
    void controlCharactersAreRejectedBeforeAnyUpstreamCall() {
        ProductSearchProperties properties =
                new ProductSearchProperties(true, "http://search", "token", 2, 60);
        ProductHybridSearchService service =
                new ProductHybridSearchService(properties, client, productService);

        assertThatThrownBy(() -> service.search(query("Nike\u0000")))
                .isInstanceOf(BusinessRuleException.class);
        verify(client, never()).search(anyString(), anyInt(), anyInt(), anyMap());
    }

    private static ProductListQuery query(String text) {
        return new ProductListQuery(
                1, 20, null, text, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}
