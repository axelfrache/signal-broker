package com.axelfrache.signalbroker.model.kafka.connect;

import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConnectEventMapper {

    private static final ConnectSchema LABELED_TICKET_SCHEMA = new ConnectSchema(
            "struct",
            false,
            "labeled_tickets",
            List.of(
                    new ConnectField("string", false, "ticketId"),
                    new ConnectField("string", true, "receivedAt"),
                    new ConnectField("string", true, "labeledAt"),
                    new ConnectField("string", true, "subject"),
                    new ConnectField("string", true, "body"),
                    new ConnectField("string", true, "contact"),
                    new ConnectField("string", true, "category"),
                    new ConnectField("string", true, "ticketType"),
                    new ConnectField("string", true, "priority"),
                    new ConnectField("double", true, "confidence"),
                    new ConnectField("int32", false, "schemaVersion")));

    public record LabeledTicketConnectPayload(
            String ticketId,
            String receivedAt,
            String labeledAt,
            String subject,
            String body,
            String contact,
            String category,
            String ticketType,
            String priority,
            double confidence,
            int schemaVersion) {
    }

    public static ConnectPayload<LabeledTicketConnectPayload> toConnectPayload(LabeledTicketEvent event) {
        var payload = new LabeledTicketConnectPayload(
                event.ticketId().toString(),
                event.receivedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(event.receivedAt()) : null,
                event.labeledAt() != null ? DateTimeFormatter.ISO_INSTANT.format(event.labeledAt()) : null,
                event.subject(),
                event.body(),
                event.contact(),
                event.category() != null ? event.category().name() : null,
                event.ticketType() != null ? event.ticketType().name() : null,
                event.priority() != null ? event.priority().name() : null,
                event.confidence(),
                event.schemaVersion());

        return new ConnectPayload<>(LABELED_TICKET_SCHEMA, payload);
    }
}
