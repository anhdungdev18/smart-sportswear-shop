package com.dunghaiquyen.ecommerce.modules.banner.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerCreateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerItemCreateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerItemResponse;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerItemUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerListQuery;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerResponse;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.service.BannerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ApiResponse<List<BannerResponse>> list(@ModelAttribute BannerListQuery query) {
        BannerService.ListResult result = bannerService.list(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @GetMapping("/{id}")
    public ApiResponse<BannerResponse> detail(@PathVariable UUID id) {
        return ApiResponse.ok(bannerService.getDetail(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BannerResponse>> create(@Valid @RequestBody BannerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Banner created", bannerService.create(request)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<BannerResponse> update(@PathVariable UUID id, @Valid @RequestBody BannerUpdateRequest request) {
        return ApiResponse.ok("Banner updated", bannerService.update(id, request));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<BannerItemResponse>> addItem(
            @PathVariable UUID id, @Valid @RequestBody BannerItemCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Banner item added", bannerService.addItem(id, request)));
    }

    @PatchMapping("/items/{itemId}")
    public ApiResponse<BannerItemResponse> updateItem(
            @PathVariable UUID itemId, @Valid @RequestBody BannerItemUpdateRequest request) {
        return ApiResponse.ok("Banner item updated", bannerService.updateItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> deleteItem(@PathVariable UUID itemId) {
        bannerService.deleteItem(itemId);
        return ApiResponse.ok("Banner item deleted", null);
    }
}
