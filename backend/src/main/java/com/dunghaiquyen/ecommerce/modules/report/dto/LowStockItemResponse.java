package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.util.UUID;

public record LowStockItemResponse(
        UUID variantId, String sku, String productName, int stockQuantity, int reservedQuantity, int availableQuantity) {
}
