package com.axelfrache.signalbroker.model.kafka;

import com.axelfrache.signalbroker.model.enums.ChannelType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RawInboundEvent(
                @NotNull UUID eventId,
                @NotNull ChannelType channelType,
                @NotBlank String sourceMessageId,
                @NotNull Instant timestamp,
                @NotBlank String contact,
                @NotBlank(message = "Body cannot be empty") String body,
                Map<String, String> metadata,
                int schemaVersion) {
}
