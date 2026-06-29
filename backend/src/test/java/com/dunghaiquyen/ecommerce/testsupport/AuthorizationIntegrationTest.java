package com.dunghaiquyen.ecommerce.testsupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * Focused on the ApiResponse envelope contract for the two security-layer
 * rejection paths, independent of any particular business endpoint:
 * - no/invalid auth -> RestAuthenticationEntryPoint -> 401
 * - valid auth, insufficient role -> @PreAuthorize -> GlobalExceptionHandler -> 403
 * (a URL-pattern hasRole() rule would hit RestAccessDeniedHandler instead, but
 * produces the identical status+body, see SecurityConfig javadoc).
 */
class AuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void protectedEndpoint_noToken_returns401StandardApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void protectedEndpoint_garbageToken_returns401StandardApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void authenticatedButInsufficientRole_returns403StandardApiResponse() throws Exception {
        TokenPair tokens = registerUser(uniqueEmail("authz-403")); // CUSTOMER, not ADMIN

        mockMvc.perform(get("/api/v1/_test/admin-only")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }
}
