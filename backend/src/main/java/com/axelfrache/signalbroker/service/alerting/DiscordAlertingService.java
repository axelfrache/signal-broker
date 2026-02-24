package com.axelfrache.signalbroker.service.alerting;

import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import com.axelfrache.signalbroker.service.AlertingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class DiscordAlertingService implements AlertingService {

    private final WebClient webClient;
    private final boolean enabled;
    private final String webhookUrl;

    public DiscordAlertingService(WebClient.Builder webClientBuilder,
            @Value("${discord.enabled:false}") boolean enabled,
            @Value("${discord.webhookUrl:}") String webhookUrl) {
        this.webClient = webClientBuilder.build();
        this.enabled = enabled;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void formattingFailed(@lombok.NonNull RawInboundEvent raw, @lombok.NonNull Exception e) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        var snippet = raw.body() != null && raw.body().length() > 100
                ? raw.body().substring(0, 100) + "..."
                : raw.body();

        var message = """
                🚨 **Formatting Failed**
                Source: %s
                Contact: %s
                Error: %s
                Snippet: ```%s```
                """.formatted(raw.sourceType(), raw.contact(), e.getMessage(), snippet);

        sendDiscordMessage(message);
    }

    @Override
    public void labelingFailed(@lombok.NonNull FormattedTicketEvent formatted, @lombok.NonNull Exception e) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        var snippet = formatted.body() != null && formatted.body().length() > 100
                ? formatted.body().substring(0, 100) + "..."
                : formatted.body();

        var message = """
                🚨 **Labeling Failed**
                Ticket ID: %s
                Channel: %s
                Contact: %s
                Error: %s
                Snippet: ```%s```
                """.formatted(formatted.ticketId(), formatted.channel(), formatted.contact(), e.getMessage(), snippet);

        sendDiscordMessage(message);
    }

    private void sendDiscordMessage(String text) {
        try {
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(Map.of("content", text))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
        } catch (Exception ex) {
            System.err.println("Failed to send discord alert: " + ex.getMessage());
        }
    }
}
