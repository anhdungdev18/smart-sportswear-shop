package com.dunghaiquyen.ecommerce.modules.coupon.dto;

import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponStatus;

public record CouponListQuery(Integer page, Integer limit, CouponStatus status) {
}
