package com.dunghaiquyen.ecommerce.modules.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.ai.sync-secret=core-ai-integration-secret")
class AiReplenishmentDataControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/internal/v1/ai/replenishment/snapshot";
    private static final String BODY = """
            {"fromInclusive":"2026-01-01","toInclusive":"2026-01-02","variantIds":[]}
            """;

    @Test
    void rejectsMissingAndIncorrectSyncSecret() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ENDPOINT)
                        .header("X-AI-Sync-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsCorrectSecretAndRejectsInvalidDateRange() throws Exception {
        var valid = mockMvc.perform(post(ENDPOINT)
                        .header("X-AI-Sync-Secret", "core-ai-integration-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(valid.getResponse().getContentAsString()).at("/data/generatedAt").asText())
                .isNotBlank();

        String invalidRange = """
                {"fromInclusive":"%s","toInclusive":"%s","variantIds":[]}
                """.formatted(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1));
        mockMvc.perform(post(ENDPOINT)
                        .header("X-AI-Sync-Secret", "core-ai-integration-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRange))
                .andExpect(status().isUnprocessableEntity());
    }
}
