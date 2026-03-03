package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.config.properties.OllamaProperties;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketGroupingService {

    private final WebClient ollamaWebClient;
    private final OllamaProperties ollamaProperties;
    private final ConcurrentLinkedDeque<TicketGroup> ticketGroups = new ConcurrentLinkedDeque<>();
    private final AtomicLong nextCommonId = new AtomicLong(1);

    private static final int MAX_GROUPS = 100;

    public Long assignCommonId(LabeledTicketEvent ticket) {
        for (var group : ticketGroups) {
            if (areSimilar(ticket, group.representative())) {
                log.info("Ticket {} groupé avec commonId {}", ticket.ticketId(), group.commonId());
                group.addTicket(ticket);
                return group.commonId();
            }
        }

        var newCommonId = nextCommonId.getAndIncrement();
        var newGroup = new TicketGroup(newCommonId, ticket);
        ticketGroups.addLast(newGroup);

        while (ticketGroups.size() > MAX_GROUPS) {
            ticketGroups.pollFirst();
        }

        log.info("Nouveau groupe créé avec commonId {} pour ticket {}", newCommonId, ticket.ticketId());
        return newCommonId;
    }

    private boolean areSimilar(LabeledTicketEvent ticket1, LabeledTicketEvent ticket2) {
        try {
            var prompt = buildSimilarityPrompt(ticket1, ticket2);

            var request = Map.of(
                    "model", ollamaProperties.model().classifier(),
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

    private record TicketGroup(Long commonId, LabeledTicketEvent representative, List<LabeledTicketEvent> tickets) {
        private TicketGroup(Long commonId, LabeledTicketEvent representative) {
            this(commonId, representative, new CopyOnWriteArrayList<>(List.of(representative)));
        }

        private void addTicket(LabeledTicketEvent ticket) {
            this.tickets.add(ticket);
        }
    }
}
