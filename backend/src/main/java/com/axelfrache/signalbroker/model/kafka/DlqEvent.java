package com.axelfrache.signalbroker.model.kafka;

import com.axelfrache.signalbroker.model.enums.ProcessingStage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DlqEvent(
                @NotNull UUID eventId,
                @NotNull ProcessingStage stage,
                @NotBlank String originalTopic,
                @NotBlank String reason,
                String payloadSnippet,
                @NotNull Instant failedAt,
                Map<String, String> metadata,
                int schemaVersion) {
}
