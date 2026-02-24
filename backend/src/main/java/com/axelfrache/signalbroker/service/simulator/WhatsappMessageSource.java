package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.model.enums.ChannelType;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WhatsappMessageSource implements MessageSource {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public ChannelType channelType() {
        return ChannelType.WHATSAPP;
    }

    @Override
    public RawInboundEvent next() {
        int id = counter.incrementAndGet();
        return new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.WHATSAPP,
                "msg-wa-" + id,
                Instant.now(),
                "+1234567890" + id,
                "Hey the app is crushing on my iphone 15! Fix asap!!!",
                Map.of("platform", "ios"),
                1);
    }
}
