package com.dunghaiquyen.ecommerce.modules.setting.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.setting.dto.SiteSettingResponse;
import com.dunghaiquyen.ecommerce.modules.setting.dto.UpsertSiteSettingRequest;
import com.dunghaiquyen.ecommerce.modules.setting.service.SiteSettingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSiteSettingController {

    private final SiteSettingService siteSettingService;

    public AdminSiteSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    public ApiResponse<List<SiteSettingResponse>> list() {
        return ApiResponse.ok(siteSettingService.list());
    }

    /** Create-or-update by key - see SiteSettingService's class javadoc. */
    @PutMapping("/{key}")
    public ApiResponse<SiteSettingResponse> upsert(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String key,
            @Valid @RequestBody UpsertSiteSettingRequest request) {
        return ApiResponse.ok("Setting saved", siteSettingService.upsert(key, request, principal.getUser()));
    }
}
