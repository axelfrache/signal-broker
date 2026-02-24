package com.axelfrache.signalbroker.model.kafka.connect;

public record ConnectField(
        String type,
        boolean optional,
        String field) {
}
