package com.dunghaiquyen.ecommerce.modules.product.dto;

import java.util.UUID;

/** Deliberately minimal (Phase N3 autocomplete) - just enough for a suggestion dropdown, not a full list item. */
public record ProductSuggestionResponse(UUID id, String name, String slug, String thumbnail) {
}
