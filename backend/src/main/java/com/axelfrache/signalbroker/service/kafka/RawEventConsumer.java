package com.axelfrache.signalbroker.service.kafka;

import com.axelfrache.signalbroker.model.enums.ProcessingStage;
import com.axelfrache.signalbroker.model.kafka.DlqEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;

import com.axelfrache.signalbroker.service.EventPublisher;
import com.axelfrache.signalbroker.service.FormattingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RawEventConsumer {

    private final FormattingService formattingService;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "${kafka.topics.whatsapp.raw}", groupId = "${kafka.groups.formatter}", containerFactory = "rawKafkaListenerContainerFactory")
    public void onWhatsappRaw(@lombok.NonNull RawInboundEvent raw, @lombok.NonNull Acknowledgment ack) {
        try {
            var formatted = formattingService.format(raw);
            eventPublisher.publishFormatted(formatted);
        } catch (Exception e) {
            eventPublisher.publishWhatsappFormatDlq(buildDlqEvent(raw, "support.whatsapp.raw", e));
        } finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "${kafka.topics.mail.raw}", groupId = "${kafka.groups.formatter}", containerFactory = "rawKafkaListenerContainerFactory")
    public void onMailRaw(@lombok.NonNull RawInboundEvent raw, @lombok.NonNull Acknowledgment ack) {
        try {
            var formatted = formattingService.format(raw);
            eventPublisher.publishFormatted(formatted);
        } catch (Exception e) {
            eventPublisher.publishMailFormatDlq(buildDlqEvent(raw, "support.mail.raw", e));
        } finally {
            ack.acknowledge();
        }
    }

    private DlqEvent buildDlqEvent(@lombok.NonNull RawInboundEvent raw, String sourceTopic,
            @lombok.NonNull Exception e) {
        var snippet = raw.body() != null && raw.body().length() > 200
                ? raw.body().substring(0, 200) + "..."
                : raw.body();

        return new DlqEvent(
                UUID.randomUUID(),
                ProcessingStage.FORMAT,
                sourceTopic,
                e.getMessage(),
                snippet,
                Instant.now(),
                Map.of("rawEventId", raw.eventId() != null ? raw.eventId().toString() : "unknown",
                        "contact", raw.contact() != null ? raw.contact() : "unknown"),
                1);
    }
}
