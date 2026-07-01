package com.dunghaiquyen.ecommerce.modules.collection.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddProductToCollectionRequest(@NotNull(message = "collectionId is required") UUID collectionId,
        Integer sortOrder,
        Boolean isPrimary) {
}
