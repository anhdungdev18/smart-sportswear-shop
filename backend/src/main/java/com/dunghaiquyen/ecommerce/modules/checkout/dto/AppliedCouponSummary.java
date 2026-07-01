package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.math.BigDecimal;

public record AppliedCouponSummary(String code, BigDecimal discountAmount) {
}
