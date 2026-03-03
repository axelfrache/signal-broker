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
public class MailMessageSource implements MessageSource {

    private static final Logger log = LoggerFactory.getLogger(MailMessageSource.class);
    private static final String RESOURCE_PATH = "simulator/mail.json";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String FALLBACK_MAIL = "user@example.com";
    private static final String FALLBACK_MESSAGE = "Bonjour, je rencontre un souci de connexion sur mon compte.";
    private static final String METADATA_CLIENT = "webmail";

    private final AtomicInteger counter = new AtomicInteger();
    private final List<MailExample> examples;

    public MailMessageSource(ObjectMapper objectMapper) {
        this.examples = loadExamples(objectMapper);
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.MAIL;
    }

    @Override
    public RawInboundEvent next() {
        var sequence = counter.incrementAndGet();
        var example = examples.get(Math.floorMod(sequence - 1, examples.size()));
        return new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.MAIL,
                "msg-mail-" + sequence,
                example.timestamp(),
                example.mail(),
                example.message(),
                Map.of("client", METADATA_CLIENT),
                1);
    }

    private List<MailExample> loadExamples(ObjectMapper objectMapper) {
        try (var input = new ClassPathResource(RESOURCE_PATH).getInputStream()) {
            List<RawMailExample> loaded = objectMapper.readerForListOf(RawMailExample.class).readValue(input);
            if (loaded == null || loaded.isEmpty()) {
                throw new IllegalStateException("No mail examples found");
            }
            return loaded.stream().map(this::toMailExample).toList();
        } catch (IOException | RuntimeException exception) {
            log.warn("Failed to load {}, using fallback mail example", RESOURCE_PATH, exception);
            return List.of(new MailExample(Instant.now(), FALLBACK_MAIL, FALLBACK_MESSAGE));
        }
    }

    private MailExample toMailExample(RawMailExample raw) {
        return new MailExample(
                parseTimestamp(raw.date()),
                normalize(raw.mail(), FALLBACK_MAIL),
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
    private record RawMailExample(String date, String mail, String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MailExample(Instant timestamp, String mail, String message) {
    }
}
