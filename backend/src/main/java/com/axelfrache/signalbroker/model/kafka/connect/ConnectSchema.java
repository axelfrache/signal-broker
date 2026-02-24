package com.axelfrache.signalbroker.model.kafka.connect;

import java.util.List;

public record ConnectSchema(
        String type,
        boolean optional,
        String name,
        List<ConnectField> fields) {
}
