package com.dunghaiquyen.ecommerce.modules.banner.repository;

import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerItemRepository extends JpaRepository<BannerItem, UUID> {

    List<BannerItem> findAllByBannerIdOrderBySortOrderAsc(UUID bannerId);

    List<BannerItem> findAllByBannerIdAndIsActiveTrueOrderBySortOrderAsc(UUID bannerId);
}
