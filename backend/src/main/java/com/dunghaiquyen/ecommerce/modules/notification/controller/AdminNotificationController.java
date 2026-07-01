package com.dunghaiquyen.ecommerce.modules.notification.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.notification.dto.AdminNotificationListQuery;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationResponse;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Full send history across all users - ADMIN only (Phase O requirement: customers must not see other users' notifications). */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list(@ModelAttribute AdminNotificationListQuery query) {
        NotificationService.ListResult result = notificationService.list(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    /** See NotificationService.resend's javadoc for eligibility/cap/cooldown rules and the audit-trail strategy. */
    @PostMapping("/{id}/resend")
    public ApiResponse<NotificationResponse> resend(@PathVariable UUID id) {
        return ApiResponse.ok("Notification resent", notificationService.resend(id));
    }
}
