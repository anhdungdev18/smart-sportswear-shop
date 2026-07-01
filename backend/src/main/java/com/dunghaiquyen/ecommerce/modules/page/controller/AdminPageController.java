package com.dunghaiquyen.ecommerce.modules.page.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageCreateRequest;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageListQuery;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageResponse;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.page.service.PageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/pages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageController {

    private final PageService pageService;

    public AdminPageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public ApiResponse<List<PageResponse>> list(@ModelAttribute PageListQuery query) {
        PageService.ListResult result = pageService.list(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @GetMapping("/{id}")
    public ApiResponse<PageResponse> detail(@PathVariable UUID id) {
        return ApiResponse.ok(pageService.getDetail(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody PageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Page created", pageService.create(request, principal.getUser())));
    }

    @PatchMapping("/{id}")
    public ApiResponse<PageResponse> update(@PathVariable UUID id, @Valid @RequestBody PageUpdateRequest request) {
        return ApiResponse.ok("Page updated", pageService.update(id, request));
    }
}
