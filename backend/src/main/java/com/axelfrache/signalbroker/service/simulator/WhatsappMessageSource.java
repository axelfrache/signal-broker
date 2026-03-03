package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.model.enums.ChannelType;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WhatsappMessageSource implements MessageSource {

    private static final Logger log = LoggerFactory.getLogger(WhatsappMessageSource.class);
    private static final String RESOURCE_PATH = "simulator/whatsapp.json";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String FALLBACK_PHONE = "+33610000000";
    private static final String FALLBACK_MESSAGE = "Bonjour, l'application plante quand j'ouvre mon historique.";
    private static final String METADATA_PLATFORM = "ios";

    private final AtomicInteger counter = new AtomicInteger();
    private final List<WhatsappExample> examples;

    public WhatsappMessageSource(ObjectMapper objectMapper) {
        this.examples = loadExamples(objectMapper);
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.WHATSAPP;
    }

    @Override
    public RawInboundEvent next() {
        var sequence = counter.incrementAndGet();
        var example = examples.get(Math.floorMod(sequence - 1, examples.size()));
        return new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.WHATSAPP,
                "msg-wa-" + sequence,
                example.timestamp(),
                example.telephone(),
                example.message(),
                Map.of("platform", METADATA_PLATFORM),
                1);
    }

    private List<WhatsappExample> loadExamples(ObjectMapper objectMapper) {
        try (var input = new ClassPathResource(RESOURCE_PATH).getInputStream()) {
            List<RawWhatsappExample> loaded = objectMapper.readerForListOf(RawWhatsappExample.class).readValue(input);
            if (loaded == null || loaded.isEmpty()) {
                throw new IllegalStateException("No whatsapp examples found");
            }
            return loaded.stream().map(this::toWhatsappExample).toList();
        } catch (IOException | RuntimeException exception) {
            log.warn("Failed to load {}, using fallback whatsapp example", RESOURCE_PATH, exception);
            return List.of(new WhatsappExample(Instant.now(), FALLBACK_PHONE, FALLBACK_MESSAGE));
        }
    }

    private WhatsappExample toWhatsappExample(RawWhatsappExample raw) {
        return new WhatsappExample(
                parseTimestamp(raw.date()),
                normalize(raw.telephone(), FALLBACK_PHONE),
                normalize(raw.message(), FALLBACK_MESSAGE));
    }

    private Instant parseTimestamp(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return Instant.now();
        }

        try {
            return LocalDateTime.parse(rawDate, INPUT_DATE_FORMATTER).toInstant(ZoneOffset.UTC);
        } catch (DateTimeException exception) {
            return Instant.now();
        }
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawWhatsappExample(String date, String telephone, String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WhatsappExample(Instant timestamp, String telephone, String message) {
    }
}
