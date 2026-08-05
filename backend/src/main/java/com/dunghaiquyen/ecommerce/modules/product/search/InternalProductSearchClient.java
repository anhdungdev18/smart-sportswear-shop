package com.dunghaiquyen.ecommerce.modules.product.search;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Component
public class InternalProductSearchClient {

    public record Item(UUID productId, Integer keywordRank, Integer semanticRank,
            Double semanticScore, double fusionScore, List<String> matchedReasons) {}
    public record Response(List<Item> items, long total, JsonNode parsedQuery,
            String searchMode, long processingTimeMs) {}

    private final ProductSearchProperties properties;
    private final RestClient client;

    public InternalProductSearchClient(ProductSearchProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
        this.client = builder
                .baseUrl(properties.serviceUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public Response search(String query, int page, int limit, Map<String, Object> filters) {
        return client.post()
                .uri("/internal/v1/product-search")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", properties.internalToken())
                .body(Map.of("query", query, "page", page, "limit", limit, "filters", filters))
                .retrieve()
                .body(Response.class);
    }
}
