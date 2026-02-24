package com.axelfrache.signalbroker.dto;

import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;
import com.axelfrache.signalbroker.model.enums.TicketType;

import java.util.List;

public record ClassificationResult(
                String subject,
                Category category,
                TicketType ticketType,
                Priority priority,
                List<String> labels,
                String summary,
                double confidence) {
}
