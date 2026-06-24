package com.dunghaiquyen.ecommerce.common.security;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Invoked by Spring Security whenever a protected endpoint is hit with no
 * valid authentication present (missing/invalid/expired token, or a token
 * for a now-locked user that JwtAuthenticationFilter declined to authenticate) -
 * i.e. the "401" bucket. Insufficient-role-on-a-valid-principal is a
 * different case, handled by RestAccessDeniedHandler (403).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("Authentication required"));
    }
}
