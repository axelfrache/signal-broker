package com.axelfrache.signalbroker.model.kafka;

import com.axelfrache.signalbroker.model.enums.ChannelType;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FormattedTicketEvent(
                @NotNull UUID ticketId,
                @NotNull ChannelType channel,
                @NotNull Instant receivedAt,
                @NotNull Instant createdAt,
                @NotBlank String body,
                @NotBlank String contact,
                int schemaVersion) {
}
