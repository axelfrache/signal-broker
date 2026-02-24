package com.axelfrache.signalbroker.model.kafka;

import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LabeledTicketEvent(
        UUID eventId,
        UUID ticketId,
        Instant labeledAt,
        Category category,
        Priority priority,
        List<String> labels,
        String summary,
        double confidence,
        int schemaVersion) {
}
