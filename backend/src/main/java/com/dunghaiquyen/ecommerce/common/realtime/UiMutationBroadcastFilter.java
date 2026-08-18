package com.dunghaiquyen.ecommerce.common.realtime;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UiMutationBroadcastFilter extends OncePerRequestFilter {
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final UiRealtimeService realtimeService;

    public UiMutationBroadcastFilter(UiRealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, response);
        String path = request.getRequestURI();
        if (MUTATING_METHODS.contains(request.getMethod())
                && shouldBroadcast(path)
                && response.getStatus() >= 200
                && response.getStatus() < 300) {
            realtimeService.publish(path);
        }
    }

    private boolean shouldBroadcast(String path) {
        // Inventory already has its own fine-grained stock-changed stream.
        if (path.startsWith("/api/v1/admin/inventory")) return false;
        // Admin writes and explicit customer order/review/return actions affect
        // data visible in another application. Background calls (preview,
        // search, tracking logs, payment queries, etc.) must never refresh UIs.
        return path.startsWith("/api/v1/admin/")
                || path.equals("/api/v1/orders")
                || path.matches("/api/v1/orders/[^/]+/cancel")
                || path.equals("/api/v1/reviews")
                || path.equals("/api/v1/returns")
                || path.matches("/api/v1/returns/[^/]+/cancel");
    }
}
