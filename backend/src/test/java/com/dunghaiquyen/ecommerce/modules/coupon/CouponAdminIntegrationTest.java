package com.dunghaiquyen.ecommerce.modules.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class CouponAdminIntegrationTest extends AbstractIntegrationTest {

    private String adminToken() throws Exception {
        return registerAdminAndGetAccessToken(uniqueEmail("coupon-admin"));
    }

    private String createOrderScopePromotion(String token, int discountPercent) throws Exception {
        String slug = "coupon-promo-" + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"PERCENTAGE\",\"scope\":\"ORDER\","
                                        + "\"status\":\"ACTIVE\",\"discountPercent\":%d}")
                                .formatted(slug, discountPercent)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    // ===== admin can create a coupon linked to a promotion =====

    @Test
    void createCoupon_withPromotion_succeeds() throws Exception {
        String token = adminToken();
        String promotionId = createOrderScopePromotion(token, 10);
        String code = "SAVE-" + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"code\":\"%s\",\"promotionId\":\"%s\"}").formatted(code, promotionId)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/code").asText()).isEqualTo(code.toUpperCase());
        assertThat(body.at("/data/status").asText()).isEqualTo("ACTIVE");
        assertThat(body.at("/data/promotionId").asText()).isEqualTo(promotionId);
    }

    // ===== duplicate code rejected =====

    @Test
    void createCoupon_duplicateCode_returns409() throws Exception {
        String token = adminToken();
        String promotionId = createOrderScopePromotion(token, 10);
        String code = "DUP-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"code\":\"%s\",\"promotionId\":\"%s\"}").formatted(code, promotionId)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"code\":\"%s\",\"promotionId\":\"%s\"}").formatted(code.toLowerCase(), promotionId)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
    }

    // ===== promotionId must exist =====

    @Test
    void createCoupon_promotionNotFound_returns404() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"code\":\"NOPROMO-%s\",\"promotionId\":\"%s\"}")
                                .formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    // ===== update changes status =====

    @Test
    void updateCoupon_changesStatus() throws Exception {
        String token = adminToken();
        String promotionId = createOrderScopePromotion(token, 10);
        String code = "UPD-" + UUID.randomUUID();
        MvcResult created = mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"code\":\"%s\",\"promotionId\":\"%s\"}").formatted(code, promotionId)))
                .andExpect(status().isCreated())
                .andReturn();
        String couponId = json(created.getResponse().getContentAsString()).at("/data/id").asText();

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/coupons/" + couponId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result.getResponse().getContentAsString()).at("/data/status").asText())
                .isEqualTo("INACTIVE");
    }

    // ===== non-admin cannot manage coupons =====

    @Test
    void nonAdmin_cannotCreateCoupon_returns403() throws Exception {
        TokenPair customer = registerUser(uniqueEmail("coupon-not-admin"));
        mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"X\",\"promotionId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isForbidden());
    }
}
