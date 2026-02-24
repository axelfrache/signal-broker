package com.axelfrache.signalbroker.model.kafka.connect;

public record ConnectPayload<T>(
        ConnectSchema schema,
        T payload) {
}
