package com.dunghaiquyen.ecommerce.modules.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.dunghaiquyen.ecommerce.modules.address.entity.Address;
import com.dunghaiquyen.ecommerce.modules.address.repository.AddressRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class ValidateCouponIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AddressRepository addressRepository;

    private record AdminContext(String token, String categoryId, String brandId) {
    }

    private AdminContext setUpAdmin() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("vc-admin"));
        String categorySlug = "vc-cat-" + UUID.randomUUID();
        MvcResult cat = mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat\",\"slug\":\"" + categorySlug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = json(cat.getResponse().getContentAsString()).at("/data/id").asText();

        String brandSlug = "vc-brand-" + UUID.randomUUID();
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
        String slug = "vc-prod-" + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"%s\",\"slug\":\"%s\",\"categoryId\":\"%s\",\"brandId\":\"%s\",\"status\":\"ACTIVE\"}")
                                .formatted(name, slug, ctx.categoryId(), ctx.brandId())))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String createVariant(AdminContext ctx, String productId, int price) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/products/" + productId + "/variants")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"sku\":\"SKU-%s\",\"size\":\"M\",\"color\":\"Black\",\"price\":%d,\"stockQuantity\":50}")
                                .formatted(UUID.randomUUID(), price)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private void addToCart(String token, String variantId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":\"" + variantId + "\",\"quantity\":" + quantity + "}"))
                .andExpect(status().isCreated());
    }

    private String createAddressForUser(String email) {
        var user = userRepository.findByEmail(email).orElseThrow();
        Address address = new Address();
        address.setUser(user);
        address.setReceiverName("Test Receiver");
        address.setPhone("0900000000");
        address.setProvince("HCM");
        address.setDistrict("District 1");
        address.setWard("Ward 1");
        address.setAddressLine("123 Test Street");
        return addressRepository.save(address).getId().toString();
    }

    private String createOrderScopePromotion(AdminContext ctx, int discountPercent, Integer minOrderAmount) throws Exception {
        String slug = "vc-promo-" + UUID.randomUUID();
        StringBuilder body = new StringBuilder(
                ("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"PERCENTAGE\",\"scope\":\"ORDER\",\"status\":\"ACTIVE\","
                                + "\"discountPercent\":%d")
                        .formatted(slug, discountPercent));
        if (minOrderAmount != null) {
            body.append(",\"minOrderAmount\":").append(minOrderAmount);
        }
        body.append("}");
        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String createCoupon(AdminContext ctx, String promotionId, String code, StringBuilder extraFields) throws Exception {
        String body = ("{\"code\":\"%s\",\"promotionId\":\"%s\"%s}").formatted(code, promotionId, extraFields);
        MvcResult result = mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private MvcResult validateCoupon(String token, String couponCode) throws Exception {
        return mockMvc.perform(post("/api/v1/checkout/validate-coupon")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"" + couponCode + "\"}"))
                .andReturn();
    }

    private MvcResult checkout(String buyerToken, String addressId, String couponCode) throws Exception {
        String couponField = couponCode != null ? ",\"couponCode\":\"" + couponCode + "\"" : "";
        return mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":\"" + addressId + "\",\"paymentMethod\":\"COD\"" + couponField + "}"))
                .andReturn();
    }

    // ===== valid coupon: 200, valid=true, correct discountAmount =====

    @Test
    void validateCoupon_validCoupon_returnsValidTrueWithDiscountAmount() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate Shirt");
        String variantId = createVariant(ctx, productId, 100000);
        String promotionId = createOrderScopePromotion(ctx, 10, null);
        String code = "VC-VALID-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String email = uniqueEmail("vc-valid-buyer");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = validateCoupon(buyer.accessToken(), code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/valid").asBoolean()).isTrue();
        assertThat(body.at("/data/subtotal").asDouble()).isEqualTo(100000.0);
        assertThat(body.at("/data/discountAmount").asDouble()).isEqualTo(10000.0);
        assertThat(body.at("/data/message").isNull()).isTrue();
    }

    // ===== unknown coupon code: 200, valid=false, clean message =====

    @Test
    void validateCoupon_unknownCode_returns200_validFalse_cleanMessage() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate Unknown");
        String variantId = createVariant(ctx, productId, 50000);

        String email = uniqueEmail("vc-unknown-buyer");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = validateCoupon(buyer.accessToken(), "DOES-NOT-EXIST-" + UUID.randomUUID());
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/discountAmount").asDouble()).isEqualTo(0.0);
        assertThat(body.at("/data/message").asText()).isNotBlank();
    }

    // ===== expired coupon: 200, valid=false =====

    @Test
    void validateCoupon_expiredCoupon_returns200_validFalse() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate Expired");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, 10, null);
        String code = "VC-EXPIRED-" + UUID.randomUUID();
        String pastDate = Instant.now().minus(10, ChronoUnit.DAYS).toString();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"endsAt\":\"" + pastDate + "\""));

        String email = uniqueEmail("vc-expired-buyer");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = validateCoupon(buyer.accessToken(), code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/message").asText()).containsIgnoringCase("expired");
    }

    // ===== inactive coupon: 200, valid=false =====

    @Test
    void validateCoupon_inactiveCoupon_returns200_validFalse() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate Inactive");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, 10, null);
        String code = "VC-INACTIVE-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"status\":\"INACTIVE\""));

        String email = uniqueEmail("vc-inactive-buyer");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = validateCoupon(buyer.accessToken(), code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result.getResponse().getContentAsString()).at("/data/valid").asBoolean()).isFalse();
    }

    // ===== usage limit reached: 200, valid=false =====

    @Test
    void validateCoupon_pastUsageLimit_returns200_validFalse() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate UsageLimit");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, 10, null);
        String code = "VC-LIMIT-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"usageLimit\":1"));

        String firstEmail = uniqueEmail("vc-limit-first");
        TokenPair first = registerUser(firstEmail);
        addToCart(first.accessToken(), variantId, 1);
        String firstAddress = createAddressForUser(firstEmail);
        assertThat(checkout(first.accessToken(), firstAddress, code).getResponse().getStatus()).isEqualTo(201);

        String secondEmail = uniqueEmail("vc-limit-second");
        TokenPair second = registerUser(secondEmail);
        addToCart(second.accessToken(), variantId, 1);

        MvcResult result = validateCoupon(second.accessToken(), code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/message").asText()).containsIgnoringCase("usage limit");
    }

    // ===== below min order amount: 200, valid=false =====

    @Test
    void validateCoupon_belowMinOrderAmount_returns200_validFalse() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate MinOrder");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, 10, 200000);
        String code = "VC-MINORDER-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String email = uniqueEmail("vc-minorder-buyer");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = validateCoupon(buyer.accessToken(), code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/message").asText()).containsIgnoringCase("minimum");
    }

    // ===== empty cart: 200, valid=false, clean message =====

    @Test
    void validateCoupon_emptyCart_returns200_validFalse_cartEmptyMessage() throws Exception {
        AdminContext ctx = setUpAdmin();
        String promotionId = createOrderScopePromotion(ctx, 10, null);
        String code = "VC-EMPTYCART-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String email = uniqueEmail("vc-emptycart-buyer");
        TokenPair buyer = registerUser(email);

        MvcResult result = validateCoupon(buyer.accessToken(), code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/message").asText()).isEqualTo("Cart is empty");
    }

    // ===== unauthenticated: 401 =====

    @Test
    void validateCoupon_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/validate-coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"ANY\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ===== blank couponCode: 422 (request-shape validation, not a business outcome) =====

    @Test
    void validateCoupon_blankCouponCode_returns422() throws Exception {
        String email = uniqueEmail("vc-blank-buyer");
        TokenPair buyer = registerUser(email);
        mockMvc.perform(post("/api/v1/checkout/validate-coupon")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ===== consistency: validate-coupon's discountAmount matches what create order actually applies =====

    @Test
    void validateCoupon_thenCreateOrder_sameDiscountAmountApplied() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate Consistency");
        String variantId = createVariant(ctx, productId, 80000);
        String promotionId = createOrderScopePromotion(ctx, 15, null);
        String code = "VC-CONSISTENT-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String email = uniqueEmail("vc-consistent-buyer");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(email);

        MvcResult validation = validateCoupon(buyer.accessToken(), code);
        double previewedDiscount = json(validation.getResponse().getContentAsString())
                .at("/data/discountAmount").asDouble();
        assertThat(previewedDiscount).isEqualTo(12000.0);

        MvcResult order = checkout(buyer.accessToken(), addressId, code);
        assertThat(order.getResponse().getStatus()).isEqualTo(201);
        double actualDiscount = json(order.getResponse().getContentAsString()).at("/data/discountAmount").asDouble();
        assertThat(actualDiscount).isEqualTo(previewedDiscount);
    }

    // ===== race: validate says valid, but coupon is exhausted by another order before create-order runs =====

    @Test
    void validateCoupon_passesButCouponExhaustedBeforeCreateOrder_createOrderFailsCleanly_not500() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Validate Race");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, 10, null);
        String code = "VC-RACE-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"usageLimit\":1"));

        String slowEmail = uniqueEmail("vc-race-slow");
        TokenPair slow = registerUser(slowEmail);
        addToCart(slow.accessToken(), variantId, 1);
        String slowAddress = createAddressForUser(slowEmail);

        // Validate passes while the coupon still has its one usage slot free.
        MvcResult validation = validateCoupon(slow.accessToken(), code);
        assertThat(json(validation.getResponse().getContentAsString()).at("/data/valid").asBoolean()).isTrue();

        // Another customer claims the only usage slot in the meantime.
        String fastEmail = uniqueEmail("vc-race-fast");
        TokenPair fast = registerUser(fastEmail);
        addToCart(fast.accessToken(), variantId, 1);
        String fastAddress = createAddressForUser(fastEmail);
        assertThat(checkout(fast.accessToken(), fastAddress, code).getResponse().getStatus()).isEqualTo(201);

        // The slow customer's create-order, using the now-exhausted coupon, must
        // fail with a clean business error - never a 500 - and create no order.
        MvcResult result = checkout(slow.accessToken(), slowAddress, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(json(result.getResponse().getContentAsString()).at("/message").asText())
                .containsIgnoringCase("usage limit");
    }
}
