package com.dunghaiquyen.ecommerce.modules.notification.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationTemplateResponse;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationTemplateUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN only - editing transactional email copy is system configuration, same sensitivity bar as AdminNotificationController's history view. */
@RestController
@RequestMapping("/api/v1/admin/notification-templates")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    public AdminNotificationTemplateController(NotificationTemplateService notificationTemplateService) {
        this.notificationTemplateService = notificationTemplateService;
    }

    @GetMapping
    public ApiResponse<List<NotificationTemplateResponse>> list() {
        return ApiResponse.ok(notificationTemplateService.list());
    }

    @PatchMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> update(
            @PathVariable UUID id, @Valid @RequestBody NotificationTemplateUpdateRequest request) {
        return ApiResponse.ok("Notification template updated", notificationTemplateService.update(id, request));
    }
}
