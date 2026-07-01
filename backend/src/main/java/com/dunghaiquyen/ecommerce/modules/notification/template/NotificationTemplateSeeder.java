package com.dunghaiquyen.ecommerce.modules.notification.template;

import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationTemplate;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import com.dunghaiquyen.ecommerce.modules.notification.repository.NotificationTemplateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures every {@link NotificationType} has a real, editable row in
 * notification_templates by application startup, seeded from
 * {@link NotificationTemplates#defaultFor}. Idempotent (existsByType guard) -
 * runs on every startup including each test's @SpringBootTest context, but
 * only ever inserts a missing type once. Seeding happens here (Java, app
 * startup) rather than as INSERT statements in the V7 migration so the
 * Vietnamese default copy stays in Java source - the literal sql file is the
 * one place in this codebase that has not already been proven to round-trip
 * Vietnamese text through Flyway/Postgres.
 */
@Component
@Order(Integer.MIN_VALUE)
public class NotificationTemplateSeeder implements ApplicationRunner {

    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplateSeeder(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (NotificationType type : NotificationType.values()) {
            if (templateRepository.existsByType(type)) {
                continue;
            }
            EmailContent defaults = NotificationTemplates.defaultFor(type);
            NotificationTemplate template = new NotificationTemplate();
            template.setType(type);
            template.setSubject(defaults.subject());
            template.setBody(defaults.body());
            templateRepository.save(template);
        }
    }
}
