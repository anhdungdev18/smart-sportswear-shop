package com.dunghaiquyen.ecommerce.modules.promotion.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PublicPromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.service.PromotionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public catalog read - see SecurityConfig.PUBLIC_GET_ENDPOINTS. Admin CRUD stays under /api/v1/admin/promotions. */
@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/active")
    public ApiResponse<List<PublicPromotionResponse>> active() {
        return ApiResponse.ok(promotionService.listActive());
    }
}
