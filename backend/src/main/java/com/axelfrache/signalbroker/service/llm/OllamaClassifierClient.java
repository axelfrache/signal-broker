package com.axelfrache.signalbroker.service.llm;

import com.axelfrache.signalbroker.dto.ClassificationResult;
import com.axelfrache.signalbroker.exception.OllamaClientException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.service.ClassifierClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

            return mapper.readValue(response.response(), ClassificationResult.class);

        } catch (Exception e) {
            throw new OllamaClientException("Error calling Ollama API: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(FormattedTicketEvent formatted) {
        return """
                You are a customer support ticket classification assistant.
                Analyze the following support message.
                You MUST return a STRICT JSON object without any prose, formatting strings, or markdown ticks.
                The JSON must contain exact these fields:
                - "category": String. Must be exactly one of: AUTH, BILLING, BUG, OUTAGE, FEATURE_REQUEST, OTHER
                - "priority": String. Must be exactly one of: P1, P2, P3, P4
                - "labels": Array of Strings. Maximum 8 items, each max 20 characters.
                - "summary": String. Short summary of the issue.
                - "confidence": Number. Between 0.0 and 1.0 representing your confidence.

                Ticket Subject: %s
                Ticket Body: %s
                """.formatted(formatted.subject(), formatted.body());
    }

    private record OllamaResponse(String response) {
    }
}
