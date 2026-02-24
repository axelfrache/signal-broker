package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.model.enums.SourceType;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;

public interface MessageSource {
    SourceType sourceType();

    RawInboundEvent next();
}
