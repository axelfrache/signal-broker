package com.axelfrache.signalbroker.dto;

import java.time.Instant;
import java.util.UUID;

public record TicketCommentDto(
        UUID id,
        UUID ticketId,
        String authorName,
        String body,
        Instant createdAt) {
}
