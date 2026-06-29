package com.dunghaiquyen.ecommerce.common.response;

import org.springframework.data.domain.Page;

/** Field names match API_SPEC_PHASE1.md 2.6 exactly: page, limit, total, totalPages. */
public record PageMeta(int page, int limit, long total, int totalPages) {

    /** {@code page} is 1-indexed here to match the API contract; Spring's Page is 0-indexed internally. */
    public static PageMeta from(Page<?> page) {
        return new PageMeta(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
