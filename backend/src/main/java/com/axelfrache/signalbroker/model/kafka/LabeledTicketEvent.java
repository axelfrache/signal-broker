package com.axelfrache.signalbroker.model.kafka;

import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;
import com.axelfrache.signalbroker.model.enums.TicketType;

import java.time.Instant;
import java.util.UUID;

public record LabeledTicketEvent(
                UUID ticketId,
                Instant receivedAt,
                Instant labeledAt,
                String subject,
                String body,
                String contact,
                Category category,
                TicketType ticketType,
                Priority priority,
                double confidence,
                Long communId,
                int schemaVersion) {
}
