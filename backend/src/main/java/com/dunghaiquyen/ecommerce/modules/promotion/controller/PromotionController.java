package com.dunghaiquyen.ecommerce.modules.promotion.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.ActivePromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.service.PromotionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public: active product promotions (used by the storefront flash-sale countdown). */
@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/active")
    public ApiResponse<List<ActivePromotionResponse>> active() {
        return ApiResponse.ok(promotionService.listActive());
    }
}
