package com.dunghaiquyen.ecommerce.modules.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.dunghaiquyen.ecommerce.modules.address.entity.Address;
import com.dunghaiquyen.ecommerce.modules.address.repository.AddressRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class CheckoutPreviewIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AddressRepository addressRepository;

    private record AdminContext(String token, String categoryId, String brandId) {
    }

    private AdminContext setUpAdmin() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("prev-admin"));
        String categorySlug = "prev-cat-" + UUID.randomUUID();
        MvcResult cat = mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat\",\"slug\":\"" + categorySlug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = json(cat.getResponse().getContentAsString()).at("/data/id").asText();

        String brandSlug = "prev-brand-" + UUID.randomUUID();
        MvcResult brand = mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Brand\",\"slug\":\"" + brandSlug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String brandId = json(brand.getResponse().getContentAsString()).at("/data/id").asText();

        return new AdminContext(token, categoryId, brandId);
    }

    private String createProduct(AdminContext ctx, String name, String status) throws Exception {
        String slug = "prev-prod-" + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"%s\",\"slug\":\"%s\",\"categoryId\":\"%s\",\"brandId\":\"%s\",\"status\":\"%s\"}")
                                .formatted(name, slug, ctx.categoryId(), ctx.brandId(), status)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String createVariant(AdminContext ctx, String productId, int price, int stock) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/products/" + productId + "/variants")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"sku\":\"SKU-%s\",\"size\":\"M\",\"color\":\"Black\",\"price\":%d,\"stockQuantity\":%d}")
                                .formatted(UUID.randomUUID(), price, stock)))
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

    private String createOrderScopePromotion(AdminContext ctx, int discountPercent) throws Exception {
        String slug = "prev-promo-" + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/promotions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"name\":\"Promo\",\"slug\":\"%s\",\"type\":\"PERCENTAGE\",\"scope\":\"ORDER\","
                                        + "\"status\":\"ACTIVE\",\"discountPercent\":%d}")
                                .formatted(slug, discountPercent)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String createCoupon(AdminContext ctx, String promotionId, String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"code\":\"%s\",\"promotionId\":\"%s\"}").formatted(code, promotionId)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private MvcResult preview(String token, String addressId, String couponCode) throws Exception {
        StringBuilder body = new StringBuilder("{");
        if (addressId != null) {
            body.append("\"addressId\":\"").append(addressId).append("\"");
        }
        if (couponCode != null) {
            if (body.length() > 1) {
                body.append(",");
            }
            body.append("\"couponCode\":\"").append(couponCode).append("\"");
        }
        body.append("}");
        return mockMvc.perform(post("/api/v1/checkout/preview")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andReturn();
    }

    // ===== valid cart returns correct totals =====

    @Test
    void preview_validCart_returnsCorrectSubtotalAndTotal() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview Shirt", "ACTIVE");
        String variantId = createVariant(ctx, productId, 100000, 10);

        String email = uniqueEmail("prev-valid");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 2);
        String addressId = createAddressForUser(email);

        MvcResult result = preview(buyer.accessToken(), addressId, null);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/subtotal").asDouble()).isEqualTo(200000.0);
        assertThat(body.at("/data/discountAmount").asDouble()).isEqualTo(0.0);
        assertThat(body.at("/data/totalAmount").asDouble()).isEqualTo(200000.0);
        assertThat(body.at("/data/canCheckout").asBoolean()).isTrue();
        assertThat(body.at("/data/items")).hasSize(1);
        assertThat(body.at("/data/items/0/valid").asBoolean()).isTrue();
    }

    // ===== valid coupon =====

    @Test
    void preview_validCoupon_appliesDiscount() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview Coupon Shirt", "ACTIVE");
        String variantId = createVariant(ctx, productId, 100000, 10);
        String promotionId = createOrderScopePromotion(ctx, 10);
        String code = "PREV10-" + UUID.randomUUID();
        createCoupon(ctx, promotionId, code);

        String email = uniqueEmail("prev-coupon-ok");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = preview(buyer.accessToken(), null, code);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/discountAmount").asDouble()).isEqualTo(10000.0);
        assertThat(body.at("/data/totalAmount").asDouble()).isEqualTo(90000.0);
        assertThat(body.at("/data/appliedCoupon/code").asText()).isEqualTo(code.toUpperCase());
        assertThat(body.at("/data/couponError").isNull()).isTrue();
        assertThat(body.at("/data/canCheckout").asBoolean()).isTrue();
    }

    // ===== invalid coupon =====

    @Test
    void preview_invalidCoupon_returnsCleanCouponError_butStillShowsTotals() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview BadCoupon Shirt", "ACTIVE");
        String variantId = createVariant(ctx, productId, 100000, 10);

        String email = uniqueEmail("prev-coupon-bad");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = preview(buyer.accessToken(), null, "DOES-NOT-EXIST-" + UUID.randomUUID());
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/couponError").asText()).isNotBlank();
        assertThat(body.at("/data/appliedCoupon").isNull()).isTrue();
        assertThat(body.at("/data/canCheckout").asBoolean()).isTrue();
        assertThat(body.at("/data/subtotal").asDouble()).isEqualTo(100000.0);
    }

    // ===== item exceeding stock is flagged invalid =====

    @Test
    void preview_itemExceedsStock_flaggedInvalid_canCheckoutFalse() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview LowStock", "ACTIVE");
        String variantId = createVariant(ctx, productId, 50000, 2);

        String email = uniqueEmail("prev-lowstock");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 2);

        // Someone else's purchase reserves the only stock left.
        String otherEmail = uniqueEmail("prev-lowstock-other");
        TokenPair other = registerUser(otherEmail);
        addToCart(other.accessToken(), variantId, 2);
        String otherAddressId = createAddressForUser(otherEmail);
        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + other.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":\"" + otherAddressId + "\",\"paymentMethod\":\"COD\"}"))
                .andExpect(status().isCreated());

        MvcResult result = preview(buyer.accessToken(), null, null);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/canCheckout").asBoolean()).isFalse();
        assertThat(body.at("/data/items/0/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/items/0/errorMessage").asText()).contains("Insufficient stock");
        assertThat(body.at("/data/subtotal").asDouble()).isEqualTo(0.0);
    }

    // ===== inactive variant flagged invalid =====

    @Test
    void preview_inactiveVariant_flaggedInvalid() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview Inactive Variant", "ACTIVE");
        String variantId = createVariant(ctx, productId, 50000, 10);

        String email = uniqueEmail("prev-inactive-variant");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        mockMvc.perform(patch("/api/v1/admin/variants/" + variantId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        MvcResult result = preview(buyer.accessToken(), null, null);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/canCheckout").asBoolean()).isFalse();
        assertThat(body.at("/data/items/0/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/items/0/errorMessage").asText()).contains("no longer available");
    }

    // ===== draft product flagged invalid =====

    @Test
    void preview_draftProduct_flaggedInvalid() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview Draft Product", "ACTIVE");
        String variantId = createVariant(ctx, productId, 50000, 10);

        String email = uniqueEmail("prev-draft-product");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);

        mockMvc.perform(patch("/api/v1/admin/products/" + productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk());

        MvcResult result = preview(buyer.accessToken(), null, null);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/canCheckout").asBoolean()).isFalse();
        assertThat(body.at("/data/items/0/valid").asBoolean()).isFalse();
        assertThat(body.at("/data/items/0/errorMessage").asText()).contains("no longer available");
    }

    // ===== preview does not mutate the cart =====

    @Test
    void preview_doesNotMutateCart_checkoutStillWorksAfterward() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview NoMutate", "ACTIVE");
        String variantId = createVariant(ctx, productId, 80000, 10);

        String email = uniqueEmail("prev-no-mutate");
        TokenPair buyer = registerUser(email);
        addToCart(buyer.accessToken(), variantId, 1);
        String addressId = createAddressForUser(email);

        preview(buyer.accessToken(), addressId, null);
        preview(buyer.accessToken(), addressId, null);

        MvcResult cart = mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyer.accessToken()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode cartBody = json(cart.getResponse().getContentAsString());
        assertThat(cartBody.at("/data/items")).hasSize(1);
        assertThat(cartBody.at("/data/items/0/quantity").asInt()).isEqualTo(1);

        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":\"" + addressId + "\",\"paymentMethod\":\"COD\"}"))
                .andExpect(status().isCreated());
    }

    // ===== empty cart -> same error as createOrderFromCart =====

    @Test
    void preview_emptyCart_returns422_sameAsCheckout() throws Exception {
        TokenPair buyer = registerUser(uniqueEmail("prev-empty-cart"));

        MvcResult result = preview(buyer.accessToken(), null, null);
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(json(result.getResponse().getContentAsString()).at("/message").asText())
                .isEqualTo("Cart is empty");
    }

    // ===== address not owned by caller -> 404 =====

    @Test
    void preview_addressNotOwnedByCaller_returns404() throws Exception {
        AdminContext ctx = setUpAdmin();
        String productId = createProduct(ctx, "Preview AddressOwnership", "ACTIVE");
        String variantId = createVariant(ctx, productId, 50000, 10);

        String ownerEmail = uniqueEmail("prev-address-owner");
        registerUser(ownerEmail);
        String addressId = createAddressForUser(ownerEmail);

        String buyerEmail = uniqueEmail("prev-address-buyer");
        TokenPair buyer = registerUser(buyerEmail);
        addToCart(buyer.accessToken(), variantId, 1);

        MvcResult result = preview(buyer.accessToken(), addressId, null);
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    // ===== non-customer (no role) cannot preview - mirrors POST /orders restriction =====

    @Test
    void preview_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
