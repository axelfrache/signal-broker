package com.axelfrache.signalbroker.service.formatting;

import com.axelfrache.signalbroker.exception.FormattingException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import com.axelfrache.signalbroker.service.FormattingService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DefaultFormattingService implements FormattingService {

    @Override
    public FormattedTicketEvent format(@lombok.NonNull RawInboundEvent raw) throws FormattingException {
        if (raw.body() == null || raw.body().isBlank()) {
            throw new FormattingException("Body cannot be empty");
        }
        if (raw.contact() == null || raw.contact().isBlank()) {
            throw new FormattingException("Contact cannot be empty");
        }

        var normalizedBody = raw.body().trim().replaceAll("\\s+", " ");

        return new FormattedTicketEvent(
                UUID.randomUUID(),
                raw.channelType(),
                Instant.now(),
                normalizedBody,
                raw.contact(),
                1);
    }
}
