package com.dunghaiquyen.ecommerce.visualsearch.api;

import jakarta.validation.constraints.AssertTrue;
import java.util.UUID;

public record VisualSearchReindexRequest(UUID imageId, UUID productId) {
    @AssertTrue(message = "Provide exactly one imageId or productId")
    public boolean hasExactlyOneTarget() {
        return (imageId == null) != (productId == null);
    }
}
