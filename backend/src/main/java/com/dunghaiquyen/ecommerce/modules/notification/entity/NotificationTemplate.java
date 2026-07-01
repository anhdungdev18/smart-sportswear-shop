package com.dunghaiquyen.ecommerce.modules.notification.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Exactly one row per {@link NotificationType} (uq_notification_templates_type)
 * - admin's editable copy of that type's email, with {placeholder} tokens
 * substituted at send time by NotificationTemplates. Seeded at startup
 * (NotificationTemplateSeeder) from the same default copy that used to live
 * only in code, so GET always returns a real, editable row - never a
 * synthesized/virtual one.
 */
@Getter
@Setter
@Entity
@Table(name = "notification_templates")
public class NotificationTemplate extends AbstractAuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel = NotificationChannel.EMAIL;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String body;
}
