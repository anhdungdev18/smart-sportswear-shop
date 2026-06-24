package com.dunghaiquyen.ecommerce.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class LoginIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void login_success_returnsUserAndTokens() throws Exception {
        String email = uniqueEmail("login-ok");
        registerUser(email);

        String body = """
                {"email":"%s","password":"Password123"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokens.refreshToken").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String email = uniqueEmail("login-wrong-pwd");
        registerUser(email);

        String body = """
                {"email":"%s","password":"WrongPassword1"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_unknownEmail_returns401SameMessageAsWrongPassword() throws Exception {
        String body = """
                {"email":"%s","password":"WhateverPassword1"}
                """.formatted(uniqueEmail("login-unknown"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_lockedAccount_returns403_evenWithCorrectPassword() throws Exception {
        String email = uniqueEmail("login-locked");
        registerUser(email);
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);

        String body = """
                {"email":"%s","password":"Password123"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Account is locked"));
    }
}
