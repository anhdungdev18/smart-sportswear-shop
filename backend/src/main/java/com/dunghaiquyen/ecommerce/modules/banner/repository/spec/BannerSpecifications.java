package com.dunghaiquyen.ecommerce.modules.banner.repository.spec;

import com.dunghaiquyen.ecommerce.modules.banner.entity.Banner;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import org.springframework.data.jpa.domain.Specification;

public final class BannerSpecifications {

    private BannerSpecifications() {
    }

    public static Specification<Banner> hasStatus(BannerStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Banner> hasPlacement(BannerPlacement placement) {
        return (root, query, cb) -> cb.equal(root.get("placement"), placement);
    }
}
