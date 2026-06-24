package com.dunghaiquyen.ecommerce.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RegisterIntegrationTest extends AbstractIntegrationTest {

    @Test
    void register_success_returnsUserAndTokens() throws Exception {
        String email = uniqueEmail("register-ok");
        String body = """
                {"fullName":"Nguyen Van A","email":"%s","password":"Password123","phone":"0900000000"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Register successful"))
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.data.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokens.refreshToken").isNotEmpty());
    }

    @Test
    void register_clientSuppliedRole_isIgnored_alwaysCustomer() throws Exception {
        String email = uniqueEmail("register-role-injection");
        // RegisterRequest has no "role" field at all; an extra unknown JSON
        // property is simply ignored by Jackson binding, not bound to anything.
        String body = """
                {"fullName":"Hacker","email":"%s","password":"Password123","role":"ADMIN"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.role").value("CUSTOMER"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String email = uniqueEmail("register-dup");
        String body = """
                {"fullName":"A","email":"%s","password":"Password123"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists: " + email));
    }

    @Test
    void register_invalidPayload_returns422WithFieldErrors() throws Exception {
        String email = uniqueEmail("register-invalid");
        // password fails the "min 8 chars + letter + digit" rule
        String body = """
                {"fullName":"X","email":"%s","password":"short"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }
}
