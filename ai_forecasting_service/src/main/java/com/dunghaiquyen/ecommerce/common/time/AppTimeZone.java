package com.dunghaiquyen.ecommerce.common.time;

import java.time.ZoneId;

/**
 * Single shared timezone every LocalDate-based query filter (admin order/
 * notification/inventory date-range filters, report date-range filters)
 * must convert against - bug fix: these previously each hardcoded
 * {@code ZoneOffset.UTC}, while every caller computes its LocalDate
 * (e.g. {@code LocalDate.now()} for "today") in the server/client's actual
 * local timezone (Asia/Ho_Chi_Minh, UTC+7 - this shop's only operating
 * timezone, no DST). For roughly 7 hours every day (local midnight to UTC
 * midnight), "today" in UTC and "today" in Asia/Ho_Chi_Minh are different
 * calendar dates, so a filter built as "from start of today to end of
 * today" using UTC would silently exclude records created just hours ago.
 * Fixed by anchoring every such conversion to this single zone instead.
 */
public final class AppTimeZone {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private AppTimeZone() {
    }
}
