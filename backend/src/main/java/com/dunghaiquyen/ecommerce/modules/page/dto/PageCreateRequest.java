package com.dunghaiquyen.ecommerce.modules.page.dto;

import com.dunghaiquyen.ecommerce.modules.page.entity.PageStatus;
import jakarta.validation.constraints.NotBlank;

public record PageCreateRequest(
        @NotBlank String title, @NotBlank String slug, String summary, @NotBlank String contentHtml, PageStatus status) {
}
