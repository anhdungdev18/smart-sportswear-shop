package com.dunghaiquyen.ecommerce.modules.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class PublicPromotionIntegrationTest extends AbstractIntegrationTest {

    private String createPromotion(String adminToken, String name, String status, Instant startsAt, Instant endsAt)
            throws Exception {
        String slug = "pub-promo-" + UUID.randomUUID();
        StringBuilder body = new StringBuilder(
                ("{\"name\":\"%s\",\"slug\":\"%s\",\"type\":\"PERCENTAGE\",\"scope\":\"ORDER\","
                                + "\"status\":\"%s\",\"discountPercent\":10")
                        .formatted(name, slug, status));
        if (startsAt != null) {
            body.append(",\"startsAt\":\"").append(startsAt).append("\"");
        }
        if (endsAt != null) {
            body.append(",\"endsAt\":\"").append(endsAt).append("\"");
        }
        body.append("}");

        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    // ===== only ACTIVE + within time window is returned =====

    @Test
    void active_returnsOnlyActivePromotionsCurrentlyWithinTimeWindow() throws Exception {
        String adminToken = registerAdminAndGetAccessToken(uniqueEmail("pubpromo-admin"));
        Instant now = Instant.now();

        String activeNoWindowId = createPromotion(adminToken, "Active No Window", "ACTIVE", null, null);
        String activeWithinWindowId = createPromotion(
                adminToken, "Active Within Window", "ACTIVE",
                now.minus(1, ChronoUnit.DAYS), now.plus(10, ChronoUnit.DAYS));
        String draftId = createPromotion(adminToken, "Draft", "DRAFT", null, null);
        String inactiveId = createPromotion(adminToken, "Inactive", "INACTIVE", null, null);
        String notStartedId = createPromotion(
                adminToken, "Not Started Yet", "ACTIVE", now.plus(10, ChronoUnit.DAYS), null);
        String alreadyEndedId = createPromotion(
                adminToken, "Already Ended", "ACTIVE", null, now.minus(1, ChronoUnit.DAYS));

        MvcResult result = mockMvc.perform(get("/api/v1/promotions/active"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = json(result.getResponse().getContentAsString()).at("/data");

        var ids = new java.util.ArrayList<String>();
        data.forEach(item -> ids.add(item.at("/id").asText()));

        assertThat(ids).contains(activeNoWindowId, activeWithinWindowId);
        assertThat(ids).doesNotContain(draftId, inactiveId, notStartedId, alreadyEndedId);
    }

    @Test
    void active_noAuthRequired() throws Exception {
        mockMvc.perform(get("/api/v1/promotions/active"))
                .andExpect(status().isOk());
    }

    // ===== public DTO does not leak admin-only fields =====

    @Test
    void active_responseShape_doesNotExposeAdminOnlyFields() throws Exception {
        String adminToken = registerAdminAndGetAccessToken(uniqueEmail("pubpromo-shape-admin"));
        createPromotion(adminToken, "Shape Check", "ACTIVE", null, null);

        MvcResult result = mockMvc.perform(get("/api/v1/promotions/active"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode first = json(result.getResponse().getContentAsString()).at("/data/0");

        assertThat(first.has("status")).as("status is implicit in this list, not exposed").isFalse();
        assertThat(first.has("usageCount")).as("redemption metrics are not public").isFalse();
        assertThat(first.has("usageLimit")).isFalse();
        assertThat(first.has("createdAt")).isFalse();
        assertThat(first.has("updatedAt")).isFalse();
        assertThat(first.has("name")).isTrue();
        assertThat(first.has("discountPercent")).isTrue();
    }
}
