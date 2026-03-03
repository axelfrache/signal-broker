package com.axelfrache.signalbroker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kafka")
public record KafkaAppProperties(
        String bootstrapServers,
        Groups groups,
        Concurrency concurrency,
        SchemaRegistry schemaRegistry,
        Topics topics) {

    public record Groups(String formatter, String labeler) {
    }

    public record Concurrency(int formatter, int labeler) {
    }

    public record SchemaRegistry(String url) {
    }

    public record Topics(
            ChannelTopics whatsapp,
            ChannelTopics mail,
            String formatted,
            String labeled,
            String labelDlq) {
    }

    public record ChannelTopics(String raw, String formatDlq) {
    }
}
