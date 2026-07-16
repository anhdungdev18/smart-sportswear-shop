package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import java.util.List;
import java.util.UUID;

/** No status/timestamps - the /active listing already filters to ACTIVE-and-within-window. */
public record PublicBannerResponse(UUID id, String name, BannerPlacement placement, List<BannerItemResponse> items) {
}
