package com.dunghaiquyen.ecommerce.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class RefreshTokenIntegrationTest extends AbstractIntegrationTest {

    @Test
    void refresh_success_rotatesToken() throws Exception {
        TokenPair tokens = registerUser(uniqueEmail("refresh-ok"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + tokens.refreshToken() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"not.a.jwt\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void refresh_revokedToken_returns401() throws Exception {
        TokenPair tokens = registerUser(uniqueEmail("refresh-revoked"));

        // first rotation revokes the original token
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + tokens.refreshToken() + "\"}"))
                .andExpect(status().isOk());

        // reusing the now-revoked original token must fail
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + tokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked"));
    }

    @Test
    void refresh_oneTimeUse_secondAttemptWithSameTokenFails() throws Exception {
        // Same scenario as refresh_revokedToken_returns401, named explicitly per the
        // one-time-use requirement: rotate once, the SAME old token must never work again.
        TokenPair tokens = registerUser(uniqueEmail("refresh-once"));
        String original = tokens.refreshToken();

        MvcResult first = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + original + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode firstBody = json(first.getResponse().getContentAsString());
        String rotated = firstBody.at("/data/refreshToken").asText();
        assertThat(rotated).isNotEqualTo(original);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + original + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_concurrentRequestsOnSameToken_exactlyOneSucceeds() throws Exception {
        TokenPair tokens = registerUser(uniqueEmail("refresh-race"));
        String refreshToken = tokens.refreshToken();
        int concurrency = 10;

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        try {
            Callable<Integer> call = () -> mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            List<Future<Integer>> futures = pool.invokeAll(List.of(
                    call, call, call, call, call, call, call, call, call, call));

            List<Integer> statusCodes = futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            long successCount = statusCodes.stream().filter(code -> code == 200).count();
            long unauthorizedCount = statusCodes.stream().filter(code -> code == 401).count();

            assertThat(statusCodes).hasSize(concurrency);
            assertThat(successCount).isEqualTo(1);
            assertThat(unauthorizedCount).isEqualTo(concurrency - 1);
        } finally {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    @Test
    void refresh_winnersNewTokenAfterRace_isAlsoRevokedByLosersSweep_documentsCurrentBehavior() throws Exception {
        // Follow-up on refresh_concurrentRequestsOnSameToken_exactlyOneSucceeds: that
        // test only proves "exactly 1 of N wins". It does NOT prove the winner's own
        // freshly-minted refreshToken is still usable afterwards. This test answers
        // that specific question.
        //
        // Current observed behavior (see RefreshTokenRepository#revokeAllActiveForUser
        // javadoc for the implementation-level explanation): every LOSER calls an
        // UNSCOPED revokeAllActiveForUser(user) before failing. Postgres holds the row
        // lock on the original token until the WINNER's whole transaction (including
        // the insert of its new sibling token) commits, so every loser's sweep runs
        // strictly AFTER that commit and therefore also revokes the winner's brand
        // new token - deterministically, not flaky.
        //
        // This is a known, accepted, fail-closed tradeoff (forces one extra
        // login/refresh on a benign concurrent double-call; never grants
        // unauthorized access) - NOT a bug being fixed here. This test only pins
        // down the current behavior so a future change to AuthService.refresh()'s
        // revoke-all scoping cannot silently flip it unnoticed: if this assertion
        // starts failing with 200 instead of 401, that is a deliberate behavior
        // change to call out, not a fix to wave through.
        TokenPair tokens = registerUser(uniqueEmail("refresh-race-winner"));
        String refreshToken = tokens.refreshToken();
        int concurrency = 10;

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        String winnerNewRefreshToken;
        try {
            Callable<MvcResult> call = () -> mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                    .andReturn();

            List<Future<MvcResult>> futures = pool.invokeAll(List.of(
                    call, call, call, call, call, call, call, call, call, call));

            List<MvcResult> results = futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            List<MvcResult> winners = results.stream()
                    .filter(r -> r.getResponse().getStatus() == 200)
                    .collect(Collectors.toList());
            assertThat(winners).hasSize(1);

            JsonNode winnerBody = json(winners.get(0).getResponse().getContentAsString());
            winnerNewRefreshToken = winnerBody.at("/data/refreshToken").asText();
            assertThat(winnerNewRefreshToken).isNotBlank();
        } finally {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }

        // Documents the current fail-closed behavior: the winner's new token does
        // NOT survive the losers' unscoped revoke-all sweep.
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + winnerNewRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked"));
    }
}
