package com.dunghaiquyen.ecommerce.modules.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.dunghaiquyen.ecommerce.modules.address.entity.Address;
import com.dunghaiquyen.ecommerce.modules.address.repository.AddressRepository;
import com.dunghaiquyen.ecommerce.modules.coupon.repository.CouponRepository;
import com.dunghaiquyen.ecommerce.modules.coupon.repository.CouponUsageRepository;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.PromotionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class CouponCheckoutIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    private record AdminContext(String token, String categoryId, String brandId) {
    }

    private AdminContext setUpAdmin() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("ck-admin"));
        String categorySlug = "ck-cat-" + UUID.randomUUID();
        MvcResult cat = mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat\",\"slug\":\"" + categorySlug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = json(cat.getResponse().getContentAsString()).at("/data/id").asText();

        String brandSlug = "ck-brand-" + UUID.randomUUID();
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
        String slug = "ck-prod-" + UUID.randomUUID();
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

    private String createOrderScopePromotion(
            AdminContext ctx, String type, String discountField, int discountValue, Integer minOrderAmount, Integer maxDiscount)
            throws Exception {
        String slug = "ck-promo-" + UUID.randomUUID();
        StringBuilder json = new StringBuilder(
                ("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"%s\",\"scope\":\"ORDER\",\"status\":\"ACTIVE\",\"%s\":%d")
                        .formatted(slug, type, discountField, discountValue));
        if (minOrderAmount != null) {
            json.append(",\"minOrderAmount\":").append(minOrderAmount);
        }
        if (maxDiscount != null) {
            json.append(",\"maxDiscountAmount\":").append(maxDiscount);
        }
        json.append("}");

        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
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

    private MvcResult checkout(String buyerToken, String addressId, String couponCode) throws Exception {
        String couponField = couponCode != null ? ",\"couponCode\":\"" + couponCode + "\"" : "";
        return mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":\"" + addressId + "\",\"paymentMethod\":\"COD\"" + couponField + "}"))
                .andReturn();
    }

    // ===== valid coupon applies successfully, discount computed correctly =====

    @Test
    void checkout_withValidPercentageCoupon_appliesDiscount() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Shirt");
        String variantId = createVariant(ctx, productId, 100000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, null, null);
        String code = "VALID-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String buyerEmail = uniqueEmail("ck-valid-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/subtotalAmount").asDouble()).isEqualTo(100000.0);
        assertThat(body.at("/data/discountAmount").asDouble()).isEqualTo(10000.0);
        assertThat(body.at("/data/totalAmount").asDouble()).isEqualTo(90000.0);
    }

    // ===== expired coupon rejected =====

    @Test
    void checkout_withExpiredCoupon_returns422() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Expired");
        String variantId = createVariant(ctx, productId, 100000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, null, null);
        String code = "EXPIRED-" + UUID.randomUUID();
        String pastDate = Instant.now().minus(10, ChronoUnit.DAYS).toString();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"endsAt\":\"" + pastDate + "\""));

        String buyerEmail = uniqueEmail("ck-expired-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== inactive coupon rejected =====

    @Test
    void checkout_withInactiveCoupon_returns422() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Inactive");
        String variantId = createVariant(ctx, productId, 100000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, null, null);
        String code = "INACTIVE-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"status\":\"INACTIVE\""));

        String buyerEmail = uniqueEmail("ck-inactive-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== inactive promotion blocks checkout even if the coupon itself is active =====

    @Test
    void checkout_withInactivePromotion_returns422() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Inactive Promotion");
        String variantId = createVariant(ctx, productId, 100000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, null, null);
        String code = "PROMO-INACTIVE-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());
        mockMvc.perform(patch("/api/v1/admin/promotions/" + promotionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        String buyerEmail = uniqueEmail("ck-inactive-promotion-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== usage_limit exceeded rejected on the next attempt =====

    @Test
    void checkout_pastUsageLimit_returns422OnSecondAttempt() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Limited");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, null, null);
        String code = "LIMIT1-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"usageLimit\":1"));

        String firstEmail = uniqueEmail("ck-limit-first");
        TokenPair first = registerUser(firstEmail);
        addToCart(first.accessToken(), variantId, 1);
        String firstAddress = createAddressForUser(firstEmail);
        assertThat(checkout(first.accessToken(), firstAddress, code).getResponse().getStatus()).isEqualTo(201);

        String secondEmail = uniqueEmail("ck-limit-second");
        TokenPair second = registerUser(secondEmail);
        addToCart(second.accessToken(), variantId, 1);
        String secondAddress = createAddressForUser(secondEmail);
        MvcResult result = checkout(second.accessToken(), secondAddress, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== per-user limit exceeded on a second order by the SAME user =====

    @Test
    void checkout_pastPerUserLimit_returns422OnSecondOrderBySameUser() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon PerUser");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, null, null);
        String code = "PERUSER1-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder(",\"perUserLimit\":1"));

        String buyerEmail = uniqueEmail("ck-peruser-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        String addressId = createAddressForUser(buyerEmail);

        addToCart(buyer.accessToken(), variantId, 1);
        assertThat(checkout(buyer.accessToken(), addressId, code).getResponse().getStatus()).isEqualTo(201);

        addToCart(buyer.accessToken(), variantId, 1);
        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== minimum order amount not met rejected =====

    @Test
    void checkout_belowMinOrderAmount_returns422() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon MinOrder");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 10, 200000, null);
        String code = "MINORDER-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String buyerEmail = uniqueEmail("ck-minorder-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== discount never exceeds order total (huge fixed-amount coupon is capped) =====

    @Test
    void checkout_hugeFixedAmountCoupon_discountCappedAtSubtotal_totalNeverNegative() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon HugeFixed");
        String variantId = createVariant(ctx, productId, 50000);
        String promotionId = createOrderScopePromotion(ctx, "FIXED_AMOUNT", "discountAmount", 999999, null, null);
        String code = "HUGEFIXED-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code, new StringBuilder());

        String buyerEmail = uniqueEmail("ck-hugefixed-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/discountAmount").asDouble()).isEqualTo(50000.0);
        assertThat(body.at("/data/totalAmount").asDouble()).isEqualTo(0.0);
    }

    // ===== invalid coupon code rejected, no order created =====

    @Test
    void checkout_unknownCouponCode_returns422_createsNoOrder() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Unknown");
        String variantId = createVariant(ctx, productId, 50000);

        String buyerEmail = uniqueEmail("ck-unknown-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, "DOES-NOT-EXIST-" + UUID.randomUUID());
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    // ===== usage recorded after successful checkout: usage row + usageCount incremented =====

    @Test
    void checkout_withValidCoupon_recordsUsageAndIncrementsCount() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createActiveProduct(ctx, "Coupon Usage");
        String variantId = createVariant(ctx, productId, 80000);
        String promotionId = createOrderScopePromotion(ctx, "PERCENTAGE", "discountPercent", 20, null, null);
        String code = "USAGE-" + UUID.randomUUID();
        String couponId = createCoupon(ctx, promotionId, code, new StringBuilder());

        String buyerEmail = uniqueEmail("ck-usage-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(buyerEmail);

        MvcResult result = checkout(buyer.accessToken(), addressId, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        String orderId = json(result.getResponse().getContentAsString()).at("/data/id").asText();

        var coupon = couponRepository.findById(UUID.fromString(couponId)).orElseThrow();
        assertThat(coupon.getUsageCount()).isEqualTo(1);
        assertThat(promotionRepository.findById(UUID.fromString(promotionId)).orElseThrow().getUsageCount())
                .isEqualTo(1);

        var usages = couponUsageRepository.findAll().stream()
                .filter(u -> u.getOrder().getId().equals(UUID.fromString(orderId)))
                .toList();
        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).getDiscountAmount().doubleValue()).isEqualTo(16000.0);
    }
}
