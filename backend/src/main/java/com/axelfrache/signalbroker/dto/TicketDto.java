package com.axelfrache.signalbroker.dto;

public record TicketDto(
        String ticketId,
        String subject,
        String contact,
        Double confidence,
        Double receivedAt,
        Double labeledAt,
        String category,
        String priority,
        String ticketType,
        Long commonId) {
}
