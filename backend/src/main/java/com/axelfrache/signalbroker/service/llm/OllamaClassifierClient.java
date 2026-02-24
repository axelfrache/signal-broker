package com.axelfrache.signalbroker.service.llm;

import com.axelfrache.signalbroker.dto.ClassificationResult;
import com.axelfrache.signalbroker.exception.OllamaClientException;
import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;
import com.axelfrache.signalbroker.model.enums.TicketType;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.service.ClassifierClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OllamaClassifierClient implements ClassifierClient {

    private final WebClient webClient;
    private final String model;
    private final ObjectMapper mapper;

    private static final Map<String, Category> CATEGORY_ALIASES = Map.ofEntries(
            Map.entry("FRONTEND", Category.FRONTEND),
            Map.entry("WEB_FRONTEND", Category.FRONTEND),
            Map.entry("WEB", Category.FRONTEND),
            Map.entry("UI", Category.FRONTEND),
            Map.entry("BACKEND", Category.BACKEND),
            Map.entry("API", Category.BACKEND),
            Map.entry("SERVER", Category.BACKEND),
            Map.entry("INFRA", Category.INFRA),
            Map.entry("INFRASTRUCTURE", Category.INFRA),
            Map.entry("DEVOPS", Category.INFRA),
            Map.entry("MOBILE", Category.MOBILE),
            Map.entry("IOS", Category.MOBILE),
            Map.entry("ANDROID", Category.MOBILE)
    );

    private static final Map<String, TicketType> TICKET_TYPE_ALIASES = Map.of(
            "FEATURE", TicketType.FEATURE,
            "FEATURE_REQUEST", TicketType.FEATURE,
            "BUG", TicketType.BUG,
            "BUGFIX", TicketType.BUG,
            "OTHER", TicketType.OTHER,
            "QUESTION", TicketType.OTHER,
            "SUPPORT", TicketType.OTHER
    );

    private static final Map<String, Priority> PRIORITY_ALIASES = Map.of(
            "P0", Priority.P0,
            "P1", Priority.P1,
            "P2", Priority.P2,
            "P3", Priority.P3,
            "CRITICAL", Priority.P0,
            "HIGH", Priority.P1,
            "MEDIUM", Priority.P2,
            "LOW", Priority.P3
    );

    public OllamaClassifierClient(
            @org.springframework.beans.factory.annotation.Qualifier("ollamaWebClient") WebClient webClient,
            @Value("${ollama.model.classifier:qwen2.5:3b-instruct}") String model) {
        this.webClient = webClient;
        this.model = model;
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ClassificationResult classify(@lombok.NonNull FormattedTicketEvent formatted) throws OllamaClientException {
        var prompt = buildPrompt(formatted);

        try {
            var request = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json");

            var response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> Mono.error(
                                    new OllamaClientException("Ollama API error: " + clientResponse.statusCode())))
                    .bodyToMono(OllamaResponse.class)
                    .block();

            if (response == null || response.response() == null) {
                throw new OllamaClientException("Empty response from Ollama API");
            }

            return parseResponse(response.response());

        } catch (OllamaClientException e) {
            throw e;
        } catch (Exception e) {
            throw new OllamaClientException("Error calling Ollama API: " + e.getMessage(), e);
        }
    }

    private ClassificationResult parseResponse(String json) throws OllamaClientException {
        try {
            JsonNode node = mapper.readTree(json);

            var rawCategory = getTextOrDefault(node, "category", "OTHER");
            var rawTicketType = getTextOrDefault(node, "ticketType", "OTHER");
            var rawPriority = getTextOrDefault(node, "priority", "P2");
            var subject = getTextOrDefault(node, "subject", "No subject");
            var confidence = node.has("confidence") ? node.get("confidence").asDouble(0.5) : 0.5;

            var category = CATEGORY_ALIASES.getOrDefault(rawCategory.toUpperCase(), Category.BACKEND);
            var ticketType = TICKET_TYPE_ALIASES.getOrDefault(rawTicketType.toUpperCase(), TicketType.OTHER);
            var priority = PRIORITY_ALIASES.getOrDefault(rawPriority.toUpperCase(), Priority.P2);

            return new ClassificationResult(subject, category, ticketType, priority, confidence);

        } catch (Exception e) {
            throw new OllamaClientException("Failed to parse Ollama response: " + e.getMessage(), e);
        }
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText(defaultValue) : defaultValue;
    }

    private String buildPrompt(FormattedTicketEvent formatted) {
        return """
                You are a support ticket classifier. Analyze the message and return ONLY a JSON object.
                
                CRITICAL: Use ONLY these EXACT values (no variations, no synonyms):
                
                {
                  "subject": "short description, max 80 chars",
                  "category": "FRONTEND | BACKEND | INFRA | MOBILE",
                  "ticketType": "FEATURE | BUG | OTHER",
                  "priority": "P0 | P1 | P2 | P3",
                  "confidence": 0.0 to 1.0
                }

                Priority guide: P0 = production down, P1 = major impact, P2 = moderate, P3 = minor.
                
                Message: %s
                """.formatted(formatted.body());
    }

    private record OllamaResponse(String response) {
    }
}
