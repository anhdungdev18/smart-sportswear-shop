package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record VisualSearchCandidate(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("image_id") UUID imageId,
        @JsonProperty("matched_image_url") String matchedImageUrl,
        double similarity) {
}
