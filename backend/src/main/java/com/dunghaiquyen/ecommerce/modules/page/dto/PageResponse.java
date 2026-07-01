package com.dunghaiquyen.ecommerce.modules.page.dto;

import com.dunghaiquyen.ecommerce.modules.page.entity.PageStatus;
import java.time.Instant;
import java.util.UUID;

public record PageResponse(
        UUID id,
        String title,
        String slug,
        String summary,
        String contentHtml,
        PageStatus status,
        Instant publishedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {
}
