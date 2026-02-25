package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TicketGroupingService {

    private final WebClient ollamaWebClient;
    private final String model;
    private final List<TicketGroup> ticketGroups = new ArrayList<>();
    private final AtomicLong nextCommonId = new AtomicLong(1);

    private static final int MAX_GROUPS = 100;

    public TicketGroupingService(
            @Qualifier("ollamaWebClient") WebClient ollamaWebClient,
            @Value("${ollama.model.classifier:qwen2.5:3b-instruct}") String model) {
        this.ollamaWebClient = ollamaWebClient;
        this.model = model;
    }

    public Long assignCommonId(LabeledTicketEvent ticket) {
        synchronized (ticketGroups) {
            for (var group : ticketGroups) {
                if (areSimilar(ticket, group.getRepresentative())) {
                    log.info("Ticket {} groupé avec commonId {}", ticket.ticketId(), group.getCommonId());
                    group.addTicket(ticket);
                    return group.getCommonId();
                }
            }

            var newCommonId = nextCommonId.getAndIncrement();
            var newGroup = new TicketGroup(newCommonId, ticket);
            ticketGroups.add(newGroup);

            if (ticketGroups.size() > MAX_GROUPS) {
                ticketGroups.removeFirst();
            }

            log.info("Nouveau groupe créé avec commonId {} pour ticket {}", newCommonId, ticket.ticketId());
            return newCommonId;
        }
    }

    private boolean areSimilar(LabeledTicketEvent ticket1, LabeledTicketEvent ticket2) {
        try {
            var prompt = buildSimilarityPrompt(ticket1, ticket2);

            var request = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false);

            var response = ollamaWebClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .block();

            if (response == null || response.response() == null) {
                log.error("Réponse vide d'Ollama lors de la comparaison de tickets");
                return false;
            }

            var similar = response.response().trim().toUpperCase().startsWith("OUI");

            log.debug("Comparaison tickets {} et {}: {}",
                    ticket1.ticketId(), ticket2.ticketId(), similar ? "similaires" : "différents");

            return similar;
        } catch (Exception e) {
            log.error("Erreur lors de la comparaison des tickets", e);
            return false;
        }
    }

    private record OllamaResponse(String response) {
    }

    private String buildSimilarityPrompt(LabeledTicketEvent ticket1, LabeledTicketEvent ticket2) {
        return String.format(
                """
                        Tu es un expert en analyse de tickets de support technique.

                        Voici deux tickets utilisateurs :

                        TICKET 1:
                        Sujet: %s
                        Description: %s
                        Catégorie: %s
                        Priorité: %s

                        TICKET 2:
                        Sujet: %s
                        Description: %s
                        Catégorie: %s
                        Priorité: %s

                        Question: Ces deux tickets décrivent-ils le MÊME problème technique sous-jacent ?

                        Réponds UNIQUEMENT par "OUI" si les tickets traitent du même problème (même si les mots utilisés sont différents).
                        Réponds "NON" s'ils traitent de problèmes différents.

                        Exemples:
                        - "Page ne charge pas" et "Écran blanc au chargement" → OUI
                        - "Erreur 404 sur /dashboard" et "Page introuvable dashboard" → OUI
                        - "Impossible de se connecter" et "Erreur 404" → NON

                        Réponse:""",
                ticket1.subject(), ticket1.body(), ticket1.category(), ticket1.priority(),
                ticket2.subject(), ticket2.body(), ticket2.category(), ticket2.priority());
    }

    @Getter
    private static class TicketGroup {
        private final Long commonId;
        private final LabeledTicketEvent representative;
        private final List<LabeledTicketEvent> tickets;

        public TicketGroup(Long commonId, LabeledTicketEvent representative) {
            this.commonId = commonId;
            this.representative = representative;
            this.tickets = new ArrayList<>();
            this.tickets.add(representative);
        }

        public void addTicket(LabeledTicketEvent ticket) {
            tickets.add(ticket);
        }
    }
}
