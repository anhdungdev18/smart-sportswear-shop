package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record VisualSearchModelResponse(
        String id, String provider, String model, int dimensions, String status,
        @JsonAlias("target_image_count") int targetImageCount,
        @JsonAlias("ready_image_count") int readyImageCount,
        @JsonAlias("failed_image_count") int failedImageCount,
        @JsonAlias("activated_at") String activatedAt) {
    public record ListResponse(List<VisualSearchModelResponse> models) {}
}
