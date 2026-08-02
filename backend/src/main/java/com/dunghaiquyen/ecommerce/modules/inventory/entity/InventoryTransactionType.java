package com.dunghaiquyen.ecommerce.modules.inventory.entity;

public enum InventoryTransactionType {
    IMPORT,
    EXPORT,
    ADJUSTMENT_UP,
    ADJUSTMENT_DOWN,
    ORDER_RESERVE,
    ORDER_RELEASE,
    ORDER_CONFIRM_DEDUCT,
    RETURN_RESTOCK
}
