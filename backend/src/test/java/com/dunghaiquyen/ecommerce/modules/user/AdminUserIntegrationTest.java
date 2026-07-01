package com.dunghaiquyen.ecommerce.modules.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AdminUserIntegrationTest extends AbstractIntegrationTest {

    private record AdminContext(String token, UUID adminId) {
    }

    private AdminContext setUpAdmin(String email) throws Exception {
        String token = registerAdminAndGetAccessToken(email);
        UUID adminId = userRepository.findByEmail(email).orElseThrow().getId();
        return new AdminContext(token, adminId);
    }

    private String registerCustomerAndGetId(String email, String fullName) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + fullName + "\",\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated());
        return userRepository.findByEmail(email).orElseThrow().getId().toString();
    }

    // ===== list =====

    @Test
    void adminListUsers_success_isPaginated() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-list-admin"));
        registerCustomerAndGetId(uniqueEmail("ausr-list-1"), "List One");
        registerCustomerAndGetId(uniqueEmail("ausr-list-2"), "List Two");

        MvcResult result = mockMvc.perform(get("/api/v1/admin/users?limit=1&page=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data")).hasSize(1);
        assertThat(body.at("/meta/limit").asInt()).isEqualTo(1);
        assertThat(body.at("/meta/total").asInt()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void adminListUsers_filtersByKeywordRoleAndStatus() throws Exception {
        String adminEmail = uniqueEmail("ausr-filter-admin");
        AdminContext ctx = setUpAdmin(adminEmail);
        String targetEmail = uniqueEmail("ausr-filter-target");
        String uniqueNameKeyword = "Findable-" + UUID.randomUUID();
        String targetId = registerCustomerAndGetId(targetEmail, uniqueNameKeyword + " Person");
        registerCustomerAndGetId(uniqueEmail("ausr-filter-other"), "Someone Else");

        MvcResult byKeyword = mockMvc.perform(get("/api/v1/admin/users?keyword=" + uniqueNameKeyword)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andReturn();
        JsonNode keywordData = json(byKeyword.getResponse().getContentAsString()).at("/data");
        assertThat(keywordData).hasSize(1);
        assertThat(keywordData.get(0).at("/id").asText()).isEqualTo(targetId);

        MvcResult byEmailKeyword = mockMvc.perform(get("/api/v1/admin/users?keyword=" + targetEmail)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andReturn();
        assertThat(json(byEmailKeyword.getResponse().getContentAsString()).at("/data")).hasSize(1);

        MvcResult byRole = mockMvc.perform(get("/api/v1/admin/users?role=ADMIN&keyword=" + adminEmail)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andReturn();
        JsonNode roleData = json(byRole.getResponse().getContentAsString()).at("/data");
        assertThat(roleData).hasSize(1);
        assertThat(roleData.get(0).at("/id").asText()).isEqualTo(ctx.adminId().toString());

        MvcResult byStatus = mockMvc.perform(get("/api/v1/admin/users?status=ACTIVE&keyword=" + targetEmail)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andReturn();
        assertThat(json(byStatus.getResponse().getContentAsString()).at("/data")).hasSize(1);
    }

    // ===== detail =====

    @Test
    void adminGetUserDetail_success() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-detail-admin"));
        String targetEmail = uniqueEmail("ausr-detail-target");
        String targetId = registerCustomerAndGetId(targetEmail, "Detail Target");

        MvcResult result = mockMvc.perform(get("/api/v1/admin/users/" + targetId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result.getResponse().getContentAsString());
        assertThat(body.at("/data/id").asText()).isEqualTo(targetId);
        assertThat(body.at("/data/email").asText()).isEqualTo(targetEmail);
        assertThat(body.at("/data/fullName").asText()).isEqualTo("Detail Target");
        assertThat(body.at("/data/role").asText()).isEqualTo("CUSTOMER");
        assertThat(body.at("/data/status").asText()).isEqualTo("ACTIVE");
        assertThat(body.at("/data/createdAt").asText()).isNotBlank();
    }

    @Test
    void adminGetUserDetail_unknownId_returns404() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-404-admin"));
        mockMvc.perform(get("/api/v1/admin/users/" + UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminUpdateStatus_unknownId_returns404() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-404-status-admin"));
        mockMvc.perform(patch("/api/v1/admin/users/" + UUID.randomUUID() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\"}"))
                .andExpect(status().isNotFound());
    }

    // ===== role gate =====

    @Test
    void nonAdmin_getsForbidden_onAdminUserEndpoints() throws Exception {
        TokenPair customer = registerUser(uniqueEmail("ausr-notadmin"));
        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customer.accessToken()))
                .andExpect(status().isForbidden());
    }

    // ===== lock / unlock =====

    @Test
    void lockUser_success_thenLoginIsBlocked() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-lock-admin"));
        String targetEmail = uniqueEmail("ausr-lock-target");
        String targetId = registerCustomerAndGetId(targetEmail, "Lock Target");

        MvcResult lockResult = mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\"}"))
                .andReturn();
        assertThat(lockResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(lockResult.getResponse().getContentAsString()).at("/data/status").asText())
                .isEqualTo("LOCKED");

        var lockedUser = userRepository.findById(UUID.fromString(targetId)).orElseThrow();
        assertThat(lockedUser.getStatus().name()).isEqualTo("LOCKED");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + targetEmail + "\",\"password\":\"Password123\"}"))
                .andReturn();
        assertThat(loginResult.getResponse().getStatus()).isEqualTo(403);
        assertThat(json(loginResult.getResponse().getContentAsString()).at("/message").asText())
                .isEqualTo("Account is locked");
    }

    @Test
    void lockUser_existingAccessToken_isRejectedNotBypassed() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-lock-token-admin"));
        String targetEmail = uniqueEmail("ausr-lock-token-target");
        TokenPair target = registerUser(targetEmail);
        String targetId = userRepository.findByEmail(targetEmail).orElseThrow().getId().toString();

        // Sanity: the token works before the lock.
        mockMvc.perform(get("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + target.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\"}"))
                .andExpect(status().isOk());

        // Same OLD access token, never re-issued - JwtAuthenticationFilter checks
        // ACTIVE status on every request, not just at login, so this must now be
        // treated as unauthenticated (401), never silently let through.
        MvcResult afterLock = mockMvc.perform(
                        get("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + target.accessToken()))
                .andReturn();
        assertThat(afterLock.getResponse().getStatus())
                .as("a locked account's pre-existing access token must not bypass the lock")
                .isEqualTo(401);
    }

    @Test
    void unlockUser_success_thenLoginWorksAgain() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-unlock-admin"));
        String targetEmail = uniqueEmail("ausr-unlock-target");
        String targetId = registerCustomerAndGetId(targetEmail, "Unlock Target");

        mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\"}"))
                .andExpect(status().isOk());

        MvcResult unlockResult = mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andReturn();
        assertThat(unlockResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(unlockResult.getResponse().getContentAsString()).at("/data/status").asText())
                .isEqualTo("ACTIVE");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + targetEmail + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_sameStatusAsCurrent_isIdempotentNoOp() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-idem-status-admin"));
        String targetId = registerCustomerAndGetId(uniqueEmail("ausr-idem-status-target"), "Idem Target");

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result.getResponse().getContentAsString()).at("/data/status").asText())
                .isEqualTo("ACTIVE");
    }

    // ===== role change =====

    @Test
    void changeRole_success() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-role-admin"));
        String targetId = registerCustomerAndGetId(uniqueEmail("ausr-role-target"), "Role Target");

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SALES_STAFF\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result.getResponse().getContentAsString()).at("/data/role").asText())
                .isEqualTo("SALES_STAFF");

        var user = userRepository.findById(UUID.fromString(targetId)).orElseThrow();
        assertThat(user.getRole().name()).isEqualTo("SALES_STAFF");
    }

    @Test
    void changeRole_sameRoleAsCurrent_isIdempotentNoOp() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-idem-role-admin"));
        String targetId = registerCustomerAndGetId(uniqueEmail("ausr-idem-role-target"), "Idem Role Target");

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"CUSTOMER\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // ===== self-action guards =====

    @Test
    void admin_cannotLockOwnAccount() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-selflock-admin"));

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/users/" + ctx.adminId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(json(result.getResponse().getContentAsString()).at("/message").asText())
                .isEqualTo("Cannot lock your own account");

        var self = userRepository.findById(ctx.adminId()).orElseThrow();
        assertThat(self.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void admin_cannotDemoteOwnRoleAwayFromAdmin() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-selfdemote-admin"));

        MvcResult result = mockMvc.perform(patch("/api/v1/admin/users/" + ctx.adminId() + "/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"CUSTOMER\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(json(result.getResponse().getContentAsString()).at("/message").asText())
                .isEqualTo("Cannot change your own role away from ADMIN");

        var self = userRepository.findById(ctx.adminId()).orElseThrow();
        assertThat(self.getRole().name()).isEqualTo("ADMIN");
    }

    @Test
    void admin_canReconfirmOwnRoleAsAdmin_withoutTriggeringGuard() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-selfconfirm-admin"));

        mockMvc.perform(patch("/api/v1/admin/users/" + ctx.adminId() + "/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void admin_canLockAndDemoteAnotherAdmin_selfGuardDoesNotApplyToOthers() throws Exception {
        AdminContext ctxA = setUpAdmin(uniqueEmail("ausr-othersa"));
        AdminContext ctxB = setUpAdmin(uniqueEmail("ausr-othersb"));

        mockMvc.perform(patch("/api/v1/admin/users/" + ctxB.adminId() + "/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctxA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SALES_STAFF\"}"))
                .andExpect(status().isOk());

        var userB = userRepository.findById(ctxB.adminId()).orElseThrow();
        assertThat(userB.getRole().name()).isEqualTo("SALES_STAFF");
    }

    // ===== regression: concurrent status + role updates on the SAME target must not lose either change =====

    @Test
    void concurrentStatusAndRoleUpdate_onSameUser_neitherChangeIsLost() throws Exception {
        AdminContext ctx = setUpAdmin(uniqueEmail("ausr-race-admin"));
        String targetId = registerCustomerAndGetId(uniqueEmail("ausr-race-target"), "Race Target");

        Callable<Integer> lockCall = () -> mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\"}"))
                .andReturn()
                .getResponse()
                .getStatus();
        Callable<Integer> roleCall = () -> mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ctx.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SALES_STAFF\"}"))
                .andReturn()
                .getResponse()
                .getStatus();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Integer>> results = pool.invokeAll(List.of(lockCall, roleCall));
        pool.shutdown();

        for (Future<Integer> f : results) {
            assertThat(f.get()).as("neither concurrent update may fail or 500").isEqualTo(200);
        }

        var user = userRepository.findById(UUID.fromString(targetId)).orElseThrow();
        assertThat(user.getStatus().name())
                .as("the status change must not have been lost to the concurrent role update")
                .isEqualTo("LOCKED");
        assertThat(user.getRole().name())
                .as("the role change must not have been lost to the concurrent status update")
                .isEqualTo("SALES_STAFF");
    }
}
