package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
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
    private final AtomicLong nextCommunId = new AtomicLong(1);
    
    // Limite de tickets à garder en cache
    private static final int MAX_GROUPS = 100;

    public TicketGroupingService(
            @Qualifier("ollamaWebClient") WebClient ollamaWebClient,
            @Value("${ollama.model.classifier:qwen2.5:3b-instruct}") String model) {
        this.ollamaWebClient = ollamaWebClient;
        this.model = model;
    }

    /**
     * Assigne un communId au ticket en fonction de sa similarité avec les tickets existants
     */
    public Long assignCommunId(LabeledTicketEvent ticket) {
        synchronized (ticketGroups) {
            // Chercher un groupe similaire
            for (TicketGroup group : ticketGroups) {
                if (areSimilar(ticket, group.getRepresentative())) {
                    log.info("Ticket {} groupé avec communId {}", ticket.ticketId(), group.getCommunId());
                    group.addTicket(ticket);
                    return group.getCommunId();
                }
            }

            // Si aucun groupe similaire, créer un nouveau groupe
            Long newCommunId = nextCommunId.getAndIncrement();
            TicketGroup newGroup = new TicketGroup(newCommunId, ticket);
            ticketGroups.add(newGroup);
            
            // Limiter la taille du cache
            if (ticketGroups.size() > MAX_GROUPS) {
                ticketGroups.remove(0);
            }

            log.info("Nouveau groupe créé avec communId {} pour ticket {}", newCommunId, ticket.ticketId());
            return newCommunId;
        }
    }

    /**
     * Vérifie si deux tickets sont similaires (traitent du même problème)
     */
    private boolean areSimilar(LabeledTicketEvent ticket1, LabeledTicketEvent ticket2) {
        try {
            // Construire le prompt pour Ollama
            String prompt = buildSimilarityPrompt(ticket1, ticket2);
            
            // Construire la requête Ollama
            var request = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false);
            
            // Appeler Ollama via WebClient
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
            
            // Parser la réponse (on attend "OUI" ou "NON")
            boolean similar = response.response().trim().toUpperCase().startsWith("OUI");
            
            log.debug("Comparaison tickets {} et {}: {}", 
                ticket1.ticketId(), ticket2.ticketId(), similar ? "similaires" : "différents");
            
            return similar;
        } catch (Exception e) {
            log.error("Erreur lors de la comparaison des tickets", e);
            // En cas d'erreur, on considère qu'ils ne sont pas similaires
            return false;
        }
    }
    
    /**
     * Record pour la réponse d'Ollama
     */
    private record OllamaResponse(String response) {
    }

    private String buildSimilarityPrompt(LabeledTicketEvent ticket1, LabeledTicketEvent ticket2) {
        return String.format("""
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

    /**
     * Classe interne pour représenter un groupe de tickets similaires
     */
    private static class TicketGroup {
        private final Long communId;
        private final LabeledTicketEvent representative;
        private final List<LabeledTicketEvent> tickets;

        public TicketGroup(Long communId, LabeledTicketEvent representative) {
            this.communId = communId;
            this.representative = representative;
            this.tickets = new ArrayList<>();
            this.tickets.add(representative);
        }

        public Long getCommunId() {
            return communId;
        }

        public LabeledTicketEvent getRepresentative() {
            return representative;
        }

        public void addTicket(LabeledTicketEvent ticket) {
            tickets.add(ticket);
        }
    }
}
