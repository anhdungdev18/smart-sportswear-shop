package com.dunghaiquyen.ecommerce.modules.page.dto;

import java.time.Instant;

/** No status/createdBy/timestamps - this is only ever returned for an already-PUBLISHED page. */
public record PublicPageResponse(String title, String slug, String summary, String contentHtml, Instant publishedAt) {
}
