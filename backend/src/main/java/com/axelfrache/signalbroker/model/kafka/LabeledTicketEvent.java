package com.axelfrache.signalbroker.model.kafka;

import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;
import com.axelfrache.signalbroker.model.enums.TicketType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LabeledTicketEvent(
                UUID ticketId,
                Instant labeledAt,
                String subject,
                Category category,
                TicketType ticketType,
                Priority priority,
                List<String> labels,
                String summary,
                double confidence,
                int schemaVersion) {
}
