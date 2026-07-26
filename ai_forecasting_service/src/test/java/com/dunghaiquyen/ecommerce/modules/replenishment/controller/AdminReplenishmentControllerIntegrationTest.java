package com.dunghaiquyen.ecommerce.modules.replenishment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AdminReplenishmentControllerIntegrationTest {
    private static final String JWT_SECRET =
            "integration-test-access-secret-that-is-at-least-32-bytes";
    private static final UUID ADMIN_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CUSTOMER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.jwt.access-secret", () -> JWT_SECRET);
        registry.add("app.jwt.refresh-secret", () -> JWT_SECRET + "-refresh");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ReplenishmentRecommendationRepository recommendationRepository;
    @Autowired JdbcClient jdbc;

    @BeforeEach
    void cleanDatabase() {
        recommendationRepository.deleteAll();
        jdbc.sql("delete from ai_inventory_snapshot").update();
        jdbc.sql("delete from ai_product_variant_snapshot").update();
    }

    @Test
    void protectedApiRejectsMissingJwtAndCustomerRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions")
                        .header("Authorization", bearer(CUSTOMER_ID, "CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptUsesJwtPrincipalAndDoesNotChangeInventorySnapshot() throws Exception {
        UUID variantId = UUID.randomUUID();
        ReplenishmentRecommendation recommendation = pendingRecommendation(variantId, 18);
        insertInventorySnapshot(variantId, 25, 4);

        mockMvc.perform(post(actionUrl(recommendation, "accept"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"approved for next import\"}"))
                .andExpect(status().isOk());

        ReplenishmentRecommendation saved = find(recommendation);
        assertThat(saved.getStatus()).isEqualTo(ReplenishmentStatus.ACCEPTED);
        assertThat(saved.getAdminQuantity()).isEqualTo(18);
        assertThat(saved.getAdminNote()).isEqualTo("approved for next import");
        assertThat(saved.getActedBy()).isEqualTo(ADMIN_ID);
        assertThat(saved.getActedAt()).isNotNull();
        assertInventorySnapshot(variantId, 25, 4);
    }

    @Test
    void adjustPersistsQuantityAndDismissRequiresNote() throws Exception {
        ReplenishmentRecommendation adjusted = pendingRecommendation(UUID.randomUUID(), 18);
        mockMvc.perform(post(actionUrl(adjusted, "adjust"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":12,\"note\":\"one pack\"}"))
                .andExpect(status().isOk());

        ReplenishmentRecommendation savedAdjusted = find(adjusted);
        assertThat(savedAdjusted.getStatus()).isEqualTo(ReplenishmentStatus.ADJUSTED);
        assertThat(savedAdjusted.getAdminQuantity()).isEqualTo(12);
        assertThat(savedAdjusted.getActedBy()).isEqualTo(ADMIN_ID);

        ReplenishmentRecommendation dismissed = pendingRecommendation(UUID.randomUUID(), 8);
        mockMvc.perform(post(actionUrl(dismissed, "dismiss"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"   \"}"))
                .andExpect(status().isUnprocessableEntity());
        assertThat(find(dismissed).getStatus()).isEqualTo(ReplenishmentStatus.PENDING);

        mockMvc.perform(post(actionUrl(dismissed, "dismiss"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"supplier unavailable\"}"))
                .andExpect(status().isOk());
        assertThat(find(dismissed).getStatus()).isEqualTo(ReplenishmentStatus.DISMISSED);
    }

    @Test
    void rejectsNegativeAdjustmentAndRepeatedTransition() throws Exception {
        ReplenishmentRecommendation recommendation = pendingRecommendation(UUID.randomUUID(), 9);
        mockMvc.perform(post(actionUrl(recommendation, "adjust"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":-1}"))
                .andExpect(status().isUnprocessableEntity());
        assertThat(find(recommendation).getStatus()).isEqualTo(ReplenishmentStatus.PENDING);

        mockMvc.perform(post(actionUrl(recommendation, "accept"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(post(actionUrl(recommendation, "dismiss"))
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"too late\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listSuggestionsFiltersByStatusPriorityAndKeyword() throws Exception {
        UUID pendingHighVariant = UUID.randomUUID();
        UUID dismissedHighVariant = UUID.randomUUID();
        UUID pendingLowVariant = UUID.randomUUID();
        insertVariantSnapshot(pendingHighVariant, "SKU-PENDING-HIGH", "Compression Tee");
        insertVariantSnapshot(dismissedHighVariant, "SKU-DISMISSED-HIGH", "Dismissed Shorts");
        insertVariantSnapshot(pendingLowVariant, "SKU-PENDING-LOW", "Recovery Hoodie");
        recommendation(pendingHighVariant, 18, ReplenishmentPriority.HIGH, ReplenishmentStatus.PENDING);
        recommendation(dismissedHighVariant, 12, ReplenishmentPriority.HIGH, ReplenishmentStatus.DISMISSED);
        recommendation(pendingLowVariant, 6, ReplenishmentPriority.LOW, ReplenishmentStatus.PENDING);

        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions")
                        .param("status", "PENDING")
                        .param("limit", "100")
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions")
                        .param("priority", "HIGH")
                        .param("limit", "100")
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions")
                        .param("status", "PENDING")
                        .param("keyword", "compression")
                        .param("limit", "100")
                        .header("Authorization", bearer(ADMIN_ID, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU-PENDING-HIGH"));
    }

    private ReplenishmentRecommendation pendingRecommendation(UUID variantId, int suggestedQuantity) {
        return recommendation(variantId, suggestedQuantity, ReplenishmentPriority.HIGH, ReplenishmentStatus.PENDING);
    }

    private ReplenishmentRecommendation recommendation(UUID variantId, int suggestedQuantity,
                                                       ReplenishmentPriority priority,
                                                       ReplenishmentStatus status) {
        ReplenishmentRecommendation recommendation = new ReplenishmentRecommendation();
        recommendation.setVariantId(variantId);
        recommendation.setAvailableQuantity(10);
        recommendation.setIncomingQuantity(0);
        recommendation.setReorderPoint(20);
        recommendation.setSafetyStock(5);
        recommendation.setSuggestedQuantity(suggestedQuantity);
        recommendation.setEstimatedStockoutDays(5);
        recommendation.setPriority(priority);
        recommendation.setStatus(status);
        recommendation.setExplanation(Map.of("summary", "integration test"));
        return recommendationRepository.saveAndFlush(recommendation);
    }

    private ReplenishmentRecommendation find(ReplenishmentRecommendation recommendation) {
        return recommendationRepository.findById(recommendation.getId()).orElseThrow();
    }

    private String actionUrl(ReplenishmentRecommendation recommendation, String action) {
        return "/api/v1/admin/replenishment/suggestions/" + recommendation.getId() + "/" + action;
    }

    private String bearer(UUID userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "access")
                .claim("role", role)
                .issuedAt(java.util.Date.from(Instant.now()))
                .expiration(java.util.Date.from(Instant.now().plusSeconds(300)))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    private void insertInventorySnapshot(UUID variantId, int stock, int reserved) {
        jdbc.sql("""
                insert into ai_inventory_snapshot
                    (variant_id, stock_quantity, reserved_quantity, captured_at)
                values (:variantId, :stock, :reserved, now())
                """)
                .param("id", UUID.randomUUID())
                .param("variantId", variantId)
                .param("stock", stock)
                .param("reserved", reserved)
                .update();
    }

    private void insertVariantSnapshot(UUID variantId, String sku, String productName) {
        jdbc.sql("""
                insert into ai_product_variant_snapshot
                    (variant_id, product_id, sku, product_name, size, color, captured_at)
                values (:variantId, :productId, :sku, :productName, 'M', 'Black', now())
                """)
                .param("variantId", variantId)
                .param("productId", UUID.randomUUID())
                .param("sku", sku)
                .param("productName", productName)
                .update();
    }

    private void assertInventorySnapshot(UUID variantId, int stock, int reserved) {
        Map<String, Object> row = jdbc.sql("""
                select stock_quantity, reserved_quantity
                from ai_inventory_snapshot where variant_id=:variantId
                """)
                .param("variantId", variantId)
                .query()
                .singleRow();
        assertThat(row.get("stock_quantity")).isEqualTo(stock);
        assertThat(row.get("reserved_quantity")).isEqualTo(reserved);
    }
}

