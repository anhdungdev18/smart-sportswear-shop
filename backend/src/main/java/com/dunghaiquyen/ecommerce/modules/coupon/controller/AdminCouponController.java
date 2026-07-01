package com.dunghaiquyen.ecommerce.modules.coupon.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponCreateRequest;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponListQuery;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponResponse;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.coupon.service.CouponService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public ApiResponse<List<CouponResponse>> list(@ModelAttribute CouponListQuery query) {
        CouponService.ListResult result = couponService.list(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> create(@Valid @RequestBody CouponCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Coupon created", couponService.create(request)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<CouponResponse> update(@PathVariable UUID id, @Valid @RequestBody CouponUpdateRequest request) {
        return ApiResponse.ok("Coupon updated", couponService.update(id, request));
    }
}
