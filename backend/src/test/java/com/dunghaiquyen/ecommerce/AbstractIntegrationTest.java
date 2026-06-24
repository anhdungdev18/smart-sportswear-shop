package com.dunghaiquyen.ecommerce;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Shared base for Phase C integration tests: real Spring context, real
 * Spring Security filter chain (via MockMvc + spring-security-test, not a
 * @WebMvcTest slice), real Postgres - no service-layer mocking.
 *
 * Database: the docker-compose postgres service (localhost:5433, same
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
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

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
}
