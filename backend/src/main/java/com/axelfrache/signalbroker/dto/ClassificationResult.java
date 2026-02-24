package com.axelfrache.signalbroker.dto;

import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;
import com.axelfrache.signalbroker.model.enums.TicketType;

public record ClassificationResult(
                String subject,
                Category category,
                TicketType ticketType,
                Priority priority,
                double confidence) {
}
