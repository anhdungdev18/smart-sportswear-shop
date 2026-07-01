package com.dunghaiquyen.ecommerce.modules.notification.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationListQuery;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationResponse;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service only - any authenticated role may see their OWN notification
 * history (mirrors MeController / OrderController's "/me" pattern). The full
 * cross-user history lives at AdminNotificationController, ADMIN-only.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ApiResponse<List<NotificationResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal, @ModelAttribute NotificationListQuery query) {
        NotificationService.ListResult result = notificationService.listMine(principal.getUserId(), query);
        return ApiResponse.ok(result.items(), result.meta());
    }
}
