package com.dunghaiquyen.ecommerce.modules.banner.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.banner.dto.PublicBannerResponse;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.service.BannerService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public catalog read - see SecurityConfig.PUBLIC_GET_ENDPOINTS. */
@RestController
@RequestMapping("/api/v1/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/active")
    public ApiResponse<List<PublicBannerResponse>> active(@RequestParam BannerPlacement placement) {
        return ApiResponse.ok(bannerService.listActive(placement));
    }
}
