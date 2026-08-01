package com.dunghaiquyen.ecommerce.modules.demand.dto;

public record DemandClassificationBatchResponse(
        int requested,
        int classified,
        String algorithmVersion) {
}
