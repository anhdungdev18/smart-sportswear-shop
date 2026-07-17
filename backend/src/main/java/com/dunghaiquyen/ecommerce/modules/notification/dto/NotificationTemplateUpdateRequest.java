package com.dunghaiquyen.ecommerce.modules.notification.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import jakarta.validation.constraints.Size;

/**
 * Partial update (null = leave unchanged). type/channel are identity, not
 * editable here - only subject/body, this template's configurable surface.
 * See NotificationTemplateService.update for the placeholder validation applied
 * to whichever of these is actually submitted.
 */
public record NotificationTemplateUpdateRequest(
        @NullOrNotBlank @Size(max = 255, message = "subject must be at most 255 characters") String subject,
        @NullOrNotBlank String body) {
}
