package com.axelfrache.signalbroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    private String ticketId;
    private String subject;
    private String contact;
    private Double confidence;
    private Double receivedAt;
    private Double labeledAt;
    private String category;
    private String priority;
    private String ticketType;
}
