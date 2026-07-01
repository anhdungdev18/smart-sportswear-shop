package com.dunghaiquyen.ecommerce.modules.page.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.modules.page.entity.PageStatus;

public record PageUpdateRequest(
        @NullOrNotBlank String title,
        @NullOrNotBlank String slug,
        String summary,
        @NullOrNotBlank String contentHtml,
        PageStatus status) {
}
