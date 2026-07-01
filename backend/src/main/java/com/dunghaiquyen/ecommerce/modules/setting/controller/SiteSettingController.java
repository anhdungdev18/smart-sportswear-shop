package com.dunghaiquyen.ecommerce.modules.setting.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.setting.dto.PublicSiteSettingResponse;
import com.dunghaiquyen.ecommerce.modules.setting.service.SiteSettingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public catalog read - see SecurityConfig.PUBLIC_GET_ENDPOINTS. Only isPublic=true settings are ever exposed here. */
@RestController
@RequestMapping("/api/v1/settings")
public class SiteSettingController {

    private final SiteSettingService siteSettingService;

    public SiteSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping("/public")
    public ApiResponse<List<PublicSiteSettingResponse>> publicSettings() {
        return ApiResponse.ok(siteSettingService.listPublic());
    }
}
