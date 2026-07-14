package com.dunghaiquyen.ecommerce;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Shared base for Phase C integration tests: real Spring context, real
 * Spring Security filter chain (via MockMvc + spring-security-test, not a
 * @WebMvcTest slice), real Postgres - no service-layer mocking.
 *
 * Database: the docker-compose postgres service (localhost:5434, same
 * postgres:16-alpine image as docker-compose.yml; see application-test.yml).
 * Flyway runs the real V1 migration against it on context startup, same as
 * the dev profile.
 *
 * Originally this used Testcontainers to spin up Postgres per run, which is
 * the more hermetic/self-contained choice. Switched to the long-lived
 * docker-compose container after Testcontainers proved unreliable in this
 * environment: running a single test class in isolation always passed, but
 * running a second class in the same JVM consistently got "Connection
 * refused" against the container's mapped port (verified, not a one-off) -
 * looks like a Docker Desktop stability issue on this machine rather than a
 * test-code bug. Precondition for running this suite: `docker compose up -d
 * postgres` (the same step local dev already requires).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AbstractIntegrationTest.NoCacheTestConfiguration.class)
public abstract class AbstractIntegrationTest {

    @TestConfiguration
    static class NoCacheTestConfiguration {

        @Bean
        @Primary
        CacheManager testCacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    protected String uniqueEmail(String prefix) {
        return prefix + "-" + java.util.UUID.randomUUID() + "@example.com";
    }

    protected JsonNode json(String content) throws Exception {
        return objectMapper.readTree(content);
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }

    /** Registers a fresh user (default password "Password123") and returns its issued token pair. */
    protected TokenPair registerUser(String email) throws Exception {
        String body = """
                {"fullName":"Test User","email":"%s","password":"Password123"}
                """.formatted(email);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = json(result.getResponse().getContentAsString());
        return new TokenPair(
                node.at("/data/tokens/accessToken").asText(),
                node.at("/data/tokens/refreshToken").asText());
    }

    /**
     * Registers a normal CUSTOMER (the only role register can ever create),
     * promotes it to ADMIN directly via the repository (no admin-creation
     * endpoint exists), then logs in again so the issued access token's "role"
     * claim reflects ADMIN - the claim is fixed at issuance time, so reusing
     * the original registration token would still say CUSTOMER.
     */
    protected String registerAdminAndGetAccessToken(String email) throws Exception {
        registerUser(email);
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        String body = """
                {"email":"%s","password":"Password123"}
                """.formatted(email);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return json(result.getResponse().getContentAsString()).at("/data/tokens/accessToken").asText();
    }
}
