package com.dunghaiquyen.ecommerce.modules.setting.repository;

import com.dunghaiquyen.ecommerce.modules.setting.entity.SiteSetting;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, UUID> {

    Optional<SiteSetting> findBySettingKey(String settingKey);

    List<SiteSetting> findAllByIsPublicTrue();

    List<SiteSetting> findAllByOrderBySettingKeyAsc();
}
