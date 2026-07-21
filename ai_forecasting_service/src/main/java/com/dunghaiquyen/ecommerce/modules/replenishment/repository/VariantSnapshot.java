package com.dunghaiquyen.ecommerce.modules.replenishment.repository;
import java.util.UUID;
public record VariantSnapshot(UUID id, UUID productId, String sku, String productName, String size, String color, int stockQuantity, int reservedQuantity) {
    public int availableQuantity() { return stockQuantity - reservedQuantity; }
}