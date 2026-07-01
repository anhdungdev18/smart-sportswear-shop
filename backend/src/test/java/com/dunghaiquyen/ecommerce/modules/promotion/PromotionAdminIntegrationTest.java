package com.dunghaiquyen.ecommerce.modules.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

class PromotionAdminIntegrationTest extends AbstractIntegrationTest {

    private record AdminContext(String token, String categoryId, String brandId) {
    }

    private AdminContext setUpAdmin() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("promo-admin"));
        String categorySlug = "promo-cat-" + UUID.randomUUID();
        MvcResult cat = mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat\",\"slug\":\"" + categorySlug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = json(cat.getResponse().getContentAsString()).at("/data/id").asText();

        String brandSlug = "promo-brand-" + UUID.randomUUID();
        MvcResult brand = mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Brand\",\"slug\":\"" + brandSlug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String brandId = json(brand.getResponse().getContentAsString()).at("/data/id").asText();

        return new AdminContext(token, categoryId, brandId);
    }

    private String createActiveProduct(AdminContext ctx, String name) throws Exception {
        String slug = "promo-prod-" + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"%s\",\"slug\":\"%s\",\"categoryId\":\"%s\",\"brandId\":\"%s\",\"status\":\"ACTIVE\"}")
                                .formatted(name, slug, ctx.categoryId(), ctx.brandId())))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String orderScopePercentBody(String slug) {
        return ("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"PERCENTAGE\",\"scope\":\"ORDER\","
                + "\"status\":\"ACTIVE\",\"discountPercent\":10}")
                .formatted(slug);
    }

    // ===== admin can create an ORDER-scope promotion =====

    @Test
    void createPromotion_orderScopePercentage_succeeds() throws Exception {
        AdminContext ctx = setUpAdmin();
        String slug = "promo-" + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderScopePercentBody(slug)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/scope").asText()).isEqualTo("ORDER");
        assertThat(body.at("/data/discountPercent").asDouble()).isEqualTo(10.0);
        assertThat(body.at("/data/usageCount").asInt()).isEqualTo(0);
    }

    // ===== PRODUCT-scope promotion requires productIds, and links them =====

    @Test
    void createPromotion_productScope_attachesProducts() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Promo Shirt");
        String slug = "promo-" + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"FIXED_AMOUNT\",\"scope\":\"PRODUCT\","
                                        + "\"status\":\"ACTIVE\",\"discountAmount\":5000,\"productIds\":[\"%s\"]}")
                                .formatted(slug, productId)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/productIds")).hasSize(1);
        assertThat(body.at("/data/productIds/0").asText()).isEqualTo(productId);
    }

    // ===== PRODUCT scope without productIds is rejected =====

    @Test
    void createPromotion_productScopeWithoutProductIds_returns422() throws Exception {
        AdminContext ctx = setUpAdmin();
        String slug = "promo-" + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"FIXED_AMOUNT\",\"scope\":\"PRODUCT\","
                                + "\"status\":\"ACTIVE\",\"discountAmount\":5000}")
                                .formatted(slug)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== mismatched discount shape for type is rejected =====

    @Test
    void createPromotion_percentageWithDiscountAmountInstead_returns422() throws Exception {
        AdminContext ctx = setUpAdmin();
        String slug = "promo-" + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"PERCENTAGE\",\"scope\":\"ORDER\","
                                + "\"status\":\"ACTIVE\",\"discountAmount\":5000}")
                                .formatted(slug)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== update changes status and discount fields =====

    @Test
    void updatePromotion_changesStatusAndDiscount() throws Exception {
        AdminContext ctx = setUpAdmin();
        String slug = "promo-" + UUID.randomUUID();
        MvcResult created = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderScopePercentBody(slug)))
                .andExpect(status().isCreated())
                .andReturn();
        String promotionId = json(created.getResponse().getContentAsString()).at("/data/id").asText();

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/promotions/" + promotionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\",\"discountPercent\":15}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/status").asText()).isEqualTo("INACTIVE");
        assertThat(body.at("/data/discountPercent").asDouble()).isEqualTo(15.0);
    }

    // ===== PATCH with empty productIds clears PRODUCT-scope links wholesale =====

    @Test
    void updatePromotion_productScopeWithEmptyProductIds_clearsProducts() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Promo Clear");
        String slug = "promo-" + UUID.randomUUID();
        MvcResult created = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"FIXED_AMOUNT\",\"scope\":\"PRODUCT\","
                                        + "\"status\":\"ACTIVE\",\"discountAmount\":5000,\"productIds\":[\"%s\"]}")
                                .formatted(slug, productId)))
                .andExpect(status().isCreated())
                .andReturn();
        String promotionId = json(created.getResponse().getContentAsString()).at("/data/id").asText();

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/promotions/" + promotionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productIds\":[]}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result.getResponse().getContentAsString()).at("/data/productIds")).isEmpty();
    }

    // ===== list returns created promotions =====

    @Test
    void listPromotions_returnsCreatedPromotion() throws Exception {
        AdminContext ctx = setUpAdmin();
        String slug = "promo-" + UUID.randomUUID();
        mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderScopePercentBody(slug)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/promotions?limit=100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode item : body.at("/data")) {
            if (item.at("/slug").asText().equals(slug)) {
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    // ===== non-admin cannot manage promotions =====

    @Test
    void nonAdmin_cannotCreatePromotion_returns403() throws Exception {
        TokenPair customer = registerUser(uniqueEmail("promo-not-admin"));
        mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderScopePercentBody("promo-" + UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}
