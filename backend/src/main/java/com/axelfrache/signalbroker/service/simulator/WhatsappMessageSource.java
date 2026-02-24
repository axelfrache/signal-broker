package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.model.enums.SourceType;
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
    public SourceType sourceType() {
        return SourceType.WHATSAPP;
    }

    @Override
    public RawInboundEvent next() {
        int id = counter.incrementAndGet();
        return new RawInboundEvent(
                UUID.randomUUID(),
                SourceType.WHATSAPP,
                "msg-wa-" + id,
                Instant.now(),
                "+1234567890" + id,
                "Hey the app is crushing on my iphone 15! Fix asap!!!",
                Map.of("platform", "ios"),
                1);
    }
}
