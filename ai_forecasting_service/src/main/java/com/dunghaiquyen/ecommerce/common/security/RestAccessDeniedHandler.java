package com.dunghaiquyen.ecommerce.common.security;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Invoked by Spring Security when a request has valid authentication but
 * fails an authorizeHttpRequests rule (e.g. a future hasRole(...) URL rule) -
 * the "403" bucket. @PreAuthorize denials inside a controller method throw
 * the same AccessDeniedException but never reach this class - those happen
 * inside MVC dispatch and are already handled by
 * GlobalExceptionHandler#handleAccessDenied with the same message, so both
 * layers stay consistent.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("Access denied"));
    }
}
