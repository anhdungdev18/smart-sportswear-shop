package com.dunghaiquyen.ecommerce.modules.notification.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationTemplateResponse;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationTemplateUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationTemplate;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import com.dunghaiquyen.ecommerce.modules.notification.repository.NotificationTemplateRepository;
import com.dunghaiquyen.ecommerce.modules.notification.template.NotificationPlaceholders;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin CRUD over notification_templates - GET /api/v1/admin/notification-templates
 * and PATCH .../{id} (Notification Operations phase). Deliberately separate
 * from {@link NotificationTemplates} (the renderer NotificationService
 * actually calls at send time): this service only ever touches the
 * subject/body TEXT an admin can edit, never builds an actual EmailContent
 * or talks to MailService - that stays centralized in NotificationTemplates,
 * per the "one place builds subject/body" rule this phase must not violate.
 */
@Service
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> list() {
        return templateRepository.findAll().stream()
                .sorted((a, b) -> a.getType().compareTo(b.getType()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Only rejects placeholder tokens NotificationPlaceholders does not
     * recognize for this template's type - a token that IS allowed but
     * simply omitted from the new text is fine (e.g. an admin who wants a
     * shorter email without {paymentMethod} is making a legitimate content
     * choice, not breaking anything). What this guards against specifically
     * is a typo'd or invented {token} reaching a customer's inbox as
     * literal, un-substituted text - the one way a saved template can
     * actually be "hỏng" per this phase's own requirement.
     */
    @Transactional
    public NotificationTemplateResponse update(UUID id, NotificationTemplateUpdateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification template not found"));

        if (request.subject() != null) {
            validatePlaceholders(template.getType(), request.subject());
            template.setSubject(request.subject());
        }
        if (request.body() != null) {
            validatePlaceholders(template.getType(), request.body());
            template.setBody(request.body());
        }

        return toResponse(templateRepository.save(template));
    }

    private void validatePlaceholders(NotificationType type, String text) {
        Set<String> allowed = NotificationPlaceholders.allowedFor(type);
        Set<String> used = NotificationPlaceholders.tokensIn(text);
        Set<String> unknown = used.stream().filter(token -> !allowed.contains(token)).collect(java.util.stream.Collectors.toSet());
        if (!unknown.isEmpty()) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Unknown placeholder(s) for " + type + ": " + unknown + ". Allowed: " + allowed);
        }
    }

    private NotificationTemplateResponse toResponse(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getType(),
                template.getChannel(),
                template.getSubject(),
                template.getBody(),
                List.copyOf(NotificationPlaceholders.allowedFor(template.getType())),
                template.getCreatedAt(),
                template.getUpdatedAt());
    }
}
