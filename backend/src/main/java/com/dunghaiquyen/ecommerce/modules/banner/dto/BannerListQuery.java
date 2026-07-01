package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;

public record BannerListQuery(Integer page, Integer limit, BannerStatus status, BannerPlacement placement) {
}
