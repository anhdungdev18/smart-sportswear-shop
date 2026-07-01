package com.dunghaiquyen.ecommerce.modules.page.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.page.dto.PublicPageResponse;
import com.dunghaiquyen.ecommerce.modules.page.service.PageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public catalog read - see SecurityConfig.PUBLIC_GET_ENDPOINTS. */
@RestController
@RequestMapping("/api/v1/pages")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/{slug}")
    public ApiResponse<PublicPageResponse> bySlug(@PathVariable String slug) {
        return ApiResponse.ok(pageService.getPublishedBySlug(slug));
    }
}
