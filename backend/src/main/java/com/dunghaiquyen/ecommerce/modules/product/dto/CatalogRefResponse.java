package com.dunghaiquyen.ecommerce.modules.product.dto;

import java.util.UUID;

/** Small embedded reference, per API_SPEC_PHASE1.md 5.3/5.4: {"id", "name"} only. */
public record CatalogRefResponse(UUID id, String name) {
}
