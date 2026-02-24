package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.model.enums.SourceType;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MailMessageSource implements MessageSource {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public SourceType sourceType() {
        return SourceType.MAIL;
    }

    @Override
    public RawInboundEvent next() {
        int id = counter.incrementAndGet();
        return new RawInboundEvent(
                UUID.randomUUID(),
                SourceType.MAIL,
                "msg-mail-" + id,
                Instant.now(),
                "user" + id + "@example.com",
                "Hello, I am having trouble logging into my account. Could you please help?\n\nBest, User" + id,
                Map.of("client", "webmail"),
                1);
    }
}
