package com.axelfrache.signalbroker.service.kafka;

import com.axelfrache.signalbroker.model.kafka.DlqEvent;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import com.axelfrache.signalbroker.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.whatsapp.raw}")
    private String whatsappRawTopic;

    @Value("${kafka.topics.mail.raw}")
    private String mailRawTopic;

    @Value("${kafka.topics.formatted}")
    private String formattedTopic;

    @Value("${kafka.topics.labeled}")
    private String labeledTopic;

    @Value("${kafka.topics.whatsapp.formatDlq}")
    private String whatsappFormatDlqTopic;

    @Value("${kafka.topics.mail.formatDlq}")
    private String mailFormatDlqTopic;

    @Value("${kafka.topics.labelDlq}")
    private String labelDlqTopic;

    @Override
    public void publishWhatsappRaw(@lombok.NonNull RawInboundEvent event) {
        var key = event.contact() != null ? event.contact() : event.sourceMessageId();
        kafkaTemplate.send(whatsappRawTopic, key, event);
    }

    @Override
    public void publishMailRaw(@lombok.NonNull RawInboundEvent event) {
        var key = event.contact() != null ? event.contact() : event.sourceMessageId();
        kafkaTemplate.send(mailRawTopic, key, event);
    }

    @Override
    public void publishFormatted(@lombok.NonNull FormattedTicketEvent event) {
        kafkaTemplate.send(formattedTopic, event.ticketId().toString(), event);
    }

    @Override
    public void publishLabeled(@lombok.NonNull LabeledTicketEvent event) {
        var connectPayload = com.axelfrache.signalbroker.model.kafka.connect.ConnectEventMapper.toConnectPayload(event);
        kafkaTemplate.send(labeledTopic, event.ticketId().toString(), connectPayload);
    }

    @Override
    public void publishWhatsappFormatDlq(@lombok.NonNull DlqEvent event) {
        kafkaTemplate.send(whatsappFormatDlqTopic, event.eventId().toString(), event);
    }

    @Override
    public void publishMailFormatDlq(@lombok.NonNull DlqEvent event) {
        kafkaTemplate.send(mailFormatDlqTopic, event.eventId().toString(), event);
    }

    @Override
    public void publishLabelDlq(@lombok.NonNull DlqEvent event) {
        kafkaTemplate.send(labelDlqTopic, event.eventId().toString(), event);
    }
}
