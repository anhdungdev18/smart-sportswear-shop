package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.Duration;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Component
public class VisualSearchClient {

    private final VisualSearchProperties properties;
    private final RestClient restClient;

    public VisualSearchClient(VisualSearchProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, properties.requestTimeoutSeconds()));
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .baseUrl(properties.serviceUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public List<VisualSearchCandidate> search(MultipartFile image, int limit) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() == null ? "query-image" : image.getOriginalFilename();
                }
            });
            VisualSearchInternalResponse response = restClient.post()
                    .uri(uri -> uri.path("/internal/v1/search").queryParam("limit", limit).build())
                    .header("X-Internal-Service-Token", properties.internalToken())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(VisualSearchInternalResponse.class);
            return response == null || response.candidates() == null ? List.of() : response.candidates();
        } catch (RestClientResponseException ex) {
            HttpStatus status = ex.getStatusCode().is4xxClientError()
                    ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.SERVICE_UNAVAILABLE;
            throw new BusinessRuleException(status, "Visual search request failed");
        } catch (RestClientException | java.io.IOException ex) {
            throw new BusinessRuleException(HttpStatus.SERVICE_UNAVAILABLE, "Visual search is temporarily unavailable");
        }
    }

    public boolean isReady() {
        if (!properties.enabled()) return false;
        try {
            restClient.get().uri("/health/ready").retrieve().toBodilessEntity();
            return true;
        } catch (RestClientException ex) {
            return false;
        }
    }

    public <T> T getAdmin(String path, Class<T> responseType) {
        return executeAdmin(path, responseType, null, false);
    }

    public <T> T postAdmin(String path, Class<T> responseType) {
        return executeAdmin(path, responseType, null, true);
    }

    public <T> T postAdmin(String path, Object body, Class<T> responseType) {
        return executeAdmin(path, responseType, body, true);
    }

    private <T> T executeAdmin(String path, Class<T> responseType, Object body, boolean post) {
        try {
            RestClient.RequestHeadersSpec<?> request;
            if (body != null) {
                ObjectMapper mapper = new ObjectMapper()
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
                request = restClient.post().uri(path)
                        .header("X-Internal-Service-Token", properties.internalToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(body));
            } else if (post) {
                request = restClient.post().uri(path).header("X-Internal-Service-Token", properties.internalToken());
            } else {
                request = restClient.get().uri(path).header("X-Internal-Service-Token", properties.internalToken());
            }
            T response = request.retrieve().body(responseType);
            if (response == null) {
                throw new BusinessRuleException(HttpStatus.SERVICE_UNAVAILABLE, "Visual search returned an empty response");
            }
            return response;
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new BusinessRuleException(HttpStatus.SERVICE_UNAVAILABLE, "Visual search is temporarily unavailable");
        }
    }
}
