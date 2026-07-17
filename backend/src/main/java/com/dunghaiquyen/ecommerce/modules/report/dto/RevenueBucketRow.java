package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Spring Data interface projection for the grouped revenue query. Alias names in
 * the native SQL ("bucket", "revenue", "orderCount") must match these getters.
 */
public interface RevenueBucketRow {

    LocalDate getBucket();

    BigDecimal getRevenue();

    long getOrderCount();
}
