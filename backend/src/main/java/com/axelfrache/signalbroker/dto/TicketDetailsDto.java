package com.axelfrache.signalbroker.dto;

public record TicketDetailsDto(
        String ticketId,
        String subject,
        String contact,
        Double confidence,
        Double receivedAt,
        Double labeledAt,
        String category,
        String priority,
        String ticketType,
        Long commonId,
        String body,
        Long schemaVersion) {
}
