package com.dunghaiquyen.ecommerce.modules.product.entity;

/**
 * Top-level product classification - see V10 migration and SPORTSWEAR_CATALOG_MODEL_V2.md
 * section 3.1. Nullable on existing products (backward-compatible V10 migration),
 * but enforced by a DB check constraint when a value is supplied.
 *
 * <p>EQUIPMENT is included per the spec ("dùng để dành mở rộng") to allow
 * balls, water bottles, training mats, etc. without requiring another migration.
 */
public enum ProductType {
    APPAREL,
    FOOTWEAR,
    ACCESSORY,
    EQUIPMENT
}
