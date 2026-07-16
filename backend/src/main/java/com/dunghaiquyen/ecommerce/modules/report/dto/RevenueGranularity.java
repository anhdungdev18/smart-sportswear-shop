package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.util.Locale;

/**
 * Bucket size for the revenue time-series report. Maps 1:1 to the Postgres
 * {@code date_trunc} field ("day"/"month"/"year"). Unknown/blank input falls
 * back to {@link #MONTH} so a bad query param never 400s the dashboard.
 */
public enum RevenueGranularity {
    DAY,
    MONTH,
    YEAR;

    public static RevenueGranularity from(String raw) {
        if (raw == null || raw.isBlank()) {
            return MONTH;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return MONTH;
        }
    }

    /** The {@code date_trunc} field keyword for this granularity. */
    public String sqlField() {
        return name().toLowerCase(Locale.ROOT);
    }
}
