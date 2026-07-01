package com.dunghaiquyen.ecommerce.modules.notification.repository.spec;

import com.dunghaiquyen.ecommerce.modules.notification.entity.Notification;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationChannel;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationStatus;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    public static Specification<Notification> hasType(NotificationType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Notification> hasStatus(NotificationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Notification> hasChannel(NotificationChannel channel) {
        return (root, query, cb) -> cb.equal(root.get("channel"), channel);
    }

    public static Specification<Notification> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Notification> belongsToOrder(UUID orderId) {
        return (root, query, cb) -> cb.equal(root.get("order").get("id"), orderId);
    }

    public static Specification<Notification> createdFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Notification> createdTo(Instant to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
