package com.dunghaiquyen.ecommerce.modules.brand;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Lighter than CategoryIntegrationTest on purpose: BrandService/AdminBrandController
 * are a structurally identical implementation of the same pattern (slug
 * uniqueness, ADMIN-only, ACTIVE-only public list) already exercised in full
 * there. This only confirms brand's own wiring isn't broken, not every shared
 * validation rule again.
 */
class BrandIntegrationTest extends AbstractIntegrationTest {

    @Test
    void create_asAdmin_succeeds() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("brand-admin"));
        String slug = "brand-" + java.util.UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nike\",\"slug\":\"" + slug + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slug").value(slug));
    }

    @Test
    void create_duplicateSlug_returns409() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("brand-dup-admin"));
        String slug = "brand-dup-" + java.util.UUID.randomUUID();
        String body = "{\"name\":\"Nike\",\"slug\":\"" + slug + "\"}";

        mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void update_blankName_returns422() throws Exception {
        String token = registerAdminAndGetAccessToken(uniqueEmail("brand-blank-admin"));
        String slug = "brand-blank-" + java.util.UUID.randomUUID();
        var created = mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nike\",\"slug\":\"" + slug + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String id = json(created.getResponse().getContentAsString()).at("/data/id").asText();

        mockMvc.perform(patch("/api/v1/admin/brands/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }
}
