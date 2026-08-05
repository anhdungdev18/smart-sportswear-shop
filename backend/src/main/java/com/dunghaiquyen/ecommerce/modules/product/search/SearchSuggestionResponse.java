package com.dunghaiquyen.ecommerce.modules.product.search;

import java.math.BigDecimal;

public record SearchSuggestionResponse(
        String type,
        String label,
        String id,
        String value,
        String slug,
        String thumbnail,
        BigDecimal minPrice,
        String subtitle) {
}
