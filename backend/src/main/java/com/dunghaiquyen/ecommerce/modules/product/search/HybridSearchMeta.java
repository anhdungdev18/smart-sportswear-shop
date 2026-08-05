package com.dunghaiquyen.ecommerce.modules.product.search;

import com.fasterxml.jackson.databind.JsonNode;

public record HybridSearchMeta(
        int page,
        int size,
        long total,
        int totalPages,
        String searchMode,
        JsonNode parsedQuery,
        String fallbackReason,
        long processingTimeMs) {
}
