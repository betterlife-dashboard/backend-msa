package com.betterlife.gateway.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class ApiDocsAggregator {

    private final WebClient webClient = WebClient.builder().build();
    private final ApiServicesProperties properties;

    @GetMapping("/v3/api-docs")
    public Mono<String> aggregate() {

        List<Mono<String>> monos = properties.getServices().stream()
                .map(s -> webClient.get()
                        .uri(s.getUrl() + "/v3/api-docs")
                        .retrieve()
                        .bodyToMono(String.class))
                .toList();

        return Mono.zip(monos, results -> {
            List<String> docs = Arrays.stream(results)
                    .map(result -> (String) result)
                    .toList();
            return mergeOpenApiDocs(docs);
        });
    }

    private String mergeOpenApiDocs(List<String> docs) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode root = mapper.createObjectNode();

            // 기본 OpenAPI 정보
            root.put("openapi", "3.0.1");

            ObjectNode info = mapper.createObjectNode();
            info.put("title", "Gateway API");
            info.put("version", "1.0.0");
            info.put("description", "Aggregated API Documentation");
            root.set("info", info);

            ObjectNode paths = mapper.createObjectNode();
            ArrayNode tags = mapper.createArrayNode();
            ObjectNode schemas = mapper.createObjectNode();
            ObjectNode components = mapper.createObjectNode();

            // 합치기
            for (String doc : docs) {
                JsonNode node = mapper.readTree(doc);

                // paths
                if (node.has("paths")) {
                    node.get("paths").fields().forEachRemaining(e ->
                            paths.set(e.getKey(), e.getValue()));
                }

                // tags
                if (node.has("tags")) {
                    node.get("tags").forEach(tags::add);
                }

                // components.schemas
                if (node.has("components") && node.get("components").has("schemas")) {
                    node.get("components").get("schemas").fields().forEachRemaining(e ->
                            schemas.set(e.getKey(), e.getValue()));
                }
            }

            // securitySchemes(bearerAuth)
            ObjectNode securitySchemes = mapper.createObjectNode();
            ObjectNode bearerAuth = mapper.createObjectNode();
            bearerAuth.put("type", "http");
            bearerAuth.put("scheme", "bearer");
            bearerAuth.put("bearerFormat", "JWT");
            securitySchemes.set("bearerAuth", bearerAuth);

            components.set("schemas", schemas);
            components.set("securitySchemes", securitySchemes);

            // 전체 security 설정
            ArrayNode security = mapper.createArrayNode();
            ObjectNode secItem = mapper.createObjectNode();
            secItem.set("bearerAuth", mapper.createArrayNode());
            security.add(secItem);

            // 최종 조립
            root.set("paths", paths);
            root.set("components", components);
            root.set("tags", tags);
            root.set("security", security);

            return mapper.writeValueAsString(root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to merge OpenAPI docs", e);
        }
    }

    @GetMapping("/v3/api-docs/swagger-config")
    public Map<String, Object> swaggerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("url", "/v3/api-docs");
        return config;
    }
}
