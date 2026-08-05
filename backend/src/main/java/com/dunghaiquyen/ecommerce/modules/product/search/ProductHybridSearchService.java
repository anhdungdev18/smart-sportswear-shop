package com.dunghaiquyen.ecommerce.modules.product.search;

import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListQuery;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;

@Service
public class ProductHybridSearchService {

    public record Result(List<ProductListItemResponse> items, HybridSearchMeta meta) {}

    private final ProductSearchProperties properties;
    private final InternalProductSearchClient client;
    private final ProductService productService;

    public ProductHybridSearchService(
            ProductSearchProperties properties,
            InternalProductSearchClient client,
            ProductService productService) {
        this.properties = properties;
        this.client = client;
        this.productService = productService;
    }

    public Result search(ProductListQuery query) {
        long started = System.nanoTime();
        int page = query.page() == null ? 1 : Math.max(1, query.page());
        int limit = query.limit() == null ? 20 : Math.max(1, Math.min(100, query.limit()));
        String rawText = query.q() != null ? query.q()
                : query.keyword() == null ? "" : query.keyword();
        if (rawText.length() > 300 || rawText.chars().anyMatch(ch -> ch < 32 && ch != '\t')) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid search query");
        }
        String text = rawText.trim();
        if (!properties.enabled() || text.isBlank()) {
            return fallback(query, page, limit, properties.enabled() ? "EMPTY_QUERY" : "FEATURE_DISABLED", started);
        }
        try {
            InternalProductSearchClient.Response response =
                    client.search(text, page, limit, filters(query));
            if (response == null || response.items() == null) {
                return fallback(query, page, limit, "INVALID_UPSTREAM_RESPONSE", started);
            }
            var ids = response.items().stream().map(InternalProductSearchClient.Item::productId).toList();
            var items = productService.assembleRankedSearchItems(ids, query);
            long total = Math.max(items.size(), response.total());
            return new Result(items, new HybridSearchMeta(
                    page, limit, total, (int) Math.ceil((double) total / limit),
                    response.searchMode(), response.parsedQuery(), null, elapsed(started)));
        } catch (RuntimeException exception) {
            return fallback(query, page, limit, "UPSTREAM_UNAVAILABLE", started);
        }
    }

    private Result fallback(ProductListQuery query, int page, int limit, String reason, long started) {
        ProductService.ListResult result = productService.listPublic(query);
        return new Result(result.items(), new HybridSearchMeta(
                page, limit, result.meta().total(), result.meta().totalPages(),
                "KEYWORD_FALLBACK", JsonNodeFactory.instance.objectNode(), reason, elapsed(started)));
    }

    private Map<String, Object> filters(ProductListQuery q) {
        Map<String, Object> values = new HashMap<>();
        values.put("categoryId", q.categoryId());
        values.put("categorySlug", q.categorySlug());
        values.put("brandId", q.brandId());
        values.put("brandSlug", q.brandSlug());
        values.put("gender", q.gender() == null ? null : q.gender().name());
        values.put("sportType", q.sportType());
        values.put("productType", q.productType() == null ? null : q.productType().name());
        values.put("surface", q.surface());
        values.put("color", q.color());
        values.put("size", q.size());
        values.put("minPrice", q.minPrice());
        values.put("maxPrice", q.maxPrice());
        values.put("discount", q.discount());
        values.put("inStockOnly", true);
        return values;
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}
