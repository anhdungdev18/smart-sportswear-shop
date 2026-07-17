package com.dunghaiquyen.ecommerce.modules.notification.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationListQuery;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationResponse;
import com.dunghaiquyen.ecommerce.modules.notification.dto.UnreadCountResponse;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationService;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationStreamService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Self-service only - any authenticated role may see their OWN notification
 * history (mirrors MeController / OrderController's "/me" pattern). The full
 * cross-user history lives at AdminNotificationController, ADMIN-only.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationStreamService notificationStreamService;

    public NotificationController(
            NotificationService notificationService, NotificationStreamService notificationStreamService) {
        this.notificationService = notificationService;
        this.notificationStreamService = notificationStreamService;
    }

    @GetMapping("/me")
    public ApiResponse<List<NotificationResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal, @ModelAttribute NotificationListQuery query) {
        NotificationService.ListResult result = notificationService.listMine(principal.getUserId(), query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    /** Unread badge count for the in-app inbox bell. */
    @GetMapping("/me/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        long unread = notificationService.unreadCount(principal.getUserId());
        return ApiResponse.ok(new UnreadCountResponse(unread));
    }

    /** Mark a single notification read (own notifications only; idempotent). */
    @PostMapping("/me/{id}/read")
    public ApiResponse<Void> markRead(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID id) {
        notificationService.markRead(principal.getUserId(), id);
        return ApiResponse.ok(null);
    }

    /** Mark all of the caller's unread notifications read; returns count updated. */
    @PostMapping("/me/read-all")
    public ApiResponse<Integer> markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        int updated = notificationService.markAllRead(principal.getUserId());
        return ApiResponse.ok(updated);
    }

    /**
     * Real-time in-app notification stream (SSE). One long-lived GET per browser
     * tab; the server pushes a "notification" event the instant one is created.
     * Authenticates via Authorization header, or an access_token query param
     * (EventSource cannot set headers) — see JwtAuthenticationFilter.
     */
    @GetMapping("/me/stream")
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails principal) {
        return notificationStreamService.register(principal.getUserId());
    }
}
