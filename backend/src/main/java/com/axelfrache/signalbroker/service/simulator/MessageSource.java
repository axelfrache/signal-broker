package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.model.enums.ChannelType;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;

public interface MessageSource {
    ChannelType channelType();

    RawInboundEvent next();
}
