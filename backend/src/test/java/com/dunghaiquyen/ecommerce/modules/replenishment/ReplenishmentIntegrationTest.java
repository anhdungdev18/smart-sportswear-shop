package com.dunghaiquyen.ecommerce.modules.replenishment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.dunghaiquyen.ecommerce.common.security.JwtTokenProvider;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.ReplenishmentRecommendationRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ReplenishmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ReplenishmentRecommendationRepository recommendationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setup() {
        admin = userRepository.findByEmail("admin@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin-test@example.com");
            u.setPasswordHash("hash");
            u.setFullName("Admin");
            u.setRole(com.dunghaiquyen.ecommerce.modules.user.entity.UserRole.ADMIN);
            return userRepository.save(u);
        });

        customer = userRepository.findByEmail("customer@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("customer-test@example.com");
            u.setPasswordHash("hash");
            u.setFullName("Customer");
            u.setRole(com.dunghaiquyen.ecommerce.modules.user.entity.UserRole.CUSTOMER);
            return userRepository.save(u);
        });

        adminToken = "Bearer " + jwtTokenProvider.generateAccessToken(admin.getId(), admin.getRole());
        customerToken = "Bearer " + jwtTokenProvider.generateAccessToken(customer.getId(), customer.getRole());
    }

    @Test
    void customerCannotAccessReplenishmentApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions")
                .header(HttpHeaders.AUTHORIZATION, customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessReplenishmentApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/replenishment/suggestions")
                .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void canAcceptSuggestion() throws Exception {
        // Find a variant
        List<ProductVariant> variants = variantRepository.findAll();
        if (variants.isEmpty()) return;
        ProductVariant variant = variants.get(0);

        // Create a pending suggestion
        ReplenishmentRecommendation rec = new ReplenishmentRecommendation();
        rec.setVariant(variant);
        rec.setAvailableQuantity(10);
        rec.setIncomingQuantity(0);
        rec.setReorderPoint(15);
        rec.setSafetyStock(5);
        rec.setSuggestedQuantity(50);
        rec.setPriority(ReplenishmentPriority.MEDIUM);
        rec.setStatus(ReplenishmentStatus.PENDING);
        rec = recommendationRepository.save(rec);

        mockMvc.perform(post("/api/v1/admin/replenishment/suggestions/" + rec.getId() + "/accept")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("note", "Looks good"))))
                .andExpect(status().isOk());

        ReplenishmentRecommendation updated = recommendationRepository.findById(rec.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReplenishmentStatus.ACCEPTED);
        assertThat(updated.getAdminQuantity()).isEqualTo(50);
        assertThat(updated.getAdminNote()).isEqualTo("Looks good");
        assertThat(updated.getActedBy().getId()).isEqualTo(admin.getId());
    }
}
