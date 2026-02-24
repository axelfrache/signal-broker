package com.axelfrache.signalbroker.service.kafka;

import com.axelfrache.signalbroker.model.enums.ProcessingStage;
import com.axelfrache.signalbroker.model.kafka.DlqEvent;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.service.AlertingService;
import com.axelfrache.signalbroker.service.EventPublisher;
import com.axelfrache.signalbroker.service.LabelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormattedEventConsumer {

    private final LabelingService labelingService;
    private final EventPublisher eventPublisher;
    private final AlertingService alertingService;

    @KafkaListener(topics = { "${kafka.topics.whatsapp.formatted}",
            "${kafka.topics.mail.formatted}" }, groupId = "${kafka.groups.labeler}", containerFactory = "formattedKafkaListenerContainerFactory")
    public void onFormatted(@lombok.NonNull FormattedTicketEvent formatted, @lombok.NonNull Acknowledgment ack) {
        try {
            var labeled = labelingService.label(formatted);
            eventPublisher.publishLabeled(labeled);
        } catch (Exception e) {
            alertingService.labelingFailed(formatted, e);
            eventPublisher.publishLabelDlq(buildDlqEvent(formatted, e));
        } finally {
            ack.acknowledge();
        }
    }

    private DlqEvent buildDlqEvent(@lombok.NonNull FormattedTicketEvent formatted, @lombok.NonNull Exception e) {
        var snippet = formatted.body() != null && formatted.body().length() > 200
                ? formatted.body().substring(0, 200) + "..."
                : formatted.body();

        return new DlqEvent(
                UUID.randomUUID(),
                ProcessingStage.LABEL,
                formatted.channel().name().toLowerCase() + ".formatted",
                e.getMessage(),
                snippet,
                Instant.now(),
                Map.of("ticketId", formatted.ticketId() != null ? formatted.ticketId().toString() : "unknown",
                        "contact", formatted.contact() != null ? formatted.contact() : "unknown"),
                1);
    }
}
