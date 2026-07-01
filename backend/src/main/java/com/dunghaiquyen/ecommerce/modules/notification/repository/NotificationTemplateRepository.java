package com.dunghaiquyen.ecommerce.modules.notification.repository;

import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationTemplate;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByType(NotificationType type);

    boolean existsByType(NotificationType type);
}
