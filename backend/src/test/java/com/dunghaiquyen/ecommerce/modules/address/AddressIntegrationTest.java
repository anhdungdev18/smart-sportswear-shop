package com.dunghaiquyen.ecommerce.modules.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AddressIntegrationTest extends AbstractIntegrationTest {

    private String addressBody(String receiver, boolean isDefault) {
        return ("{\"receiverName\":\"%s\",\"phone\":\"0900000001\",\"province\":\"HCM\",\"district\":\"District 1\","
                        + "\"ward\":\"Ward 1\",\"addressLine\":\"123 Test Street\",\"isDefault\":%s}")
                .formatted(receiver, isDefault);
    }

    private String createAddress(String token, String receiver, boolean isDefault) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody(receiver, isDefault)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    // ===== list own addresses =====

    @Test
    void listMine_returnsOnlyOwnAddresses() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-list"));
        createAddress(user.accessToken(), "Receiver A", false);
        createAddress(user.accessToken(), "Receiver B", false);

        TokenPair other = registerUser(uniqueEmail("addr-list-other"));
        createAddress(other.accessToken(), "Receiver Other", false);

        MvcResult result = mockMvc.perform(get("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data")).hasSize(2);
        for (JsonNode item : body.at("/data")) {
            assertThat(item.at("/receiverName").asText()).startsWith("Receiver ");
            assertThat(item.at("/receiverName").asText()).isNotEqualTo("Receiver Other");
        }
    }

    // ===== create =====

    @Test
    void create_succeeds_andDefaultsToNotDefaultUnlessRequested() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-create"));

        MvcResult result = mockMvc.perform(post("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"receiverName\":\"John\",\"phone\":\"0900000002\",\"province\":\"HCM\","
                                + "\"district\":\"D1\",\"ward\":\"W1\",\"addressLine\":\"1 Main St\"}")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/receiverName").asText()).isEqualTo("John");
        assertThat(body.at("/data/isDefault").asBoolean()).isFalse();
    }

    @Test
    void create_invalidPhone_returns422() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-badphone"));

        mockMvc.perform(post("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverName\":\"John\",\"phone\":\"abc\",\"province\":\"HCM\","
                                + "\"district\":\"D1\",\"ward\":\"W1\",\"addressLine\":\"1 Main St\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ===== set default unsets others =====

    @Test
    void create_withIsDefaultTrue_unsetsPreviousDefault() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-default-create"));
        String firstId = createAddress(user.accessToken(), "First", true);
        String secondId = createAddress(user.accessToken(), "Second", true);

        MvcResult result = mockMvc.perform(get("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result.getResponse().getContentAsString());
        boolean firstDefault = false;
        boolean secondDefault = false;
        int defaultCount = 0;
        for (JsonNode item : body.at("/data")) {
            if (item.at("/isDefault").asBoolean()) {
                defaultCount++;
            }
            if (item.at("/id").asText().equals(firstId)) {
                firstDefault = item.at("/isDefault").asBoolean();
            }
            if (item.at("/id").asText().equals(secondId)) {
                secondDefault = item.at("/isDefault").asBoolean();
            }
        }
        assertThat(defaultCount).isEqualTo(1);
        assertThat(firstDefault).isFalse();
        assertThat(secondDefault).isTrue();
    }

    @Test
    void setDefault_unsetsOtherAddressesDefault() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-setdefault"));
        String firstId = createAddress(user.accessToken(), "First", true);
        String secondId = createAddress(user.accessToken(), "Second", false);

        mockMvc.perform(patch("/api/v1/me/addresses/" + secondId + "/default")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.isDefault").value(true));

        MvcResult result = mockMvc.perform(get("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result.getResponse().getContentAsString());
        for (JsonNode item : body.at("/data")) {
            if (item.at("/id").asText().equals(firstId)) {
                assertThat(item.at("/isDefault").asBoolean()).isFalse();
            }
            if (item.at("/id").asText().equals(secondId)) {
                assertThat(item.at("/isDefault").asBoolean()).isTrue();
            }
        }
    }

    // ===== update =====

    @Test
    void update_partialFields_leavesOthersUnchanged() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-update"));
        String addressId = createAddress(user.accessToken(), "Original Name", false);

        MvcResult result = mockMvc.perform(patch("/api/v1/me/addresses/" + addressId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressLine\":\"456 New Street\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/receiverName").asText()).isEqualTo("Original Name");
        assertThat(body.at("/data/addressLine").asText()).isEqualTo("456 New Street");
    }

    @Test
    void update_blankReceiverName_returns422() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-update-blank"));
        String addressId = createAddress(user.accessToken(), "Original", false);

        mockMvc.perform(patch("/api/v1/me/addresses/" + addressId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverName\":\"   \"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ===== delete =====

    @Test
    void delete_success_removesAddress() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-delete"));
        String addressId = createAddress(user.accessToken(), "ToDelete", false);

        mockMvc.perform(delete("/api/v1/me/addresses/" + addressId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(result.getResponse().getContentAsString()).at("/data")).isEmpty();
    }

    @Test
    void delete_defaultAddress_doesNotAutoPromoteAnotherToDefault() throws Exception {
        TokenPair user = registerUser(uniqueEmail("addr-delete-default"));
        String defaultId = createAddress(user.accessToken(), "DefaultOne", true);
        createAddress(user.accessToken(), "OtherOne", false);

        mockMvc.perform(delete("/api/v1/me/addresses/" + defaultId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/v1/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data")).hasSize(1);
        assertThat(body.at("/data/0/isDefault").asBoolean())
                .as("deleting the default must leave the remaining address as non-default, per the self-chosen rule")
                .isFalse();
    }

    // ===== ownership: cannot touch another user's address =====

    @Test
    void update_anotherUsersAddress_returns404() throws Exception {
        TokenPair owner = registerUser(uniqueEmail("addr-owner"));
        String addressId = createAddress(owner.accessToken(), "Owner Address", false);

        TokenPair stranger = registerUser(uniqueEmail("addr-stranger"));
        mockMvc.perform(patch("/api/v1/me/addresses/" + addressId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + stranger.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressLine\":\"Hacked\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_anotherUsersAddress_returns404() throws Exception {
        TokenPair owner = registerUser(uniqueEmail("addr-owner-del"));
        String addressId = createAddress(owner.accessToken(), "Owner Address", false);

        TokenPair stranger = registerUser(uniqueEmail("addr-stranger-del"));
        mockMvc.perform(delete("/api/v1/me/addresses/" + addressId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + stranger.accessToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void setDefault_anotherUsersAddress_returns404() throws Exception {
        TokenPair owner = registerUser(uniqueEmail("addr-owner-default"));
        String addressId = createAddress(owner.accessToken(), "Owner Address", false);

        TokenPair stranger = registerUser(uniqueEmail("addr-stranger-default"));
        mockMvc.perform(patch("/api/v1/me/addresses/" + addressId + "/default")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + stranger.accessToken()))
                .andExpect(status().isNotFound());
    }
}
