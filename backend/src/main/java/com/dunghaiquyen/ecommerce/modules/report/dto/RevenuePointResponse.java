package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One bucket of the revenue time series. {@code label} is a pre-formatted,
 * display-ready axis tick (e.g. "07/2026"); {@code date} is the machine-usable
 * bucket start. {@code revenue} is gross revenue (PAID orders) in VND.
 */
public record RevenuePointResponse(String label, LocalDate date, BigDecimal revenue, long orders) {
}
