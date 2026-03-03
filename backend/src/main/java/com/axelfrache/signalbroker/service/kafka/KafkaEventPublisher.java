package com.axelfrache.signalbroker.service.kafka;

import com.axelfrache.signalbroker.config.properties.KafkaAppProperties;
import com.axelfrache.signalbroker.model.kafka.DlqEvent;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import com.axelfrache.signalbroker.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final KafkaTemplate<String, Object> schemaRegistryKafkaTemplate;
    private final KafkaAppProperties kafkaProperties;

    @Override
    public void publishWhatsappRaw(@lombok.NonNull RawInboundEvent event) {
        var key = event.contact() != null ? event.contact() : event.sourceMessageId();
        kafkaTemplate.send(kafkaProperties.topics().whatsapp().raw(), key, event);
    }

    @Override
    public void publishMailRaw(@lombok.NonNull RawInboundEvent event) {
        var key = event.contact() != null ? event.contact() : event.sourceMessageId();
        kafkaTemplate.send(kafkaProperties.topics().mail().raw(), key, event);
    }

    @Override
    public void publishFormatted(@lombok.NonNull FormattedTicketEvent event) {
        kafkaTemplate.send(kafkaProperties.topics().formatted(), event.ticketId().toString(), event);
    }

    @Override
    public void publishLabeled(@lombok.NonNull LabeledTicketEvent event) {
        schemaRegistryKafkaTemplate.send(kafkaProperties.topics().labeled(), event.ticketId().toString(), event);
    }

    @Override
    public void publishWhatsappFormatDlq(@lombok.NonNull DlqEvent event) {
        kafkaTemplate.send(kafkaProperties.topics().whatsapp().formatDlq(), event.eventId().toString(), event);
    }

    @Override
    public void publishMailFormatDlq(@lombok.NonNull DlqEvent event) {
        kafkaTemplate.send(kafkaProperties.topics().mail().formatDlq(), event.eventId().toString(), event);
    }

    @Override
    public void publishLabelDlq(@lombok.NonNull DlqEvent event) {
        kafkaTemplate.send(kafkaProperties.topics().labelDlq(), event.eventId().toString(), event);
    }
}
